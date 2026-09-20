package com.pgis.caseinfo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "pgis.poi")
public class PoiProperties {

    /** 地图 POI 搜索接口地址 */
    private String baseUrl = "http://10.2.164.43:10000/api/search/poi";

    /** 单次请求超时（秒） */
    private int timeoutSeconds = 10;

    /** 是否启用定时补全经纬度任务 */
    private boolean enabled = false;

    /** 每次定时任务最多补全的记录数 */
    private int batchSize = 50;

    /** 定时任务 cron 表达式（6 段：秒 分 时 日 月 周），默认每小时整点 */
    private String cron = "0 0 * * * *";
}
