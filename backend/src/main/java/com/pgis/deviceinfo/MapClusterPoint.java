package com.pgis.deviceinfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "地图视野内一个聚合格或一个设备点")
public class MapClusterPoint {

    @Schema(description = "经度（行政区设备质心或设备点）")
    private Double lng;

    @Schema(description = "纬度（行政区设备质心或设备点）")
    private Double lat;

    @Schema(description = "该簇/区划设备数；点模式下为 1")
    private Long count;

    @Schema(description = "点模式下的设备编号；行政区为区划前缀；邻近点为格网键")
    private String sbbh;

    @Schema(description = "点模式下的警员姓名")
    private String jyxm;

    @Schema(description = "行政区聚合时的区划名称；邻近点/撒点为 null")
    private String regionName;

    @Schema(description = "province / city / county / nearby")
    private String regionLevel;
}
