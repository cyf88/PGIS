package com.pgis.deviceinfo;

import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/** 搜索缩放用的省/市行政区范围（不采用设备点外包，避免 0,0 等脏点把视野拉到全球）。 */
final class AdminRegionBoxes {

    private static final Map<String, double[]> BOXES = new LinkedHashMap<>();

    static {
        // 河北省
        put("13", 113.45, 36.05, 119.87, 42.62);
        put("1301", 113.52, 37.42, 115.52, 38.78);
        put("1302", 117.52, 38.92, 119.18, 40.32);
        put("1303", 118.72, 39.42, 119.88, 40.62);
        put("1304", 113.38, 36.05, 115.48, 37.02);
        put("1305", 113.85, 36.78, 115.85, 37.85);
        put("1306", 113.82, 38.15, 116.82, 39.72);
        put("1307", 113.82, 39.55, 116.58, 42.12);
        put("1308", 115.88, 40.18, 119.15, 42.62);
        put("1309", 115.62, 37.48, 117.82, 38.92);
        put("1310", 116.05, 38.48, 117.22, 40.05);
        put("1311", 115.08, 37.12, 116.62, 38.32);
    }

    private AdminRegionBoxes() {
    }

    static RegionBounds of(String regionPrefix) {
        if (!StringUtils.hasText(regionPrefix)) {
            return null;
        }
        String digits = regionPrefix.trim().replaceAll("\\D", "");
        if (digits.length() >= 4) {
            RegionBounds city = copy(BOXES.get(digits.substring(0, 4)));
            if (city != null) {
                return city;
            }
        }
        if (digits.length() >= 2) {
            return copy(BOXES.get(digits.substring(0, 2)));
        }
        return null;
    }

    static boolean isValidWgs84(RegionBounds bounds) {
        if (bounds == null || bounds.getMinLng() == null || bounds.getMinLat() == null
                || bounds.getMaxLng() == null || bounds.getMaxLat() == null) {
            return false;
        }
        if (bounds.getMinLng() >= bounds.getMaxLng() || bounds.getMinLat() >= bounds.getMaxLat()) {
            return false;
        }
        return bounds.getMinLng() >= -180 && bounds.getMaxLng() <= 180
                && bounds.getMinLat() >= -90 && bounds.getMaxLat() <= 90;
    }

    private static void put(String code, double minLng, double minLat, double maxLng, double maxLat) {
        BOXES.put(code, new double[]{minLng, minLat, maxLng, maxLat});
    }

    private static RegionBounds copy(double[] box) {
        if (box == null) {
            return null;
        }
        RegionBounds bounds = new RegionBounds();
        bounds.setMinLng(box[0]);
        bounds.setMinLat(box[1]);
        bounds.setMaxLng(box[2]);
        bounds.setMaxLat(box[3]);
        return bounds;
    }
}
