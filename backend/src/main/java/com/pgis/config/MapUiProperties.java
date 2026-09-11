package com.pgis.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "pgis.map")
public class MapUiProperties {

    /** 启动时默认选中的底图 id，需与 basemaps[].id 对应 */
    private String defaultBasemap;

    /** 可切换的底图列表；为空时用下面的单层 tile-url 兜底 */
    private List<Basemap> basemaps = new ArrayList<>();

    /** Leaflet 瓦片地址（无 basemaps 时的兜底），可用 {s} {x} {y} {z} */
    private String tileUrl = "https://webst0{s}.is.autonavi.com/appmaptile?style=7&x={x}&y={y}&z={z}";

    private String tileSubdomains = "1234";

    private String tileAttribution = "&copy; 高德地图";

    private int tileMaxZoom = 18;

    /** 只选省搜索时的缩放级别 */
    private int provinceZoom = 6;

    /** 选市搜索时 fitBounds 的最大缩放 */
    private int cityMaxZoom = 11;

    private double fallbackMinLng = 113.45;

    private double fallbackMinLat = 36.05;

    private double fallbackMaxLng = 119.87;

    private double fallbackMaxLat = 42.62;

    @PostConstruct
    void ensureBasemaps() {
        if (basemaps == null) {
            basemaps = new ArrayList<>();
        }
        if (basemaps.isEmpty()) {
            Basemap fallback = new Basemap();
            fallback.setId("default");
            fallback.setLabel("默认底图");
            fallback.setTileUrl(tileUrl);
            fallback.setTileSubdomains(tileSubdomains);
            fallback.setTileAttribution(tileAttribution);
            fallback.setTileMaxZoom(tileMaxZoom);
            basemaps.add(fallback);
        }
        if (!StringUtils.hasText(defaultBasemap)
                || basemaps.stream().noneMatch(item -> defaultBasemap.equals(item.getId()))) {
            defaultBasemap = basemaps.get(0).getId();
        }
    }

    @Data
    public static class Basemap {
        private String id;
        private String label;
        private String tileUrl;
        private String tileSubdomains = "";
        private String tileAttribution = "";
        private int tileMaxZoom = 18;
    }
}
