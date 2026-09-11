package com.pgis.config;

import com.pgis.deviceinfo.MapLatestLocationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 已有库不会因 schema.sql 的 CREATE TABLE IF NOT EXISTS 补上新索引。
 * 启动时按需创建地图聚合所需索引，并回填最新定位快照。
 */
@Component
@Order(0)
public class MapQueryIndexInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MapQueryIndexInitializer.class);

    private final JdbcTemplate jdbcTemplate;
    private final MapLatestLocationStore latestLocationStore;
    private final String jdbcUrl;

    public MapQueryIndexInitializer(JdbcTemplate jdbcTemplate,
                                    MapLatestLocationStore latestLocationStore,
                                    @Value("${spring.datasource.url:}") String jdbcUrl) {
        this.jdbcTemplate = jdbcTemplate;
        this.latestLocationStore = latestLocationStore;
        this.jdbcUrl = jdbcUrl;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (jdbcUrl == null || !jdbcUrl.contains("mysql")) {
            return;
        }
        ensureIndex("jysb_sbxx_001", "idx_001_sjly",
                "CREATE INDEX idx_001_sjly ON jysb_sbxx_001 (sjly)");
        ensureIndex("jysb_sbxx_001", "idx_001_sjly_dev",
                "CREATE INDEX idx_001_sjly_dev ON jysb_sbxx_001 (sjly, sbbh, zblx)");
        ensureIndex("jysb_sbxx_002", "idx_002_sbbh_latest",
                "CREATE INDEX idx_002_sbbh_latest ON jysb_sbxx_002 (sbbh, sbsyjssj, lrsj, xxzjbh)");
        latestLocationStore.ensureAndBackfill();
    }

    private void ensureIndex(String table, String indexName, String ddl) {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.statistics "
                        + "WHERE table_schema = DATABASE() AND table_name = ? AND index_name = ?",
                Integer.class, table, indexName);
        if (n != null && n > 0) {
            return;
        }
        log.info("正在创建索引 {}.{} ，定位表较大时可能需要几分钟", table, indexName);
        jdbcTemplate.execute(ddl);
        log.info("索引 {}.{} 已创建", table, indexName);
    }
}
