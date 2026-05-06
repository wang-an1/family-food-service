package com.familyfood.intent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "点餐意图提交响应")
public record IntentResponse(
        @Schema(description = "点餐意图 ID", example = "1")
        Long id,
        @Schema(description = "处理状态", example = "PENDING")
        String status,
        @Schema(description = "关联 AI 任务 ID", example = "10")
        Long aiTaskId) {
}
