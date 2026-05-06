package com.familyfood.dish.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("dish_tag")
@Schema(description = "菜品标签")
public class DishTag {
    @TableId(type = IdType.AUTO)
    @Schema(description = "标签 ID", example = "1")
    private Long id;
    @Schema(description = "家庭 ID", example = "1")
    private Long familyId;
    @Schema(description = "标签名称", example = "少油")
    private String name;
    @Schema(description = "标签颜色", example = "#22c55e")
    private String color;
}
