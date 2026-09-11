package com.pgis.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class MapUiPropertiesTest {

    @Autowired
    private MapUiProperties properties;

    @Test
    void bindsDefaultMapSettings() {
        assertEquals(6, properties.getProvinceZoom());
        assertEquals(11, properties.getCityMaxZoom());
        assertEquals("gaode", properties.getDefaultBasemap());
        assertEquals(2, properties.getBasemaps().size());
        assertEquals("高德地图", properties.getBasemaps().get(0).getLabel());
        assertTrue(properties.getBasemaps().get(0).getTileUrl().contains("{z}"));
        assertEquals("intranet", properties.getBasemaps().get(1).getId());
        assertTrue(properties.getBasemaps().get(1).getTileUrl().contains("10.2.164.43"));
        assertEquals(113.45, properties.getFallbackMinLng(), 0.0001);
    }
}
