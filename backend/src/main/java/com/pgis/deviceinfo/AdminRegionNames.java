package com.pgis.deviceinfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 将 sjly 区划前缀解析为名称（当前内置河北省行政区划代码表）。 */
@Component
public class AdminRegionNames {

    private static final Logger log = LoggerFactory.getLogger(AdminRegionNames.class);
    private static final String RESOURCE = "region/hebei-admin-codes.csv";

    private final Map<String, String> codeToName = new HashMap<>();
    private final Map<String, String> codeToLevel = new HashMap<>();
    private final List<AdminRegionOption> provinces = new ArrayList<>();
    private final Map<String, AdminRegionOption> provinceByCode = new LinkedHashMap<>();

    public AdminRegionNames() {
        load(RESOURCE);
    }

    AdminRegionNames(String classpathLocation) {
        load(classpathLocation);
    }

    private void load(String classpathLocation) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource(classpathLocation).getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                if (!StringUtils.hasText(line)) {
                    continue;
                }
                String[] parts = line.split(",", 3);
                if (parts.length < 2) {
                    continue;
                }
                String code = parts[0].trim();
                String name = parts[1].trim();
                if (StringUtils.hasText(code) && StringUtils.hasText(name)) {
                    codeToName.put(code, name);
                    if (parts.length >= 3) {
                        String level = normalizeLevel(parts[2].trim());
                        if (level != null) {
                            codeToLevel.put(code, level);
                            indexDropdown(code, name, level);
                        }
                    }
                }
            }
            log.info("已加载行政区划名称 {} 条", codeToName.size());
        } catch (Exception e) {
            log.warn("加载行政区划名称失败: {}", e.getMessage());
        }
    }

    public String nameOf(String regionPrefix) {
        if (!StringUtils.hasText(regionPrefix)) {
            return null;
        }
        String digits = regionPrefix.trim();
        String padded = padToSix(digits);
        String name = codeToName.get(padded);
        if (name != null) {
            return name;
        }
        return codeToName.get(digits);
    }

    /** 由区划前缀得到省名称；对照表没有时返回前两位代码。 */
    public String provinceTitle(String regionPrefix) {
        if (!StringUtils.hasText(regionPrefix)) {
            return "全部";
        }
        String digits = regionPrefix.trim().replaceAll("\\D", "");
        if (digits.length() < 2) {
            return regionPrefix.trim();
        }
        String two = digits.substring(0, 2);
        String name = nameOf(two);
        return StringUtils.hasText(name) ? name : two;
    }

    public List<AdminRegionOption> provinces() {
        return provinces;
    }

    private void indexDropdown(String sixDigit, String name, String level) {
        if ("province".equals(level) && sixDigit.length() >= 2) {
            String code = sixDigit.substring(0, 2);
            AdminRegionOption p = provinceByCode.get(code);
            if (p == null) {
                p = new AdminRegionOption();
                p.setCode(code);
                p.setName(name);
                p.setCities(new ArrayList<>());
                provinceByCode.put(code, p);
                provinces.add(p);
            }
            return;
        }
        if ("city".equals(level) && sixDigit.length() >= 4) {
            String provinceCode = sixDigit.substring(0, 2);
            String cityCode = sixDigit.substring(0, 4);
            AdminRegionOption p = provinceByCode.get(provinceCode);
            if (p == null) {
                return;
            }
            boolean exists = p.getCities().stream().anyMatch(c -> cityCode.equals(c.getCode()));
            if (exists) {
                return;
            }
            AdminRegionOption city = new AdminRegionOption();
            city.setCode(cityCode);
            city.setName(name);
            p.getCities().add(city);
        }
    }

    /** province / city / county；表中无记录时按 6 位码规则推断。 */
    public String levelOf(String regionPrefix) {
        if (!StringUtils.hasText(regionPrefix)) {
            return null;
        }
        String digits = regionPrefix.trim();
        String padded = padToSix(digits);
        String level = codeToLevel.get(padded);
        if (level == null) {
            level = codeToLevel.get(digits);
        }
        if (level != null) {
            return level;
        }
        return inferLevel(padded);
    }

    static String expectedLevel(int prefixLen) {
        if (prefixLen <= 2) {
            return "province";
        }
        if (prefixLen <= 4) {
            return "city";
        }
        return "county";
    }

    static String inferLevel(String sixDigitCode) {
        if (sixDigitCode.endsWith("0000")) {
            return "province";
        }
        if (sixDigitCode.endsWith("00")) {
            return "city";
        }
        return "county";
    }

    static String normalizeLevel(String csvLevel) {
        if ("省".equals(csvLevel)) {
            return "province";
        }
        if ("地级市".equals(csvLevel)) {
            return "city";
        }
        if ("县级".equals(csvLevel)) {
            return "county";
        }
        return null;
    }

    static String padToSix(String code) {
        if (code.length() >= 6) {
            return code.substring(0, 6);
        }
        StringBuilder sb = new StringBuilder(code);
        while (sb.length() < 6) {
            sb.append('0');
        }
        return sb.toString();
    }
}
