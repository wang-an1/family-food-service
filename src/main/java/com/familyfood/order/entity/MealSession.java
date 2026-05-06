package com.familyfood.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("meal_session")
@Schema(description = "餐次")
public class MealSession {
    @TableId(type = IdType.AUTO)
    @Schema(description = "餐次 ID", example = "1")
    private Long id;
    @Schema(description = "家庭 ID", example = "1")
    private Long familyId;
    @Schema(description = "餐次标题", example = "周三晚餐")
    private String title;
    @Schema(description = "餐次类型", example = "DINNER")
    private String mealType;
    @Schema(description = "就餐日期", format = "date")
    private LocalDate mealDate;
    @Schema(description = "期望就餐时间", format = "date-time")
    private LocalDateTime expectedTime;
    @Schema(description = "餐次状态", example = "OPEN")
    private String status;
    @Schema(description = "是否需要确认，1 表示需要", example = "1")
    private Integer confirmRequired;
    @Schema(description = "创建时间", format = "date-time")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间", format = "date-time")
    private LocalDateTime updatedAt;
    @Schema(description = "创建人用户 ID", example = "1")
    private Long createdBy;
}
