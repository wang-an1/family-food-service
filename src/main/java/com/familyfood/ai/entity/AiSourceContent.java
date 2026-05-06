package com.familyfood.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("ai_source_content")
@Schema(description = "AI 来源内容")
public class AiSourceContent {
    @TableId(type = IdType.AUTO)
    @Schema(description = "来源内容 ID", example = "1")
    private Long id;
    @Schema(description = "AI 任务 ID", example = "10")
    private Long aiTaskId;
    @Schema(description = "解析后的 URL")
    private String resolvedUrl;
    @Schema(description = "来源标题")
    private String title;
    @Schema(description = "来源描述")
    private String description;
    @Schema(description = "正文内容")
    private String contentText;
    @Schema(description = "封面图片 URL")
    private String coverUrl;
    @Schema(description = "原始元数据 JSON")
    private String rawMetadataJson;
}
