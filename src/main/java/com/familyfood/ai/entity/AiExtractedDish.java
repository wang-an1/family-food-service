package com.familyfood.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("ai_extracted_dish")
@Schema(description = "AI 抽取菜品草稿")
public class AiExtractedDish {
    @TableId(type = IdType.AUTO)
    @Schema(description = "草稿 ID", example = "1")
    private Long id;
    @Schema(description = "AI 任务 ID", example = "10")
    private Long aiTaskId;
    @Schema(description = "家庭 ID", example = "1")
    private Long familyId;
    @Schema(description = "菜品名称", example = "番茄炒蛋")
    private String name;
    @Schema(description = "菜品别名")
    private String aliases;
    @Schema(description = "分类名称", example = "家常菜")
    private String categoryName;
    @Schema(description = "标签 JSON")
    private String tagsJson;
    @Schema(description = "口味", example = "咸鲜")
    private String taste;
    @Schema(description = "适用餐次 JSON")
    private String mealTypesJson;
    @Schema(description = "制作难度", example = "EASY")
    private String difficulty;
    @Schema(description = "预计制作分钟数", example = "15")
    private Integer estimatedMinutes;
    @Schema(description = "食材 JSON")
    private String ingredientsJson;
    @Schema(description = "制作步骤")
    private String instructions;
    @Schema(description = "推荐理由")
    private String recommendationReason;
    @Schema(description = "AI 置信度", example = "0.86")
    private BigDecimal confidence;
    @Schema(description = "匹配到的已有菜品 ID", example = "2")
    private Long matchDishId;
    @Schema(description = "匹配分数", example = "0.93")
    private BigDecimal matchScore;
    @Schema(description = "审核状态", example = "PENDING")
    private String reviewStatus;
    @Schema(description = "转换后的正式菜品 ID", example = "3")
    private Long convertedDishId;
    @Schema(description = "创建时间", format = "date-time")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间", format = "date-time")
    private LocalDateTime updatedAt;
}
