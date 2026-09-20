package com.pgis.caseinfo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class PoiSearchClient {

    private static final Logger log = LoggerFactory.getLogger(PoiSearchClient.class);

    /** 响应体日志最大长度，避免大响应刷屏 */
    private static final int MAX_BODY_LOG = 800;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;
    private final PoiProperties properties;

    public PoiSearchClient(ObjectMapper objectMapper, PoiProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    /**
     * 调用地图 POI 搜索接口，返回第一个结果坐标。
     *
     * @return String[]{经度, 纬度}；未找到或调用失败返回 null
     */
    public String[] search(String keywords, String region) {
        String form = "keywords=" + URLEncoder.encode(keywords == null ? "" : keywords, StandardCharsets.UTF_8)
                + (region != null && !region.isBlank()
                        ? "&region=" + URLEncoder.encode(region, StandardCharsets.UTF_8)
                        : "")
                + "&limit=1";

        log.info("POI请求开始: url={}, keywords={}, region={}, timeout={}s",
                properties.getBaseUrl(), keywords, region, properties.getTimeoutSeconds());

        long start = System.currentTimeMillis();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getBaseUrl()))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long elapsed = System.currentTimeMillis() - start;
            String body = response.body();

            log.info("POI响应: HTTP {}, 耗时{}ms, keywords={}, body={}",
                    response.statusCode(), elapsed, keywords, abbreviate(body));

            if (response.statusCode() != 200) {
                log.warn("POI请求失败: HTTP状态码={}, keywords={}, region={}",
                        response.statusCode(), keywords, region);
                return null;
            }

            JsonNode root = objectMapper.readTree(body == null ? "" : body);
            JsonNode list = root.get("list");
            if (list == null || !list.isArray() || list.isEmpty()) {
                log.warn("POI无结果: keywords={}, region={}, 响应中list为空", keywords, region);
                return null;
            }
            JsonNode coords = list.get(0).path("geometry").path("coordinates");
            if (!coords.isArray() || coords.size() < 2) {
                log.warn("POI结果缺少坐标: keywords={}, region={}, 首条结果={}",
                        keywords, region, abbreviate(list.get(0).toString()));
                return null;
            }
            String lng = coords.get(0).asText();
            String lat = coords.get(1).asText();
            double lngNum = Double.parseDouble(lng);
            double latNum = Double.parseDouble(lat);
            if (lngNum < -180 || lngNum > 180 || latNum < -90 || latNum > 90) {
                log.warn("POI坐标超出合法范围被丢弃: keywords={}, region={}, lng={}, lat={}",
                        keywords, region, lng, lat);
                return null;
            }
            String name = list.get(0).path("properties").path("name").asText("");
            log.info("POI命中: keywords={}, region={}, name={}, lng={}, lat={}",
                    keywords, region, name, lng, lat);
            return new String[]{lng, lat};
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.warn("POI调用异常: keywords={}, region={}, 耗时{}ms, {}: {}",
                    keywords, region, elapsed, e.getClass().getSimpleName(), e.getMessage());
            log.debug("POI调用异常堆栈", e);
            return null;
        }
    }

    private static String abbreviate(String text) {
        if (text == null) {
            return "<null>";
        }
        String oneLine = text.replaceAll("\\s+", " ").trim();
        return oneLine.length() <= MAX_BODY_LOG
                ? oneLine
                : oneLine.substring(0, MAX_BODY_LOG) + "...(截断,共" + oneLine.length() + "字符)";
    }
}
