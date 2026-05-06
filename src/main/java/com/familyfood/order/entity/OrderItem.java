package com.familyfood.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("order_item")
@Schema(description = "订单菜品项")
public class OrderItem {
    @TableId(type = IdType.AUTO)
    @Schema(description = "订单菜品项 ID", example = "1")
    private Long id;
    @Schema(description = "订单 ID", example = "1")
    private Long orderId;
    @Schema(description = "菜品 ID", example = "1")
    private Long dishId;
    @Schema(description = "下单时菜品名称快照", example = "番茄炒蛋")
    private String dishNameSnapshot;
    @Schema(description = "数量", example = "1")
    private BigDecimal quantity;
    @Schema(description = "单位", example = "份")
    private String unit;
    @Schema(description = "备注")
    private String note;
}
