package com.pgis.alarm;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "报警记录地图点，优先使用报警人定位坐标")
public class AlarmMapPoint {

    @Schema(description = "经度")
    private Double lng;

    @Schema(description = "纬度")
    private Double lat;

    @Schema(description = "接警编号")
    private String jjbh;

    @Schema(description = "行政区划名称")
    private String xzqhmc;

    @Schema(description = "接警类型")
    private String jjlx;

    @Schema(description = "警情类别代码")
    private String jqlbdm;

    @Schema(description = "警情类型代码")
    private String jqlxdm;

    @Schema(description = "警情地址")
    private String jqdz;

    @Schema(description = "报警内容")
    private String bjnr;

    @Schema(description = "接警时间，YYYYMMDDHHMMSS")
    private String jjsj;
}
