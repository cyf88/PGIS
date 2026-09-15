package com.pgis.alarm;

import com.pgis.common.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AlarmService {

    private final AlarmMapper mapper;

    public AlarmService(AlarmMapper mapper) {
        this.mapper = mapper;
    }

    public PageResult<AlarmRecord> page(String jjbh, String xzqhdm, String bjrxm, String jqlbdm, int page, int size) {
        int p = Math.max(page, 1);
        int s = size <= 0 ? 20 : size;
        long total = mapper.countPage(trim(jjbh), trim(xzqhdm), trim(bjrxm), trim(jqlbdm));
        List<AlarmRecord> records = mapper.selectPage(trim(jjbh), trim(xzqhdm), trim(bjrxm), trim(jqlbdm), (p - 1) * s, s);
        return new PageResult<>(total, p, s, records);
    }

    public AlarmRecord get(String jjbh) {
        return mapper.selectById(jjbh);
    }

    public List<AlarmMapPoint> mapPoints(double minLng, double minLat, double maxLng, double maxLat, String xzqhdm) {
        if (minLng >= maxLng || minLat >= maxLat) {
            throw new IllegalArgumentException("地图范围无效：需满足 minLng < maxLng 且 minLat < maxLat");
        }
        if (minLng < -180 || maxLng > 180 || minLat < -90 || maxLat > 90) {
            throw new IllegalArgumentException("经纬度超出有效范围");
        }
        return mapper.selectMapPoints(minLng, minLat, maxLng, maxLat, trim(xzqhdm));
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
