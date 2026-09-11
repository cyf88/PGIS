package com.pgis.importdata;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "CSV 导入结果")
public class ImportResult {
    @Schema(description = "新增条数")
    private long inserted;
    @Schema(description = "覆盖更新条数")
    private long updated;
    @Schema(description = "失败条数")
    private long failed;
    @Schema(description = "被覆盖的主键列表，最多 200 条")
    private List<String> updatedKeys = new ArrayList<>();
    @Schema(description = "失败明细，最多 200 条")
    private List<RowFailure> failures = new ArrayList<>();
    @Schema(description = "跳过的文件")
    private List<SkippedFile> skippedFiles = new ArrayList<>();

    public void addInserted(int n) {
        inserted += n;
    }

    public void addUpdated(String key) {
        updated++;
        if (updatedKeys.size() < 200) {
            updatedKeys.add(key);
        }
    }

    public void addFailure(String file, long line, String reason) {
        failed++;
        if (failures.size() < 200) {
            failures.add(new RowFailure(file, line, reason));
        }
    }

    public void addSkipped(String file, String reason) {
        skippedFiles.add(new SkippedFile(file, reason));
    }
}
