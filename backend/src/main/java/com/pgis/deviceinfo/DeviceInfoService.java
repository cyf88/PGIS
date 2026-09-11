package com.pgis.deviceinfo;

import com.pgis.common.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class DeviceInfoService {

    private final DeviceInfoMapper mapper;
    private final AdminRegionNames adminRegionNames;
    private final MapQueryCache mapQueryCache;
    private final MapLatestLocationStore latestLocationStore;

    public DeviceInfoService(DeviceInfoMapper mapper, AdminRegionNames adminRegionNames,
                             MapQueryCache mapQueryCache, MapLatestLocationStore latestLocationStore) {
        this.mapper = mapper;
        this.adminRegionNames = adminRegionNames;
        this.mapQueryCache = mapQueryCache;
        this.latestLocationStore = latestLocationStore;
    }

    public PageResult<DeviceInfo> page(String sbbh, String jyxm, String ssgajgjgdm, String sjly, int page, int size) {
        int p = Math.max(page, 1);
        int s = size <= 0 ? 20 : size;
        long total = mapper.countPage(sbbh, jyxm, ssgajgjgdm, sjly);
        List<DeviceInfo> records = mapper.selectPage(sbbh, jyxm, ssgajgjgdm, sjly, (p - 1) * s, s);
        return new PageResult<>(total, p, s, records);
    }

    public DeviceInfo get(String xxzjbh) {
        return mapper.selectById(xxzjbh);
    }

    public DeviceInfo create(DeviceInfo entity) {
        if (!StringUtils.hasText(entity.getXxzjbh())) {
            entity.setXxzjbh(UUID.randomUUID().toString().replace("-", ""));
        }
        mapper.insert(entity);
        mapQueryCache.evictAll();
        return entity;
    }

    public void update(String xxzjbh, DeviceInfo entity) {
        entity.setXxzjbh(xxzjbh);
        mapper.update(entity);
        mapQueryCache.evictAll();
    }

    public void delete(String xxzjbh) {
        mapper.deleteById(xxzjbh);
        mapQueryCache.evictAll();
    }

    public PageResult<DeviceInfoWithLocation> searchBySjly(String sjly, int page, int size) {
        if (!StringUtils.hasText(sjly)) {
            throw new IllegalArgumentException("数据来源 sjly 不能为空");
        }
        int p = Math.max(page, 1);
        int s = size <= 0 ? 20 : Math.min(size, 100);
        long total = mapper.countBySjly(sjly.trim());
        List<DeviceInfoWithLocation> records =
                mapper.selectBySjlyWithLatestLocation(sjly.trim(), (p - 1) * s, s);
        return new PageResult<>(total, p, s, records);
    }

    public long countBySjly(String sjly) {
        if (!StringUtils.hasText(sjly)) {
            throw new IllegalArgumentException("数据来源 sjly 不能为空");
        }
        return mapper.countBySjly(sjly.trim());
    }

    public MapBoundsAggregate aggregateInBounds(double minLng, double minLat, double maxLng, double maxLat,
                                                Integer zoom, String sjly) {
        return aggregateInBounds(minLng, minLat, maxLng, maxLat, zoom, sjly, null);
    }

    public MapBoundsAggregate aggregateInBounds(double minLng, double minLat, double maxLng, double maxLat,
                                                Integer zoom, String sjly, String clusterMode) {
        if (minLng >= maxLng || minLat >= maxLat) {
            throw new IllegalArgumentException("地图范围无效：需满足 minLng < maxLng 且 minLat < maxLat");
        }
        if (minLng < -180 || maxLng > 180 || minLat < -90 || maxLat > 90) {
            throw new IllegalArgumentException("经纬度超出有效范围");
        }
        int z = zoom == null ? 10 : zoom;
        String source = StringUtils.hasText(sjly) ? sjly.trim() : null;
        String mode = MapQueryCache.normalizeMode(clusterMode);
        return mapQueryCache.getOrCompute(minLng, minLat, maxLng, maxLat, z, source, mode,
                () -> loadAggregate(minLng, minLat, maxLng, maxLat, z, source, mode));
    }

    MapBoundsAggregate loadAggregate(double minLng, double minLat, double maxLng, double maxLat,
                                     int z, String source, String clusterMode) {
        List<LatestMapPoint> points = mapper.selectLatestDevicesInBounds(
                minLng, minLat, maxLng, maxLat, source, latestLocationStore.useTable());
        MapBoundsAggregate result = new MapBoundsAggregate();
        if (z >= 14) {
            result.setMode("point");
            result.setCellSize(null);
            result.setTotal(points.size());
            result.setClusters(MapBoundsAggregator.samplePoints(points, 500));
        } else if ("nearby".equals(clusterMode)) {
            List<MapClusterPoint> clusters = MapBoundsAggregator.nearbyClusters(points, z);
            result.setMode("nearby");
            result.setCellSize(MapBoundsAggregator.nearbyCellDegrees(z));
            result.setClusters(clusters);
            result.setTotal(points.size());
        } else {
            result.setMode("region");
            result.setCellSize(null);
            List<MapClusterPoint> clusters = MapBoundsAggregator.regionClusters(
                    points, regionPrefixLen(z), adminRegionNames);
            result.setClusters(clusters);
            result.setTotal(clusters.stream().mapToLong(p -> p.getCount() == null ? 0L : p.getCount()).sum());
        }
        result.setTypeStats(MapBoundsAggregator.typeCounts(points));
        result.setProvinceName(resolveProvinceName(source, z, result.getMode(), result.getClusters()));
        return result;
    }

    String resolveProvinceName(String sjly, int zoom, List<MapClusterPoint> clusters) {
        return resolveProvinceName(sjly, zoom, "region", clusters);
    }

    String resolveProvinceName(String sjly, int zoom, String mode, List<MapClusterPoint> clusters) {
        if (StringUtils.hasText(sjly)) {
            return adminRegionNames.provinceTitle(sjly);
        }
        if ("region".equals(mode) && zoom < 14 && clusters != null && !clusters.isEmpty()) {
            Set<String> prefixes = new HashSet<>();
            for (MapClusterPoint p : clusters) {
                if (p != null && StringUtils.hasText(p.getSbbh()) && p.getSbbh().length() >= 2) {
                    prefixes.add(p.getSbbh().substring(0, 2));
                }
            }
            if (prefixes.size() == 1) {
                return adminRegionNames.provinceTitle(prefixes.iterator().next());
            }
        }
        return "全部";
    }

    public List<AdminRegionOption> adminRegions() {
        return adminRegionNames.provinces();
    }

    public RegionBounds regionBounds(String sjly) {
        if (!StringUtils.hasText(sjly)) {
            throw new IllegalArgumentException("请选择省或市");
        }
        String prefix = sjly.trim();
        RegionBounds admin = AdminRegionBoxes.of(prefix);
        if (admin != null) {
            return admin;
        }
        RegionBounds bounds = mapper.selectRegionBounds(prefix);
        if (!AdminRegionBoxes.isValidWgs84(bounds)) {
            return null;
        }
        expandIfPoint(bounds);
        if (!AdminRegionBoxes.isValidWgs84(bounds)) {
            return null;
        }
        return bounds;
    }

    static void expandIfPoint(RegionBounds bounds) {
        double pad = 0.12;
        if (Double.compare(bounds.getMinLng(), bounds.getMaxLng()) == 0) {
            bounds.setMinLng(Math.max(-180, bounds.getMinLng() - pad));
            bounds.setMaxLng(Math.min(180, bounds.getMaxLng() + pad));
        }
        if (Double.compare(bounds.getMinLat(), bounds.getMaxLat()) == 0) {
            bounds.setMinLat(Math.max(-90, bounds.getMinLat() - pad));
            bounds.setMaxLat(Math.min(90, bounds.getMaxLat() + pad));
        }
    }

    /** 小缩放按省/市/区（sjly 前 2/4/6 位）聚合，避免网格圆重叠。 */
    static int regionPrefixLen(int zoom) {
        if (zoom <= 6) {
            return 2;
        }
        if (zoom <= 9) {
            return 4;
        }
        return 6;
    }
}
