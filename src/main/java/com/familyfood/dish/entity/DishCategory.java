package com.familyfood.dish.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("dish_category")
@Schema(description = "菜品分类")
public class DishCategory {
    @TableId(type = IdType.AUTO)
    @Schema(description = "分类 ID", example = "1")
    private Long id;
    @Schema(description = "家庭 ID", example = "1")
    private Long familyId;
    @Schema(description = "分类名称", example = "家常菜")
    private String name;
    @Schema(description = "排序值", example = "10")
    private Integer sortOrder;
    @Schema(description = "创建时间", format = "date-time")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间", format = "date-time")
    private LocalDateTime updatedAt;
}
