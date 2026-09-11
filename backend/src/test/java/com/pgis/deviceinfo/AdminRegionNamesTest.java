package com.pgis.deviceinfo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminRegionNamesTest {

    private final AdminRegionNames names = new AdminRegionNames();

    @Test
    void resolvesHebeiProvinceCityCountyPrefixes() {
        assertEquals("河北省", names.nameOf("13"));
        assertEquals("石家庄市", names.nameOf("1301"));
        assertEquals("长安区", names.nameOf("130102"));
        assertEquals("围场满族蒙古族自治县", names.nameOf("130828"));
        assertEquals("河北省", names.provinceTitle("13"));
        assertEquals("河北省", names.provinceTitle("130102"));
        assertEquals("41", names.provinceTitle("419901"));
        assertEquals("全部", names.provinceTitle(null));
        assertEquals("province", names.levelOf("13"));
        assertEquals("city", names.levelOf("1301"));
        assertEquals("county", names.levelOf("130102"));
        assertEquals("province", AdminRegionNames.inferLevel("410000"));
        assertEquals("city", AdminRegionNames.inferLevel("419900"));
        assertEquals("county", AdminRegionNames.inferLevel("419901"));
    }

    @Test
    void provincesDropdownIncludesHebeiCities() {
        List<AdminRegionOption> provinces = names.provinces();
        assertEquals(1, provinces.size());
        assertEquals("13", provinces.get(0).getCode());
        assertEquals("河北省", provinces.get(0).getName());
        List<String> cities = provinces.get(0).getCities().stream()
                .map(AdminRegionOption::getName)
                .toList();
        assertTrue(cities.contains("石家庄市"));
        assertTrue(cities.contains("唐山市"));
        assertEquals(11, cities.size());
        RegionBounds hebei = AdminRegionBoxes.of("13");
        assertEquals(113.45, hebei.getMinLng(), 0.0001);
        assertEquals(42.62, hebei.getMaxLat(), 0.0001);
        assertEquals(113.52, AdminRegionBoxes.of("1301").getMinLng(), 0.0001);
        assertTrue(AdminRegionBoxes.isValidWgs84(hebei));
    }

    @Test
    void unknownCodeReturnsNull() {
        assertNull(names.nameOf("41"));
        assertNull(names.nameOf("419901"));
        assertNull(names.nameOf(null));
        assertNull(names.nameOf(""));
    }
}
