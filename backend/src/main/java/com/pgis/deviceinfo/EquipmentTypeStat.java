package com.pgis.deviceinfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "地图视野内某装备类型数量")
public class EquipmentTypeStat {

    @Schema(description = "装备类型代码，两位")
    private String zblx;

    @Schema(description = "装备类型名称")
    private String name;

    @Schema(description = "该类型设备数")
    private Long count;
}
