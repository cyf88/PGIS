package com.pgis.deviceinfo;

import com.pgis.common.ApiResponse;
import com.pgis.common.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "警用设备基本信息", description = "表 JYSB_SBXX_001 / jysb_sbxx_001")
@RestController
@RequestMapping("/api/device-info")
public class DeviceInfoController {

    private final DeviceInfoService service;

    public DeviceInfoController(DeviceInfoService service) {
        this.service = service;
    }

    @Operation(summary = "分页查询基本信息")
    @GetMapping
    public ApiResponse<PageResult<DeviceInfo>> page(
            @Parameter(description = "设备编号，模糊匹配") @RequestParam(required = false) String sbbh,
            @Parameter(description = "警员姓名，模糊匹配") @RequestParam(required = false) String jyxm,
            @Parameter(description = "所属公安机关机构代码，模糊匹配") @RequestParam(required = false) String ssgajgjgdm,
            @Parameter(description = "数据来源，精确匹配") @RequestParam(required = false) String sjly,
            @Parameter(description = "页码，从 1 开始") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.page(sbbh, jyxm, ssgajgjgdm, sjly, page, size));
    }

    @Operation(summary = "按数据来源分页查询设备及最近定位")
    @GetMapping("/search-by-sjly")
    public ApiResponse<PageResult<DeviceInfoWithLocation>> searchBySjly(
            @Parameter(description = "数据来源，6位区划，精确匹配") @RequestParam String sjly,
            @Parameter(description = "页码，从 1 开始") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数，最大 100") @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.searchBySjly(sjly, page, size));
    }

    @Operation(summary = "按数据来源统计设备总数")
    @GetMapping("/count-by-sjly")
    public ApiResponse<Long> countBySjly(
            @Parameter(description = "数据来源，6位区划，精确匹配") @RequestParam String sjly) {
        return ApiResponse.ok(service.countBySjly(sjly));
    }

    @Operation(summary = "按地图视野聚合最近定位")
    @GetMapping("/aggregate-in-bounds")
    public ApiResponse<MapBoundsAggregate> aggregateInBounds(
            @Parameter(description = "西南经度") @RequestParam double minLng,
            @Parameter(description = "西南纬度") @RequestParam double minLat,
            @Parameter(description = "东北经度") @RequestParam double maxLng,
            @Parameter(description = "东北纬度") @RequestParam double maxLat,
            @Parameter(description = "缩放：行政区 <=6 省、7-9 市、10-13 区；邻近点 <14 按距离聚合；>=14 撒点") @RequestParam(defaultValue = "10") int zoom,
            @Parameter(description = "数据来源前缀，模糊匹配（如 13 匹配河北省）") @RequestParam(required = false) String sjly,
            @Parameter(description = "聚合方式：region 按省/市/县，nearby 按邻近点") @RequestParam(defaultValue = "region") String clusterMode) {
        return ApiResponse.ok(service.aggregateInBounds(minLng, minLat, maxLng, maxLat, zoom, sjly, clusterMode));
    }

    @Operation(summary = "省/市下拉选项")
    @GetMapping("/admin-regions")
    public ApiResponse<List<AdminRegionOption>> adminRegions() {
        return ApiResponse.ok(service.adminRegions());
    }

    @Operation(summary = "区划内最新定位外包矩形，供地图缩放")
    @GetMapping("/region-bounds")
    public ApiResponse<RegionBounds> regionBounds(
            @Parameter(description = "区划前缀：省 2 位、市 4 位") @RequestParam String sjly) {
        return ApiResponse.ok(service.regionBounds(sjly));
    }

    @Operation(summary = "按主键查询基本信息")
    @GetMapping("/{xxzjbh}")
    public ApiResponse<DeviceInfo> get(
            @Parameter(description = "信息主键编号") @PathVariable String xxzjbh) {
        return ApiResponse.ok(service.get(xxzjbh));
    }

    @Operation(summary = "新增基本信息", description = "xxzjbh 为空时自动生成 32 位 UUID")
    @PostMapping
    public ApiResponse<DeviceInfo> create(@RequestBody DeviceInfo entity) {
        return ApiResponse.ok(service.create(entity));
    }

    @Operation(summary = "修改基本信息")
    @PutMapping("/{xxzjbh}")
    public ApiResponse<Void> update(
            @Parameter(description = "信息主键编号") @PathVariable String xxzjbh,
            @RequestBody DeviceInfo entity) {
        service.update(xxzjbh, entity);
        return ApiResponse.ok(null);
    }

    @Operation(summary = "删除基本信息")
    @DeleteMapping("/{xxzjbh}")
    public ApiResponse<Void> delete(
            @Parameter(description = "信息主键编号") @PathVariable String xxzjbh) {
        service.delete(xxzjbh);
        return ApiResponse.ok(null);
    }
}
