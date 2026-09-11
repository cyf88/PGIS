package com.pgis.devicelocation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "警用设备定位信息")
public class DeviceLocation {
    @Schema(description = "设备编号")
    private String sbbh;
    @Schema(description = "经度，WGS-84")
    private String jd;
    @Schema(description = "纬度，WGS-84")
    private String wd;
    @Schema(description = "水平速度，米/秒")
    private String spsd;
    @Schema(description = "垂直速度，米/秒")
    private String czsd;
    @Schema(description = "航迹角")
    private String hjj;
    @Schema(description = "高程，米")
    private String gc;
    @Schema(description = "定位精度，对应库字段 jd_01")
    private String jd01;
    @Schema(description = "设备使用开始时间，YYYYMMDDHHMMSS")
    private String sbsykssj;
    @Schema(description = "设备使用结束时间，YYYYMMDDHHMMSS")
    private String sbsyjssj;
    @Schema(description = "数据操作类型，1新增/2修改/3撤销")
    private String sjczlxdm;
    @Schema(description = "数据来源，6位区划代码")
    private String sjly;
    @Schema(description = "录入时间，YYYYMMDDHHMMSS")
    private String lrsj;
    @Schema(description = "录入单位代码")
    private String lrdwdm;
    @Schema(description = "录入单位名称")
    private String lrdwmc;
    @Schema(description = "录入人姓名")
    private String lrrxm;
    @Schema(description = "录入人身份证号")
    private String lrrsfzh;
    @Schema(description = "修改时间，YYYYMMDDHHMMSS")
    private String xgsj;
    @Schema(description = "修改单位代码")
    private String xgdwdm;
    @Schema(description = "修改单位名称")
    private String xgdwmc;
    @Schema(description = "修改人姓名")
    private String xgrxm;
    @Schema(description = "修改人身份证号")
    private String xgrsfzh;
    @Schema(description = "信息主键编号")
    private String xxzjbh;
}
