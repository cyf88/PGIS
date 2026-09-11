package com.pgis.deviceinfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "警用设备基本信息")
public class DeviceInfo {
    @Schema(description = "设备编号")
    private String sbbh;
    @Schema(description = "设备品牌")
    private String sbpp;
    @Schema(description = "设备型号")
    private String sbxh;
    @Schema(description = "装备类型")
    private String zblx;
    @Schema(description = "所属公安机关机构代码")
    private String ssgajgjgdm;
    @Schema(description = "车牌号码")
    private String cphm;
    @Schema(description = "警员编号")
    private String jybh;
    @Schema(description = "警员姓名")
    private String jyxm;
    @Schema(description = "警员身份证号")
    private String jysfz;
    @Schema(description = "呼号")
    private String hh;
    @Schema(description = "联系电话")
    private String lxdh;
    @Schema(description = "设备使用状态，01在线/02不在线")
    private String sbsyzt;
    @Schema(description = "备注")
    private String bz;
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
