package com.familyfood.dish.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("dish_tag_relation")
@Schema(description = "菜品标签关联")
public class DishTagRelation {
    @TableId(type = IdType.AUTO)
    @Schema(description = "关联 ID", example = "1")
    private Long id;
    @Schema(description = "菜品 ID", example = "1")
    private Long dishId;
    @Schema(description = "标签 ID", example = "2")
    private Long tagId;
}
