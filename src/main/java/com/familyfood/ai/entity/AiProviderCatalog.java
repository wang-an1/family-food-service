package com.familyfood.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("ai_provider_catalog")
@Schema(description = "AI 供应商目录")
public class AiProviderCatalog {
    @TableId(type = IdType.AUTO)
    @Schema(description = "供应商 ID", example = "1")
    private Long id;
    @Schema(description = "供应商编码", example = "deepseek")
    private String code;
    @Schema(description = "展示名称", example = "DeepSeek")
    private String displayName;
    @Schema(description = "调用类型", example = "OPENAI_CHAT_COMPLETIONS")
    private String callType;
    @Schema(description = "API 基础地址", example = "https://api.deepseek.com")
    private String baseUrl;
    @Schema(description = "状态", example = "ACTIVE")
    private String status;
    @Schema(description = "排序", example = "10")
    private Integer sortOrder;
    @Schema(description = "创建时间", format = "date-time")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间", format = "date-time")
    private LocalDateTime updatedAt;
}
