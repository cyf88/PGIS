package com.pgis.deviceinfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "地图视野聚合结果")
public class MapBoundsAggregate {

    @Schema(description = "region=按行政区划，nearby=按邻近点，point=撒点")
    private String mode;

    @Schema(description = "视野内设备总数（按最新定位落在框内）")
    private long total;

    @Schema(description = "邻近点聚合时的格网边长（度）；其它模式为 null")
    private Double cellSize;

    @Schema(description = "行政区气泡或设备点")
    private List<MapClusterPoint> clusters = new ArrayList<>();

    @Schema(description = "当前视野内按装备类型统计（与最新定位落在框内的设备一致）")
    private List<EquipmentTypeStat> typeStats = new ArrayList<>();

    @Schema(description = "当前搜索/视野对应的省名称")
    private String provinceName;
}
