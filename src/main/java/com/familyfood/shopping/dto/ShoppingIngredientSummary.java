package com.familyfood.shopping.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ShoppingIngredientSummary {
    private String name;
    private BigDecimal amount;
    private String unit;
    private String category;
    private String sourceDishIds;
}
