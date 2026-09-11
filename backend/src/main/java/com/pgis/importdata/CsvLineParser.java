package com.pgis.importdata;

import com.pgis.deviceinfo.DeviceInfo;
import com.pgis.devicelocation.DeviceLocation;

import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

/**
 * 按带头 CSV 实际列序解析（与 JPG 规范字段集合一致，但 001 中 jysfz 在 xxzjbh 之后）。
 * <pre>
 * sb001: 6信封 + sbbh..xgrsfzh + xxzjbh + jysfz + [pt]
 * sb002: 6信封 + sbbh..xgrsfzh + xxzjbh + [pt]
 * </pre>
 */
public final class CsvLineParser {

    public static final int ENVELOPE_COLUMNS = 6;
    /** 001 业务列：含 jysfz，不含信封与 pt */
    public static final int DEVICE_INFO_FIELDS = 26;
    public static final int DEVICE_LOCATION_FIELDS = 23;

    private CsvLineParser() {
    }

    public static DeviceInfo parseDeviceInfo(String[] cols) {
        // 最少到 jysfz：6 + 26 = 32 列（下标 0..31），末列 pt 可有可无
        requireColumns(cols, ENVELOPE_COLUMNS + DEVICE_INFO_FIELDS);
        int i = ENVELOPE_COLUMNS;
        DeviceInfo e = new DeviceInfo();
        e.setSbbh(clip(cols[i++], 20));
        e.setSbpp(clip(cols[i++], 20));
        e.setSbxh(clip(cols[i++], 20));
        e.setZblx(num(cols[i++], 2));
        e.setSsgajgjgdm(clip(cols[i++], 20));
        e.setCphm(clip(cols[i++], 20));
        e.setJybh(clip(cols[i++], 20));
        e.setJyxm(clip(cols[i++], 20));
        // CSV 顺序：jyxm 后是 hh，不是 jysfz
        e.setHh(clip(cols[i++], 50));
        e.setLxdh(clip(cols[i++], 11));
        e.setSbsyzt(num(cols[i++], 2));
        e.setBz(clip(cols[i++], 100));
        e.setSjczlxdm(clip(cols[i++], 1));
        e.setSjly(clip(cols[i++], 6));
        e.setLrsj(clip(cols[i++], 14));
        e.setLrdwdm(clip(cols[i++], 12));
        e.setLrdwmc(clip(cols[i++], 100));
        e.setLrrxm(clip(cols[i++], 50));
        e.setLrrsfzh(clip(cols[i++], 18));
        e.setXgsj(clip(cols[i++], 14));
        e.setXgdwdm(clip(cols[i++], 12));
        e.setXgdwmc(clip(cols[i++], 100));
        e.setXgrxm(clip(cols[i++], 50));
        e.setXgrsfzh(clip(cols[i++], 18));
        e.setXxzjbh(clip(cols[i++], 32));
        e.setJysfz(clip(cols[i], 20));
        ensurePrimaryKey(e::getXxzjbh, e::setXxzjbh);
        return e;
    }

    public static DeviceLocation parseDeviceLocation(String[] cols) {
        requireColumns(cols, ENVELOPE_COLUMNS + DEVICE_LOCATION_FIELDS);
        int i = ENVELOPE_COLUMNS;
        DeviceLocation e = new DeviceLocation();
        e.setSbbh(clip(cols[i++], 20));
        e.setJd(num(cols[i++], 20));
        e.setWd(num(cols[i++], 20));
        e.setSpsd(num(cols[i++], 8));
        e.setCzsd(num(cols[i++], 8));
        e.setHjj(num(cols[i++], 3));
        e.setGc(num(cols[i++], 8));
        e.setJd01(num(cols[i++], 2));
        e.setSbsykssj(clip(cols[i++], 14));
        e.setSbsyjssj(clip(cols[i++], 14));
        e.setSjczlxdm(clip(cols[i++], 1));
        e.setSjly(clip(cols[i++], 6));
        e.setLrsj(clip(cols[i++], 14));
        e.setLrdwdm(clip(cols[i++], 12));
        e.setLrdwmc(clip(cols[i++], 100));
        e.setLrrxm(clip(cols[i++], 50));
        e.setLrrsfzh(clip(cols[i++], 18));
        e.setXgsj(clip(cols[i++], 14));
        e.setXgdwdm(clip(cols[i++], 12));
        e.setXgdwmc(clip(cols[i++], 100));
        e.setXgrxm(clip(cols[i++], 50));
        e.setXgrsfzh(clip(cols[i++], 18));
        e.setXxzjbh(clip(cols[i], 32));
        ensurePrimaryKey(e::getXxzjbh, e::setXxzjbh);
        return e;
    }

    /** 跳过带头 CSV 的表头行 */
    public static boolean isHeaderRow(String[] cols) {
        if (cols == null || cols.length == 0) {
            return false;
        }
        String first = cols[0] == null ? "" : cols[0].trim().toLowerCase(Locale.ROOT);
        return "sjhj_appid".equals(first) || "sbbh".equals(first);
    }

    public static boolean matchesTableFile(String filename, String prefix) {
        if (filename == null || prefix == null) {
            return false;
        }
        String name = Paths.get(filename).getFileName().toString().toLowerCase(Locale.ROOT);
        String p = prefix.toLowerCase(Locale.ROOT);
        return name.endsWith(".csv") && name.startsWith(p);
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static void requireColumns(String[] cols, int min) {
        if (cols == null || cols.length < min) {
            throw new IllegalArgumentException("列数不足");
        }
    }

    private static void ensurePrimaryKey(java.util.function.Supplier<String> getter,
                                         java.util.function.Consumer<String> setter) {
        if (isBlank(getter.get())) {
            setter.accept(UUID.randomUUID().toString().replace("-", ""));
        }
    }

    static String clip(String raw, int max) {
        if (raw == null) {
            return "";
        }
        String value = raw.trim();
        if (value.isEmpty()) {
            return "";
        }
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }

    /** 数值型字段：空串转 null，避免 DECIMAL 列写入失败；保留科学计数法原文由库解析 */
    static String num(String raw, int max) {
        String value = clip(raw, max);
        return value.isEmpty() ? null : value;
    }
}
