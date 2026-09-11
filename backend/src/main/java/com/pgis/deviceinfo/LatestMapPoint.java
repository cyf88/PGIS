package com.pgis.deviceinfo;

import lombok.Data;

/** 视野内一台设备的最新定位，供一次查出后在内存聚合。 */
@Data
class LatestMapPoint {
    private String sbbh;
    private Double jd;
    private Double wd;
    private String sjly;
    private String zblx;
    private String jyxm;
}
