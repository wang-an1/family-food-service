package com.familyfood.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("ai_recommendation")
@Schema(description = "AI 推荐记录")
public class AiRecommendation {
    @TableId(type = IdType.AUTO)
    @Schema(description = "推荐记录 ID", example = "1")
    private Long id;
    @Schema(description = "AI 任务 ID", example = "10")
    private Long aiTaskId;
    @Schema(description = "家庭 ID", example = "1")
    private Long familyId;
    @Schema(description = "用户 ID", example = "2")
    private Long userId;
    @Schema(description = "推荐提示词")
    private String prompt;
    @Schema(description = "正式菜品 ID")
    private Long dishId;
    @Schema(description = "AI 抽取菜品草稿 ID")
    private Long extractedDishId;
    @Schema(description = "推荐标题", example = "番茄炒蛋")
    private String title;
    @Schema(description = "推荐理由")
    private String reason;
    @Schema(description = "推荐分数", example = "0.92")
    private BigDecimal score;
    @Schema(description = "创建时间", format = "date-time")
    private LocalDateTime createdAt;
}
