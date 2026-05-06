package com.familyfood.dish.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("dish_ingredient")
@Schema(description = "菜品食材")
public class DishIngredient {
    @TableId(type = IdType.AUTO)
    @Schema(description = "食材 ID", example = "1")
    private Long id;
    @Schema(description = "菜品 ID", example = "1")
    private Long dishId;
    @Schema(description = "食材名称", example = "鸡蛋")
    private String name;
    @Schema(description = "数量", example = "2")
    private BigDecimal amount;
    @Schema(description = "单位", example = "个")
    private String unit;
    @Schema(description = "分类", example = "蛋奶")
    private String category;
    @Schema(description = "是否必需，1 表示必需", example = "1")
    private Integer required;
    @Schema(description = "备注")
    private String note;
}
