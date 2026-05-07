package com.familyfood.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("ai_model_catalog")
@Schema(description = "AI 模型目录")
public class AiModelCatalog {
    @TableId(type = IdType.AUTO)
    @Schema(description = "模型 ID", example = "1")
    private Long id;
    @Schema(description = "供应商 ID", example = "1")
    private Long providerId;
    @Schema(description = "模型名称", example = "deepseek-v4-pro")
    private String modelName;
    @Schema(description = "展示名称", example = "DeepSeek V4 Pro")
    private String displayName;
    @TableField("default_model")
    @Schema(description = "是否默认模型，1 表示是", example = "1")
    private Integer defaultModel;
    @Schema(description = "状态", example = "ACTIVE")
    private String status;
    @Schema(description = "排序", example = "10")
    private Integer sortOrder;
    @Schema(description = "创建时间", format = "date-time")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间", format = "date-time")
    private LocalDateTime updatedAt;
}
