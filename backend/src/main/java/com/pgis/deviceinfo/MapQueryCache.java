package com.pgis.deviceinfo;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Locale;
import java.util.function.Supplier;

/** 地图聚合结果内存缓存；导入或设备/定位变更时整表失效。 */
@Component
public class MapQueryCache {

    private final Cache<String, MapBoundsAggregate> aggregateCache;

    public MapQueryCache(
            @Value("${pgis.map-cache.ttl-minutes:5}") long ttlMinutes,
            @Value("${pgis.map-cache.max-size:2000}") long maxSize) {
        this.aggregateCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(Math.max(ttlMinutes, 1)))
                .maximumSize(Math.max(maxSize, 64))
                .build();
    }

    public MapBoundsAggregate getOrCompute(double minLng, double minLat, double maxLng, double maxLat,
                                           int zoom, String sjly, String clusterMode,
                                           Supplier<MapBoundsAggregate> loader) {
        return aggregateCache.get(key(minLng, minLat, maxLng, maxLat, zoom, sjly, clusterMode),
                ignored -> loader.get());
    }

    public void evictAll() {
        aggregateCache.invalidateAll();
    }

    static String key(double minLng, double minLat, double maxLng, double maxLat, int zoom, String sjly) {
        return key(minLng, minLat, maxLng, maxLat, zoom, sjly, "region");
    }

    static String key(double minLng, double minLat, double maxLng, double maxLat, int zoom, String sjly,
                      String clusterMode) {
        String source = StringUtils.hasText(sjly) ? sjly.trim() : "";
        return String.format(Locale.US, "%s|%s|%d|%.4f|%.4f|%.4f|%.4f",
                source, normalizeMode(clusterMode), zoom,
                quantize(minLng, zoom),
                quantize(minLat, zoom),
                quantize(maxLng, zoom),
                quantize(maxLat, zoom));
    }

    static String normalizeMode(String clusterMode) {
        return "nearby".equalsIgnoreCase(clusterMode) ? "nearby" : "region";
    }

    /** 低缩放粗取整，让拖动/抖动仍能命中；高缩放更精细。 */
    static double quantize(double value, int zoom) {
        int stepMilli = zoom <= 6 ? 100 : zoom <= 9 ? 50 : 10;
        long milli = Math.round(value * 1000.0);
        long quantized = Math.round(milli / (double) stepMilli) * stepMilli;
        return quantized / 1000.0;
    }
}
