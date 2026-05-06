package com.familyfood.dish.dto;

import com.familyfood.dish.entity.DishTag;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DishTagView extends DishTag {
    private Long dishId;
}
