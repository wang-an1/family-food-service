package com.familyfood.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("order_status_log")
@Schema(description = "订单状态变更日志")
public class OrderStatusLog {
    @TableId(type = IdType.AUTO)
    @Schema(description = "日志 ID", example = "1")
    private Long id;
    @Schema(description = "订单 ID", example = "1")
    private Long orderId;
    @Schema(description = "变更前状态", example = "SUBMITTED")
    private String fromStatus;
    @Schema(description = "变更后状态", example = "CONFIRMED")
    private String toStatus;
    @Schema(description = "操作人用户 ID", example = "1")
    private Long operatorId;
    @Schema(description = "变更原因")
    private String reason;
    @Schema(description = "创建时间", format = "date-time")
    private LocalDateTime createdAt;
}
