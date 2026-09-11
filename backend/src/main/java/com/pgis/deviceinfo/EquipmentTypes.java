package com.pgis.deviceinfo;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 装备类型代码表（01–11、99）。 */
final class EquipmentTypes {

    static final String[][] DEFS = {
            {"01", "警用汽车"},
            {"02", "警用电动车"},
            {"03", "警用摩托车"},
            {"04", "警用无人机"},
            {"05", "警用直升机"},
            {"06", "手台"},
            {"07", "移动警务终端"},
            {"08", "执法记录仪"},
            {"09", "警用船舶"},
            {"10", "通信车"},
            {"11", "机器人"},
            {"99", "其他"}
    };

    private EquipmentTypes() {
    }

    static List<EquipmentTypeStat> merge(List<EquipmentTypeStat> raw) {
        Map<String, Long> counts = new LinkedHashMap<>();
        Map<String, String> names = new LinkedHashMap<>();
        for (String[] def : DEFS) {
            counts.put(def[0], 0L);
            names.put(def[0], def[1]);
        }
        if (raw != null) {
            for (EquipmentTypeStat row : raw) {
                long n = row.getCount() == null ? 0L : row.getCount();
                String code = normalize(row.getZblx());
                counts.merge(code, n, Long::sum);
            }
        }
        List<EquipmentTypeStat> list = new ArrayList<>();
        for (String[] def : DEFS) {
            list.add(new EquipmentTypeStat(def[0], names.get(def[0]), counts.get(def[0])));
        }
        return list;
    }

    static String normalize(String zblx) {
        if (!StringUtils.hasText(zblx)) {
            return "99";
        }
        String t = zblx.trim();
        if (t.endsWith(".0") || t.endsWith(".00")) {
            t = t.substring(0, t.indexOf('.'));
        }
        try {
            int n = Integer.parseInt(t);
            if (n == 99) {
                return "99";
            }
            if (n >= 1 && n <= 11) {
                return String.format("%02d", n);
            }
        } catch (NumberFormatException ignored) {
            // fall through
        }
        return "99";
    }
}
