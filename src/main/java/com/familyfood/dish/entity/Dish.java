package com.familyfood.dish.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("dish")
@Schema(description = "菜品")
public class Dish {
    @TableId(type = IdType.AUTO)
    @Schema(description = "菜品 ID", example = "1")
    private Long id;
    @Schema(description = "家庭 ID", example = "1")
    private Long familyId;
    @Schema(description = "分类 ID", example = "1")
    private Long categoryId;
    @Schema(description = "菜品名称", example = "番茄炒蛋")
    private String name;
    @Schema(description = "菜品别名")
    private String aliases;
    @Schema(description = "菜品描述")
    private String description;
    @Schema(description = "菜品图片 URL")
    private String imageUrl;
    @Schema(description = "口味", example = "咸鲜")
    private String taste;
    @Schema(description = "适用餐次，逗号或 JSON 存储")
    private String mealTypes;
    @Schema(description = "制作难度", example = "EASY")
    private String difficulty;
    @Schema(description = "预计制作分钟数", example = "15")
    private Integer estimatedMinutes;
    @Schema(description = "默认份数", example = "2")
    private BigDecimal defaultServings;
    @Schema(description = "制作步骤")
    private String instructions;
    @Schema(description = "来源类型", example = "MANUAL")
    private String sourceType;
    @Schema(description = "来源 URL")
    private String sourceUrl;
    @Schema(description = "菜品状态", example = "ACTIVE")
    private String status;
    @Schema(description = "创建时间", format = "date-time")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间", format = "date-time")
    private LocalDateTime updatedAt;
    @Schema(description = "创建人用户 ID", example = "1")
    private Long createdBy;
    @Schema(description = "更新人用户 ID", example = "1")
    private Long updatedBy;
    @Schema(hidden = true)
    private Integer deleted;
}
