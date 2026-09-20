package com.pgis.caseinfo;

import com.pgis.common.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CaseService {

    private static final Logger log = LoggerFactory.getLogger(CaseService.class);

    private final CaseMapper mapper;
    private final PoiSearchClient poiSearchClient;

    public CaseService(CaseMapper mapper, PoiSearchClient poiSearchClient) {
        this.mapper = mapper;
        this.poiSearchClient = poiSearchClient;
    }

    public PageResult<CaseRecord> page(String asjbh, String ajmc, String ajfsdxzqhdm, String sjly, int page, int size) {
        int p = Math.max(page, 1);
        int s = size <= 0 ? 20 : size;
        long total = mapper.countPage(trim(asjbh), trim(ajmc), trim(ajfsdxzqhdm), trim(sjly));
        List<CaseRecord> records = mapper.selectPage(trim(asjbh), trim(ajmc), trim(ajfsdxzqhdm), trim(sjly), (p - 1) * s, s);
        return new PageResult<>(total, p, s, records);
    }

    public CaseRecord get(String xxzjbh) {
        return mapper.selectById(xxzjbh);
    }

    public List<CaseMapPoint> mapPoints(double minLng, double minLat, double maxLng, double maxLat, String xzqhdm) {
        if (minLng >= maxLng || minLat >= maxLat) {
            throw new IllegalArgumentException("地图范围无效：需满足 minLng < maxLng 且 minLat < maxLat");
        }
        if (minLng < -180 || maxLng > 180 || minLat < -90 || maxLat > 90) {
            throw new IllegalArgumentException("经纬度超出有效范围");
        }
        return mapper.selectMapPoints(minLng, minLat, maxLng, maxLat, trim(xzqhdm));
    }

    public int enrichLocations(int limit) {
        int n = Math.max(1, Math.min(limit, 200));
        List<CaseRecord> records = mapper.selectNoLocation(n);
        log.info("开始补全案件经纬度: 本次捞取 {} 条无坐标案件（批量上限 {}）", records.size(), n);
        if (records.isEmpty()) {
            log.info("没有需要补全经纬度的案件，本次跳过");
            return 0;
        }
        int updated = 0;
        int missed = 0;
        int skipped = 0;
        int index = 0;
        for (CaseRecord r : records) {
            index++;
            String keywords = StringUtils.hasText(r.getAjfsdzmc()) ? r.getAjfsdzmc().trim() : r.getAjfsdxzqhmc();
            if (!StringUtils.hasText(keywords)) {
                skipped++;
                log.warn("[{}/{}] 案件 {} 跳过: 地址名称与行政区划均为空，无法构造搜索词",
                        index, records.size(), r.getXxzjbh());
                continue;
            }
            String region = StringUtils.hasText(r.getAjfsdxzqhmc()) ? r.getAjfsdxzqhmc().trim() : null;
            log.info("[{}/{}] 案件 {} 调用POI: keywords={}, region={}",
                    index, records.size(), r.getXxzjbh(), keywords, region);
            String[] coord = poiSearchClient.search(keywords.trim(), region);
            if (coord != null) {
                mapper.updateLocation(r.getXxzjbh(), coord[0], coord[1]);
                updated++;
                log.info("[{}/{}] 案件 {} 已回写坐标: lng={}, lat={}",
                        index, records.size(), r.getXxzjbh(), coord[0], coord[1]);
            } else {
                missed++;
                log.warn("[{}/{}] 案件 {} 未获取到坐标，保持空值，下次任务重试",
                        index, records.size(), r.getXxzjbh());
            }
        }
        log.info("补全案件经纬度结束: 共{}条, 成功更新{}条, 未命中{}条, 跳过{}条",
                records.size(), updated, missed, skipped);
        return updated;
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
