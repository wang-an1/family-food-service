package com.familyfood.order.dto;

import com.familyfood.order.entity.PersonalOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderView extends PersonalOrder {
    private String userNickname;
}
