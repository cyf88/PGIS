package com.pgis.deviceinfo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapQueryCacheTest {

    @Test
    void quantizeAndKeyStableForNearbyProvinceViews() {
        assertEquals(114.5, MapQueryCache.quantize(114.52, 5), 0.0001);
        assertEquals(114.5, MapQueryCache.quantize(114.48, 5), 0.0001);
        assertEquals(
                MapQueryCache.key(113.45, 36.05, 119.87, 42.62, 5, "13"),
                MapQueryCache.key(113.47, 36.08, 119.89, 42.58, 5, "13"));
        assertEquals("13|region|5|113.5000|36.1000|119.9000|42.6000",
                MapQueryCache.key(113.45, 36.05, 119.87, 42.62, 5, "13"));
        assertEquals(
                MapQueryCache.key(113.45, 36.05, 119.87, 42.62, 8, "13", "nearby"),
                MapQueryCache.key(113.45, 36.05, 119.87, 42.62, 8, "13", "NEARBY"));
    }
}
