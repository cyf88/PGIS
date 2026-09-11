package com.pgis.config;

import com.pgis.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "地图前端配置")
@RestController
@RequestMapping("/api/map-config")
public class MapConfigController {

    private final MapUiProperties properties;

    public MapConfigController(MapUiProperties properties) {
        this.properties = properties;
    }

    @Operation(summary = "地图页底图列表、默认缩放与兜底范围")
    @GetMapping
    public ApiResponse<MapUiProperties> get() {
        return ApiResponse.ok(properties);
    }
}
