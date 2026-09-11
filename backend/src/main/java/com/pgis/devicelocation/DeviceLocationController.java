package com.pgis.devicelocation;

import com.pgis.common.ApiResponse;
import com.pgis.common.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "警用设备定位信息", description = "表 JYSB_SBXX_002 / jysb_sbxx_002")
@RestController
@RequestMapping("/api/device-location")
public class DeviceLocationController {

    private final DeviceLocationService service;

    public DeviceLocationController(DeviceLocationService service) {
        this.service = service;
    }

    @Operation(summary = "分页查询定位信息")
    @GetMapping
    public ApiResponse<PageResult<DeviceLocation>> page(
            @Parameter(description = "设备编号，模糊匹配") @RequestParam(required = false) String sbbh,
            @Parameter(description = "数据来源，精确匹配") @RequestParam(required = false) String sjly,
            @Parameter(description = "页码，从 1 开始") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.page(sbbh, sjly, page, size));
    }

    @Operation(summary = "按主键查询定位信息")
    @GetMapping("/{xxzjbh}")
    public ApiResponse<DeviceLocation> get(
            @Parameter(description = "信息主键编号") @PathVariable String xxzjbh) {
        return ApiResponse.ok(service.get(xxzjbh));
    }

    @Operation(summary = "新增定位信息", description = "xxzjbh 为空时自动生成 32 位 UUID")
    @PostMapping
    public ApiResponse<DeviceLocation> create(@RequestBody DeviceLocation entity) {
        return ApiResponse.ok(service.create(entity));
    }

    @Operation(summary = "修改定位信息")
    @PutMapping("/{xxzjbh}")
    public ApiResponse<Void> update(
            @Parameter(description = "信息主键编号") @PathVariable String xxzjbh,
            @RequestBody DeviceLocation entity) {
        service.update(xxzjbh, entity);
        return ApiResponse.ok(null);
    }

    @Operation(summary = "删除定位信息")
    @DeleteMapping("/{xxzjbh}")
    public ApiResponse<Void> delete(
            @Parameter(description = "信息主键编号") @PathVariable String xxzjbh) {
        service.delete(xxzjbh);
        return ApiResponse.ok(null);
    }
}
