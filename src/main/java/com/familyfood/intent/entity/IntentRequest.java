package com.familyfood.intent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("intent_request")
@Schema(description = "点餐意图")
public class IntentRequest {
    @TableId(type = IdType.AUTO)
    @Schema(description = "点餐意图 ID", example = "1")
    private Long id;
    @Schema(description = "家庭 ID", example = "1")
    private Long familyId;
    @Schema(description = "用户 ID", example = "2")
    private Long userId;
    @Schema(description = "来源类型", example = "TEXT")
    private String sourceType;
    @Schema(description = "输入文本")
    private String inputText;
    @Schema(description = "来源 URL")
    private String sourceUrl;
    @Schema(description = "图片 URL")
    private String imageUrl;
    @Schema(description = "备注")
    private String note;
    @Schema(description = "处理状态", example = "PENDING")
    private String status;
    @Schema(description = "关联 AI 任务 ID", example = "10")
    private Long aiTaskId;
    @Schema(description = "创建时间", format = "date-time")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间", format = "date-time")
    private LocalDateTime updatedAt;
}
