package com.familyfood.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "文件上传响应")
public record UploadResponse(
        @Schema(description = "文件访问 URL", example = "/uploads/dish/example.jpg")
        String url,
        @Schema(description = "原始文件名", example = "example.jpg")
        String originalName,
        @Schema(description = "文件大小，单位字节", example = "102400")
        long size) {
}
