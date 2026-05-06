package com.familyfood.order.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class DishQuantitySummary {
    private String dishNameSnapshot;
    private BigDecimal amount;
}
