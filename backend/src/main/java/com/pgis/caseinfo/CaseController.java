package com.pgis.caseinfo;

import com.pgis.common.ApiResponse;
import com.pgis.common.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "案件信息", description = "表 xsan")
@RestController
@RequestMapping("/api/case")
public class CaseController {

    private final CaseService service;

    public CaseController(CaseService service) {
        this.service = service;
    }

    @Operation(summary = "分页查询案件信息")
    @GetMapping
    public ApiResponse<PageResult<CaseRecord>> page(
            @Parameter(description = "案事件编号，模糊匹配") @RequestParam(required = false) String asjbh,
            @Parameter(description = "案件名称，模糊匹配") @RequestParam(required = false) String ajmc,
            @Parameter(description = "案件发生地行政区划代码，前缀匹配") @RequestParam(required = false) String ajfsdxzqhdm,
            @Parameter(description = "数据来源，精确匹配") @RequestParam(required = false) String sjly,
            @Parameter(description = "页码，从 1 开始") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.page(asjbh, ajmc, ajfsdxzqhdm, sjly, page, size));
    }

    @Operation(summary = "按地图视野查询案件点")
    @GetMapping("/map-points")
    public ApiResponse<List<CaseMapPoint>> mapPoints(
            @Parameter(description = "西南经度") @RequestParam double minLng,
            @Parameter(description = "西南纬度") @RequestParam double minLat,
            @Parameter(description = "东北经度") @RequestParam double maxLng,
            @Parameter(description = "东北纬度") @RequestParam double maxLat,
            @Parameter(description = "行政区划代码前缀，模糊匹配") @RequestParam(required = false) String xzqhdm) {
        return ApiResponse.ok(service.mapPoints(minLng, minLat, maxLng, maxLat, xzqhdm));
    }

    @Operation(summary = "为缺失经纬度的案件调用 POI 接口补全坐标")
    @PostMapping("/enrich-locations")
    public ApiResponse<Integer> enrichLocations(
            @Parameter(description = "最多处理的记录数，1-200") @RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.ok(service.enrichLocations(limit));
    }

    @Operation(summary = "按信息主键编号查询案件详情")
    @GetMapping("/{xxzjbh}")
    public ApiResponse<CaseRecord> get(
            @Parameter(description = "信息主键编号") @PathVariable String xxzjbh) {
        return ApiResponse.ok(service.get(xxzjbh));
    }
}
