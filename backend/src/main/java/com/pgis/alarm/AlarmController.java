package com.pgis.alarm;

import com.pgis.common.ApiResponse;
import com.pgis.common.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "接处警报警信息", description = "表 jqhj_jjdb")
@RestController
@RequestMapping("/api/alarm")
public class AlarmController {

    private final AlarmService service;

    public AlarmController(AlarmService service) {
        this.service = service;
    }

    @Operation(summary = "分页查询报警记录")
    @GetMapping
    public ApiResponse<PageResult<AlarmRecord>> page(
            @Parameter(description = "接警编号，模糊匹配") @RequestParam(required = false) String jjbh,
            @Parameter(description = "行政区划代码，前缀匹配") @RequestParam(required = false) String xzqhdm,
            @Parameter(description = "报警人名称，模糊匹配") @RequestParam(required = false) String bjrxm,
            @Parameter(description = "警情类别代码，精确匹配") @RequestParam(required = false) String jqlbdm,
            @Parameter(description = "页码，从 1 开始") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.page(jjbh, xzqhdm, bjrxm, jqlbdm, page, size));
    }

    @Operation(summary = "按地图视野查询报警点")
    @GetMapping("/map-points")
    public ApiResponse<List<AlarmMapPoint>> mapPoints(
            @Parameter(description = "西南经度") @RequestParam double minLng,
            @Parameter(description = "西南纬度") @RequestParam double minLat,
            @Parameter(description = "东北经度") @RequestParam double maxLng,
            @Parameter(description = "东北纬度") @RequestParam double maxLat,
            @Parameter(description = "行政区划代码前缀，模糊匹配") @RequestParam(required = false) String xzqhdm) {
        return ApiResponse.ok(service.mapPoints(minLng, minLat, maxLng, maxLat, xzqhdm));
    }

    @Operation(summary = "按接警编号查询报警详情")
    @GetMapping("/{jjbh}")
    public ApiResponse<AlarmRecord> get(
            @Parameter(description = "接警编号") @PathVariable String jjbh) {
        return ApiResponse.ok(service.get(jjbh));
    }
}
