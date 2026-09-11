package com.pgis.deviceinfo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapBoundsAggregatorTest {

    private final AdminRegionNames names = new AdminRegionNames();

    @Test
    void regionClustersKeepsCityAndDropsProvincePrefix() {
        LatestMapPoint city = point("A", 114.5, 38.0, "130102", "01");
        LatestMapPoint alsoCity = point("B", 114.7, 38.2, "130102", "08");
        LatestMapPoint provinceOnly = point("C", 114.6, 38.1, "130000", "01");

        List<MapClusterPoint> clusters = MapBoundsAggregator.regionClusters(
                List.of(city, alsoCity, provinceOnly), 4, names);

        assertEquals(1, clusters.size());
        assertEquals("1301", clusters.get(0).getSbbh());
        assertEquals("石家庄市", clusters.get(0).getRegionName());
        assertEquals("city", clusters.get(0).getRegionLevel());
        assertEquals(2, clusters.get(0).getCount());
        assertEquals(114.6, clusters.get(0).getLng(), 0.0001);
        assertEquals(38.1, clusters.get(0).getLat(), 0.0001);
    }

    @Test
    void typeCountsNormalizeAndFillCodeTable() {
        List<EquipmentTypeStat> stats = MapBoundsAggregator.typeCounts(List.of(
                point("A", 114.5, 38.0, "130102", "1"),
                point("B", 114.5, 38.0, "130102", "08")));
        assertEquals(12, stats.size());
        assertEquals(1L, stats.stream().filter(s -> "01".equals(s.getZblx())).findFirst().orElseThrow().getCount());
        assertEquals(1L, stats.stream().filter(s -> "08".equals(s.getZblx())).findFirst().orElseThrow().getCount());
        assertEquals(0L, stats.stream().filter(s -> "06".equals(s.getZblx())).findFirst().orElseThrow().getCount());
    }

    @Test
    void nearbyClustersMergesClosePointsAndKeepsFarOnes() {
        LatestMapPoint a = point("A", 114.50, 38.00, "130102", "01");
        LatestMapPoint b = point("B", 114.52, 38.01, "130102", "08");
        LatestMapPoint far = point("C", 118.00, 40.00, "130200", "01");

        List<MapClusterPoint> clusters = MapBoundsAggregator.nearbyClusters(List.of(a, b, far), 8);
        assertEquals(2, clusters.size());
        assertEquals(2, clusters.get(0).getCount());
        assertEquals(1, clusters.get(1).getCount());
        assertEquals("nearby", clusters.get(0).getRegionLevel());
        assertEquals(114.51, clusters.get(0).getLng(), 0.01);
        assertEquals(96.0 * 360.0 / (256.0 * 256.0), MapBoundsAggregator.nearbyCellDegrees(8), 0.0001);
    }

    private static LatestMapPoint point(String sbbh, double jd, double wd, String sjly, String zblx) {
        LatestMapPoint p = new LatestMapPoint();
        p.setSbbh(sbbh);
        p.setJd(jd);
        p.setWd(wd);
        p.setSjly(sjly);
        p.setZblx(zblx);
        return p;
    }
}
