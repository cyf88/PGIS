package com.pgis.deviceinfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "设备基本信息 + 最近定位")
public class DeviceInfoWithLocation extends DeviceInfo {

    @Schema(description = "最近定位经度，无定位时为 null")
    private String jd;

    @Schema(description = "最近定位纬度，无定位时为 null")
    private String wd;

    @Schema(description = "最近一条定位的设备使用结束时间")
    private String sbsyjssj;
}
