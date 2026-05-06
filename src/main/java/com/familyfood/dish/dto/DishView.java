package com.familyfood.dish.dto;

import com.familyfood.dish.entity.Dish;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DishView extends Dish {
    private String categoryName;
}
