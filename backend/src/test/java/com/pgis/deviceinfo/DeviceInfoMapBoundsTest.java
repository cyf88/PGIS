package com.pgis.deviceinfo;

import com.pgis.devicelocation.DeviceLocation;
import com.pgis.devicelocation.DeviceLocationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class DeviceInfoMapBoundsTest {

    @Autowired
    private DeviceInfoService deviceInfoService;

    @Autowired
    private DeviceInfoMapper deviceInfoMapper;

    @Autowired
    private DeviceLocationMapper deviceLocationMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MapQueryCache mapQueryCache;

    @BeforeEach
    void evictMapCache() {
        mapQueryCache.evictAll();
    }

    @Test
    void aggregateInBounds_clustersLatestPointInsideBox() {
        jdbcTemplate.update("DELETE FROM jysb_sbxx_002 WHERE sbbh IN ('DEV-MAP-A','DEV-MAP-B','DEV-MAP-C','DEV-MAP-Z')");
        jdbcTemplate.update("DELETE FROM jysb_sbxx_001 WHERE xxzjbh IN ('map-a','map-b','map-c','map-zero')");

        deviceInfoMapper.insert(info("map-a", "DEV-MAP-A", "419901", "01"));
        deviceInfoMapper.insert(info("map-b", "DEV-MAP-B", "419901", "08"));
        deviceInfoMapper.insert(info("map-c", "DEV-MAP-C", "419902", "01"));

        deviceLocationMapper.insert(loc("m-old", "DEV-MAP-A", "113.10", "34.10", "20260101080000"));
        deviceLocationMapper.insert(loc("m-new", "DEV-MAP-A", "113.60", "34.70", "20260103180000"));
        deviceLocationMapper.insert(loc("m-b", "DEV-MAP-B", "113.61", "34.71", "20260102000000"));
        deviceLocationMapper.insert(loc("m-out", "DEV-MAP-C", "120.00", "30.00", "20260104000000"));

        MapBoundsAggregate district = deviceInfoService.aggregateInBounds(113.5, 34.6, 113.8, 34.9, 10, null);
        assertEquals("region", district.getMode());
        assertEquals(2, district.getTotal());
        assertEquals(1, district.getClusters().size());
        assertEquals("419901", district.getClusters().get(0).getSbbh());
        assertEquals(2, district.getClusters().get(0).getCount());

        MapBoundsAggregate city = deviceInfoService.aggregateInBounds(113.5, 34.6, 113.8, 34.9, 8, null);
        assertEquals("region", city.getMode());
        assertEquals(1, city.getClusters().size());
        assertEquals("4199", city.getClusters().get(0).getSbbh());
        assertEquals(2, city.getClusters().get(0).getCount());

        MapBoundsAggregate province = deviceInfoService.aggregateInBounds(113.5, 34.6, 113.8, 34.9, 6, null);
        assertEquals("region", province.getMode());
        assertEquals(1, province.getClusters().size());
        assertEquals("41", province.getClusters().get(0).getSbbh());
        assertEquals(2, province.getClusters().get(0).getCount());
        assertEquals(null, province.getClusters().get(0).getRegionName());

        MapBoundsAggregate points = deviceInfoService.aggregateInBounds(113.5, 34.6, 113.8, 34.9, 14, "419901");
        assertEquals("point", points.getMode());
        assertEquals(2, points.getTotal());

        MapBoundsAggregate prefix = deviceInfoService.aggregateInBounds(113.5, 34.6, 113.8, 34.9, 14, "4199");
        assertEquals(2, prefix.getTotal());
        assertEquals(2, prefix.getClusters().size());
        assertEquals(12, prefix.getTypeStats().size());
        assertEquals(1L, typeCount(prefix, "01"));
        assertEquals(1L, typeCount(prefix, "08"));
        assertEquals(0L, typeCount(prefix, "06"));

        RegionBounds bounds = deviceInfoService.regionBounds("419901");
        assertEquals(113.60, bounds.getMinLng(), 0.0001);
        assertEquals(34.70, bounds.getMinLat(), 0.0001);
        assertEquals(113.61, bounds.getMaxLng(), 0.0001);
        assertEquals(34.71, bounds.getMaxLat(), 0.0001);

        deviceInfoMapper.insert(info("map-zero", "DEV-MAP-Z", "419901"));
        deviceLocationMapper.insert(loc("m-zero", "DEV-MAP-Z", "0", "0", "20260105000000"));
        RegionBounds withoutZero = deviceInfoService.regionBounds("419901");
        assertEquals(113.60, withoutZero.getMinLng(), 0.0001);
        assertEquals(34.70, withoutZero.getMinLat(), 0.0001);

        jdbcTemplate.update("DELETE FROM jysb_sbxx_002 WHERE sbbh = 'DEV-MAP-Z'");
        jdbcTemplate.update("DELETE FROM jysb_sbxx_001 WHERE xxzjbh = 'map-zero'");

        assertEquals(2, DeviceInfoService.regionPrefixLen(6));
        assertEquals(4, DeviceInfoService.regionPrefixLen(9));
        assertEquals(6, DeviceInfoService.regionPrefixLen(10));
    }

    @Test
    void aggregateInBounds_fillsHebeiRegionName() {
        jdbcTemplate.update("DELETE FROM jysb_sbxx_002 WHERE sbbh = 'DEV-MAP-HB'");
        jdbcTemplate.update("DELETE FROM jysb_sbxx_001 WHERE xxzjbh = 'map-hb'");

        deviceInfoMapper.insert(info("map-hb", "DEV-MAP-HB", "130102"));
        deviceLocationMapper.insert(loc("m-hb", "DEV-MAP-HB", "114.52", "38.05", "20260105000000"));

        MapBoundsAggregate county = deviceInfoService.aggregateInBounds(114.4, 37.9, 114.7, 38.2, 12, null);
        assertEquals("130102", county.getClusters().get(0).getSbbh());
        assertEquals("长安区", county.getClusters().get(0).getRegionName());
        assertEquals("county", county.getClusters().get(0).getRegionLevel());

        MapBoundsAggregate city = deviceInfoService.aggregateInBounds(114.4, 37.9, 114.7, 38.2, 8, null);
        assertEquals("1301", city.getClusters().get(0).getSbbh());
        assertEquals("石家庄市", city.getClusters().get(0).getRegionName());
        assertEquals("city", city.getClusters().get(0).getRegionLevel());
        assertEquals("河北省", city.getProvinceName());

        MapBoundsAggregate filtered = deviceInfoService.aggregateInBounds(114.4, 37.9, 114.7, 38.2, 8, "13");
        assertEquals("河北省", filtered.getProvinceName());

        MapBoundsAggregate province = deviceInfoService.aggregateInBounds(114.4, 37.9, 114.7, 38.2, 6, null);
        assertEquals("13", province.getClusters().get(0).getSbbh());
        assertEquals("河北省", province.getClusters().get(0).getRegionName());
        assertEquals("province", province.getClusters().get(0).getRegionLevel());
    }

    @Test
    void aggregateInBounds_hidesProvinceBubbleAtCityZoom() {
        jdbcTemplate.update("DELETE FROM jysb_sbxx_002 WHERE sbbh IN ('DEV-MAP-P','DEV-MAP-CT')");
        jdbcTemplate.update("DELETE FROM jysb_sbxx_001 WHERE xxzjbh IN ('map-p','map-ct')");

        deviceInfoMapper.insert(info("map-p", "DEV-MAP-P", "130000"));
        deviceInfoMapper.insert(info("map-ct", "DEV-MAP-CT", "130102"));
        deviceLocationMapper.insert(loc("m-p", "DEV-MAP-P", "114.52", "38.05", "20260105000000"));
        deviceLocationMapper.insert(loc("m-ct", "DEV-MAP-CT", "114.53", "38.06", "20260105000000"));

        MapBoundsAggregate city = deviceInfoService.aggregateInBounds(114.4, 37.9, 114.7, 38.2, 8, null);
        assertEquals(1, city.getClusters().size());
        assertEquals("石家庄市", city.getClusters().get(0).getRegionName());
        assertEquals("city", city.getClusters().get(0).getRegionLevel());

        MapBoundsAggregate county = deviceInfoService.aggregateInBounds(114.4, 37.9, 114.7, 38.2, 12, null);
        assertEquals(1, county.getClusters().size());
        assertEquals("长安区", county.getClusters().get(0).getRegionName());
        assertEquals("county", county.getClusters().get(0).getRegionLevel());
    }

    @Test
    void aggregateInBounds_rejectsInvalidBox() {
        assertThrows(IllegalArgumentException.class,
                () -> deviceInfoService.aggregateInBounds(114, 34, 113, 35, 10, null));
        assertThrows(IllegalArgumentException.class, () -> deviceInfoService.regionBounds(""));
        assertThrows(IllegalArgumentException.class, () -> deviceInfoService.regionBounds(null));
    }

    @Test
    void regionBounds_usesHebeiAdminBoxIgnoringZeroPoints() {
        jdbcTemplate.update("DELETE FROM jysb_sbxx_002 WHERE sbbh IN ('DEV-HB-ZERO','DEV-HB-OK')");
        jdbcTemplate.update("DELETE FROM jysb_sbxx_001 WHERE xxzjbh IN ('hb-zero','hb-ok')");
        deviceInfoMapper.insert(info("hb-zero", "DEV-HB-ZERO", "130102"));
        deviceInfoMapper.insert(info("hb-ok", "DEV-HB-OK", "130102"));
        deviceLocationMapper.insert(loc("hb-z", "DEV-HB-ZERO", "0", "0", "20260106000000"));
        deviceLocationMapper.insert(loc("hb-o", "DEV-HB-OK", "114.52", "38.05", "20260106000000"));

        RegionBounds hebei = deviceInfoService.regionBounds("13");
        assertEquals(113.45, hebei.getMinLng(), 0.0001);
        assertEquals(36.05, hebei.getMinLat(), 0.0001);
        assertEquals(119.87, hebei.getMaxLng(), 0.0001);
        assertEquals(42.62, hebei.getMaxLat(), 0.0001);

        RegionBounds sjz = deviceInfoService.regionBounds("1301");
        assertEquals(113.52, sjz.getMinLng(), 0.0001);
        assertEquals(38.78, sjz.getMaxLat(), 0.0001);
    }

    @Test
    void aggregateInBounds_returnsCachedResultUntilEvicted() {
        jdbcTemplate.update("DELETE FROM jysb_sbxx_002 WHERE sbbh IN ('DEV-CACHE-A','DEV-CACHE-B')");
        jdbcTemplate.update("DELETE FROM jysb_sbxx_001 WHERE xxzjbh IN ('cache-a','cache-b')");

        deviceInfoMapper.insert(info("cache-a", "DEV-CACHE-A", "130102"));
        deviceLocationMapper.insert(loc("c-a", "DEV-CACHE-A", "114.52", "38.05", "20260107000000"));

        MapBoundsAggregate first = deviceInfoService.aggregateInBounds(114.4, 37.9, 114.7, 38.2, 8, "13");
        assertEquals(1, first.getTotal());
        MapBoundsAggregate cached = deviceInfoService.aggregateInBounds(114.41, 37.91, 114.71, 38.21, 8, "13");
        assertSame(first, cached);

        deviceInfoMapper.insert(info("cache-b", "DEV-CACHE-B", "130102"));
        deviceLocationMapper.insert(loc("c-b", "DEV-CACHE-B", "114.53", "38.06", "20260107000000"));
        assertEquals(1, deviceInfoService.aggregateInBounds(114.4, 37.9, 114.7, 38.2, 8, "13").getTotal());

        mapQueryCache.evictAll();
        assertEquals(2, deviceInfoService.aggregateInBounds(114.4, 37.9, 114.7, 38.2, 8, "13").getTotal());
    }

    @Test
    void aggregateInBounds_nearbyMergesClosePointsIndependentOfRegion() {
        jdbcTemplate.update("DELETE FROM jysb_sbxx_002 WHERE sbbh IN ('DEV-NB-A','DEV-NB-B','DEV-NB-C')");
        jdbcTemplate.update("DELETE FROM jysb_sbxx_001 WHERE xxzjbh IN ('nb-a','nb-b','nb-c')");

        deviceInfoMapper.insert(info("nb-a", "DEV-NB-A", "880102", "01"));
        deviceInfoMapper.insert(info("nb-b", "DEV-NB-B", "880102", "08"));
        deviceInfoMapper.insert(info("nb-c", "DEV-NB-C", "880200", "01"));
        deviceLocationMapper.insert(loc("nb-la", "DEV-NB-A", "114.50", "38.00", "20260108000000"));
        deviceLocationMapper.insert(loc("nb-lb", "DEV-NB-B", "114.52", "38.01", "20260108000000"));
        deviceLocationMapper.insert(loc("nb-lc", "DEV-NB-C", "118.00", "40.00", "20260108000000"));

        MapBoundsAggregate region = deviceInfoService.aggregateInBounds(
                113.0, 36.0, 119.5, 42.0, 6, "88", "region");
        assertEquals("region", region.getMode());
        assertEquals(1, region.getClusters().size());
        assertEquals("88", region.getClusters().get(0).getSbbh());
        assertEquals(3, region.getClusters().get(0).getCount());

        MapBoundsAggregate nearby = deviceInfoService.aggregateInBounds(
                113.0, 36.0, 119.5, 42.0, 6, "88", "nearby");
        assertEquals("nearby", nearby.getMode());
        assertEquals(3, nearby.getTotal());
        assertEquals(2, nearby.getClusters().size());
        assertEquals(2, nearby.getClusters().get(0).getCount());
        assertEquals(1, nearby.getClusters().get(1).getCount());
        assertEquals("nearby", nearby.getClusters().get(0).getRegionLevel());
        assertEquals(null, nearby.getClusters().get(0).getRegionName());

        MapBoundsAggregate points = deviceInfoService.aggregateInBounds(
                113.0, 36.0, 119.5, 42.0, 14, "88", "nearby");
        assertEquals("point", points.getMode());
        assertEquals(3, points.getTotal());
    }

    private static long typeCount(MapBoundsAggregate agg, String zblx) {
        return agg.getTypeStats().stream()
                .filter(s -> zblx.equals(s.getZblx()))
                .mapToLong(s -> s.getCount() == null ? 0L : s.getCount())
                .findFirst()
                .orElse(0L);
    }

    private static DeviceInfo info(String pk, String sbbh, String sjly) {
        return info(pk, sbbh, sjly, null);
    }

    private static DeviceInfo info(String pk, String sbbh, String sjly, String zblx) {
        DeviceInfo e = new DeviceInfo();
        e.setXxzjbh(pk);
        e.setSbbh(sbbh);
        e.setSjly(sjly);
        e.setZblx(zblx);
        e.setLrsj("20260101000000");
        return e;
    }

    private static DeviceLocation loc(String pk, String sbbh, String jd, String wd, String end) {
        DeviceLocation e = new DeviceLocation();
        e.setXxzjbh(pk);
        e.setSbbh(sbbh);
        e.setJd(jd);
        e.setWd(wd);
        e.setSbsyjssj(end);
        e.setLrsj(end);
        return e;
    }
}
