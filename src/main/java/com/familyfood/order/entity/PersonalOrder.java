package com.familyfood.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("personal_order")
@Schema(description = "个人订单")
public class PersonalOrder {
    @TableId(type = IdType.AUTO)
    @Schema(description = "订单 ID", example = "1")
    private Long id;
    @Schema(description = "家庭 ID", example = "1")
    private Long familyId;
    @Schema(description = "餐次 ID", example = "1")
    private Long mealSessionId;
    @Schema(description = "用户 ID", example = "2")
    private Long userId;
    @Schema(description = "订单状态", example = "SUBMITTED")
    private String status;
    @Schema(description = "订单备注")
    private String note;
    @Schema(description = "忌口说明")
    private String avoidances;
    @Schema(description = "期望时间", format = "date-time")
    private LocalDateTime expectedTime;
    @Schema(description = "提交时间", format = "date-time")
    private LocalDateTime submittedAt;
    @Schema(description = "确认时间", format = "date-time")
    private LocalDateTime confirmedAt;
    @Schema(description = "确认人用户 ID", example = "1")
    private Long confirmedBy;
    @Schema(description = "创建时间", format = "date-time")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间", format = "date-time")
    private LocalDateTime updatedAt;
    @Schema(hidden = true)
    private Integer deleted;
}
