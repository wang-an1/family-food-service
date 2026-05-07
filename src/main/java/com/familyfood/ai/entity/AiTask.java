package com.familyfood.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("ai_task")
@Schema(description = "AI 任务")
public class AiTask {
    @TableId(type = IdType.AUTO)
    @Schema(description = "AI 任务 ID", example = "1")
    private Long id;
    @Schema(description = "家庭 ID", example = "1")
    private Long familyId;
    @Schema(description = "发起用户 ID", example = "2")
    private Long userId;
    @Schema(description = "任务类型", example = "PARSE_LINK")
    private String taskType;
    @Schema(description = "来源类型", example = "LINK")
    private String sourceType;
    @Schema(description = "输入文本")
    private String inputText;
    @Schema(description = "来源 URL")
    private String sourceUrl;
    @Schema(description = "图片 URL")
    private String imageUrl;
    @Schema(description = "任务状态", example = "SUCCESS")
    private String status;
    @Schema(description = "结果摘要")
    private String resultSummary;
    @Schema(description = "错误码")
    private String errorCode;
    @Schema(description = "错误消息")
    private String errorMessage;
    @Schema(description = "重试次数", example = "0")
    private Integer retryCount;
    @Schema(description = "模型名称", example = "deepseek-v4-pro")
    private String modelName;
    @Schema(description = "提示词 token 数", example = "120")
    private Integer promptTokens;
    @Schema(description = "输出 token 数", example = "350")
    private Integer completionTokens;
    @Schema(description = "开始时间", format = "date-time")
    private LocalDateTime startedAt;
    @Schema(description = "完成时间", format = "date-time")
    private LocalDateTime finishedAt;
    @Schema(description = "创建时间", format = "date-time")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间", format = "date-time")
    private LocalDateTime updatedAt;
}
