package com.familyfood.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "AI 菜品转换覆盖字段")
public record ConvertOverride(
        @Schema(description = "覆盖后的菜品名称", example = "番茄炒蛋")
        @Size(max = 100)
        String name,
        @Schema(description = "覆盖后的分类 ID", example = "1")
        Long categoryId,
        @Schema(description = "覆盖后的标签 ID 列表", example = "[1,2]")
        List<Long> tagIds,
        @Schema(description = "覆盖后的菜品状态", allowableValues = {"ACTIVE", "INACTIVE", "DRAFT"}, example = "ACTIVE")
        @Pattern(regexp = "ACTIVE|INACTIVE|DRAFT")
        String status
) {
}
