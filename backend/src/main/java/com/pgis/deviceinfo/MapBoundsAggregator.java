package com.pgis.deviceinfo;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 把最新定位列表聚成区划气泡、装备类型和撒点。 */
final class MapBoundsAggregator {

    private MapBoundsAggregator() {
    }

    static List<MapClusterPoint> regionClusters(List<LatestMapPoint> points, int prefixLen,
                                                AdminRegionNames names) {
        Map<String, double[]> acc = new LinkedHashMap<>();
        for (LatestMapPoint p : points) {
            if (p == null || p.getJd() == null || p.getWd() == null || !StringUtils.hasText(p.getSjly())) {
                continue;
            }
            String region = prefixOf(p.getSjly(), prefixLen);
            double[] row = acc.computeIfAbsent(region, key -> new double[3]);
            row[0] += p.getJd();
            row[1] += p.getWd();
            row[2] += 1;
        }
        String expectedLevel = AdminRegionNames.expectedLevel(prefixLen);
        List<MapClusterPoint> clusters = new ArrayList<>();
        acc.forEach((region, row) -> {
            String level = names.levelOf(region);
            if (!expectedLevel.equals(level)) {
                return;
            }
            MapClusterPoint point = new MapClusterPoint();
            point.setSbbh(region);
            point.setLng(row[0] / row[2]);
            point.setLat(row[1] / row[2]);
            point.setCount((long) row[2]);
            point.setRegionName(names.nameOf(region));
            point.setRegionLevel(level);
            clusters.add(point);
        });
        clusters.sort(Comparator.comparing(MapClusterPoint::getCount, Comparator.nullsLast(Long::compareTo)).reversed());
        if (clusters.size() > 200) {
            return new ArrayList<>(clusters.subList(0, 200));
        }
        return clusters;
    }

    /**
     * 按缩放对应的屏幕距离把邻近点收成一簇，气泡在点的均值位置。
     * 格网边长约 96 像素换算成经纬度，避免对上万点做两两距离计算。
     */
    static List<MapClusterPoint> nearbyClusters(List<LatestMapPoint> points, int zoom) {
        double cell = nearbyCellDegrees(zoom);
        Map<String, double[]> acc = new LinkedHashMap<>();
        for (LatestMapPoint p : points) {
            if (p == null || p.getJd() == null || p.getWd() == null) {
                continue;
            }
            int gx = (int) Math.floor((p.getJd() + 180.0) / cell);
            int gy = (int) Math.floor((p.getWd() + 90.0) / cell);
            String key = gx + ":" + gy;
            double[] row = acc.computeIfAbsent(key, ignored -> new double[3]);
            row[0] += p.getJd();
            row[1] += p.getWd();
            row[2] += 1;
        }
        List<MapClusterPoint> clusters = new ArrayList<>();
        acc.forEach((key, row) -> {
            MapClusterPoint point = new MapClusterPoint();
            point.setSbbh(key);
            point.setLng(row[0] / row[2]);
            point.setLat(row[1] / row[2]);
            point.setCount((long) row[2]);
            point.setRegionLevel("nearby");
            clusters.add(point);
        });
        clusters.sort(Comparator.comparing(MapClusterPoint::getCount, Comparator.nullsLast(Long::compareTo)).reversed());
        if (clusters.size() > 400) {
            return new ArrayList<>(clusters.subList(0, 400));
        }
        return clusters;
    }

    static double nearbyCellDegrees(int zoom) {
        int z = Math.max(1, Math.min(18, zoom));
        return 96.0 * 360.0 / (256.0 * Math.pow(2, z));
    }

    static List<EquipmentTypeStat> typeCounts(List<LatestMapPoint> points) {
        List<EquipmentTypeStat> raw = new ArrayList<>();
        Map<String, Long> counts = new LinkedHashMap<>();
        for (LatestMapPoint p : points) {
            if (p == null) {
                continue;
            }
            String code = EquipmentTypes.normalize(p.getZblx());
            counts.merge(code, 1L, Long::sum);
        }
        counts.forEach((code, n) -> raw.add(new EquipmentTypeStat(code, null, n)));
        return EquipmentTypes.merge(raw);
    }

    static List<MapClusterPoint> samplePoints(List<LatestMapPoint> points, int limit) {
        List<LatestMapPoint> copy = new ArrayList<>();
        for (LatestMapPoint p : points) {
            if (p != null && p.getJd() != null && p.getWd() != null) {
                copy.add(p);
            }
        }
        copy.sort(Comparator.comparing(p -> p.getSbbh() == null ? "" : p.getSbbh()));
        int n = Math.min(limit, copy.size());
        List<MapClusterPoint> result = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            LatestMapPoint p = copy.get(i);
            MapClusterPoint point = new MapClusterPoint();
            point.setLng(p.getJd());
            point.setLat(p.getWd());
            point.setCount(1L);
            point.setSbbh(p.getSbbh());
            point.setJyxm(p.getJyxm());
            result.add(point);
        }
        return result;
    }

    static String prefixOf(String sjly, int prefixLen) {
        String digits = sjly.trim();
        return digits.length() <= prefixLen ? digits : digits.substring(0, prefixLen);
    }
}
