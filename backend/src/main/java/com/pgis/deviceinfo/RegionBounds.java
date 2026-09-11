package com.pgis.deviceinfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "区划内最新定位的外包矩形")
public class RegionBounds {

    private Double minLng;
    private Double minLat;
    private Double maxLng;
    private Double maxLat;
}
