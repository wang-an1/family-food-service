package com.familyfood.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 链接或素材解析响应")
public record ParseLinkResponse(
        @Schema(description = "AI 任务 ID", example = "1")
        Long taskId,
        @Schema(description = "任务状态", example = "PENDING")
        String status,
        @Schema(description = "来源类型", example = "LINK")
        String sourceType,
        @Schema(description = "任务轮询地址", example = "/api/ai/tasks/1")
        String pollingUrl) {
}
