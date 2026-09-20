package com.pgis.caseinfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "案件信息地图点，取案件发生地址经纬度")
public class CaseMapPoint {

    @Schema(description = "经度")
    private Double lng;

    @Schema(description = "纬度")
    private Double lat;

    @Schema(description = "信息主键编号")
    private String xxzjbh;

    @Schema(description = "案事件编号")
    private String asjbh;

    @Schema(description = "案件名称")
    private String ajmc;

    @Schema(description = "案件发生地行政区划名称")
    private String ajfsdxzqhmc;

    @Schema(description = "案件发生地址名称")
    private String ajfsdzmc;

    @Schema(description = "简要案情")
    private String jyaq;

    @Schema(description = "受理时间")
    private String slsj;

    @Schema(description = "案件来源代码")
    private String ajlydm;
}
