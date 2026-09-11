package com.pgis.importdata;

import com.pgis.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "CSV 导入", description = "按文件名前缀导入：sb001* → 基本信息，sb002* → 定位信息；主键冲突则覆盖")
@RestController
@RequestMapping("/api/import")
public class ImportController {

    private static final Logger log = LoggerFactory.getLogger(ImportController.class);

    private final CsvImportService csvImportService;

    public ImportController(CsvImportService csvImportService) {
        this.csvImportService = csvImportService;
    }

    @Operation(summary = "导入基本信息 CSV",
            description = "multipart 字段名 files，可多文件。仅接受 sb001 开头的 .csv。带头文件会自动跳过表头。")
    @PostMapping(value = "/device-info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImportResult> importDeviceInfo(
            @Parameter(description = "CSV 文件列表", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("files") List<MultipartFile> files) {
        logReceived("device-info", files);
        return ApiResponse.ok(csvImportService.importDeviceInfo(files));
    }

    @Operation(summary = "导入定位信息 CSV",
            description = "multipart 字段名 files，可多文件。仅接受 sb002 开头的 .csv。带头文件会自动跳过表头。")
    @PostMapping(value = "/device-location", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImportResult> importDeviceLocation(
            @Parameter(description = "CSV 文件列表", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("files") List<MultipartFile> files) {
        logReceived("device-location", files);
        return ApiResponse.ok(csvImportService.importDeviceLocation(files));
    }

    private void logReceived(String kind, List<MultipartFile> files) {
        long bytes = files == null ? 0 : files.stream().mapToLong(MultipartFile::getSize).sum();
        int count = files == null ? 0 : files.size();
        log.info("multipart 已接收 {} files={} totalBytes={}", kind, count, bytes);
    }
}
