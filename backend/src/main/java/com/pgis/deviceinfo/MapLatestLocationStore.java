package com.pgis.deviceinfo;

import com.pgis.devicelocation.DeviceLocationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * MySQL 上维护每台设备一条最新定位，避免地图聚合对 002 逐台回表。
 * H2 测试或快照未就绪时退回原查询。
 */
@Component
public class MapLatestLocationStore {

    private static final Logger log = LoggerFactory.getLogger(MapLatestLocationStore.class);

    static final String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS jysb_sbxx_002_latest (
                sbbh      VARCHAR(20)     NOT NULL,
                jd        DECIMAL(20, 10) NOT NULL,
                wd        DECIMAL(20, 10) NOT NULL,
                sbsyjssj  CHAR(14)        DEFAULT NULL,
                lrsj      CHAR(14)        DEFAULT NULL,
                xxzjbh    VARCHAR(32)     NOT NULL,
                PRIMARY KEY (sbbh),
                INDEX idx_002_latest_geo (jd, wd)
            )
            """;

    static final String BACKFILL = """
            INSERT INTO jysb_sbxx_002_latest (sbbh, jd, wd, sbsyjssj, lrsj, xxzjbh)
            SELECT sbbh, jd, wd, sbsyjssj, lrsj, xxzjbh
            FROM (
                SELECT sbbh, jd, wd, sbsyjssj, lrsj, xxzjbh,
                    ROW_NUMBER() OVER (
                        PARTITION BY sbbh
                        ORDER BY sbsyjssj DESC, lrsj DESC, xxzjbh DESC
                    ) AS rn
                FROM jysb_sbxx_002
                WHERE jd IS NOT NULL AND wd IS NOT NULL
            ) t
            WHERE rn = 1
            """;

    private final JdbcTemplate jdbcTemplate;
    private final DeviceLocationMapper locationMapper;
    private final boolean mysql;
    private final AtomicBoolean useTable = new AtomicBoolean(false);

    public MapLatestLocationStore(JdbcTemplate jdbcTemplate,
                                  DeviceLocationMapper locationMapper,
                                  @Value("${spring.datasource.url:}") String jdbcUrl) {
        this.jdbcTemplate = jdbcTemplate;
        this.locationMapper = locationMapper;
        this.mysql = StringUtils.hasText(jdbcUrl) && jdbcUrl.contains("mysql");
    }

    public boolean useTable() {
        return useTable.get();
    }

    public void ensureAndBackfill() {
        if (!mysql) {
            return;
        }
        jdbcTemplate.execute(CREATE_TABLE);
        Long latest = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM jysb_sbxx_002_latest", Long.class);
        Long source = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM jysb_sbxx_002", Long.class);
        if (source != null && source == 0) {
            useTable.set(true);
            return;
        }
        if (latest != null && latest > 0) {
            useTable.set(true);
            log.info("地图最新定位快照已存在 {} 条", latest);
            return;
        }
        log.info("正在回填地图最新定位快照，定位表较大时可能需要几分钟");
        jdbcTemplate.execute(BACKFILL);
        Long filled = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM jysb_sbxx_002_latest", Long.class);
        useTable.set(filled != null && filled > 0);
        log.info("地图最新定位快照回填完成 {} 条", filled);
    }

    public void refresh(List<String> sbbhs) {
        if (!mysql || CollectionUtils.isEmpty(sbbhs)) {
            return;
        }
        List<String> ids = sbbhs.stream().filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty()).distinct().toList();
        if (ids.isEmpty()) {
            return;
        }
        locationMapper.deleteLatestBySbbhs(ids);
        locationMapper.insertLatestBySbbhs(ids);
    }
}
