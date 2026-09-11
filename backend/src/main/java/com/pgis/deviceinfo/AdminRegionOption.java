package com.pgis.deviceinfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "省/市下拉选项")
public class AdminRegionOption {

    @Schema(description = "区划前缀：省 2 位，市 4 位")
    private String code;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "下属地级市；仅省级节点有值")
    private List<AdminRegionOption> cities = new ArrayList<>();
}
