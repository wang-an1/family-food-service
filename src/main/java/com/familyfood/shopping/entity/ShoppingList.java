package com.familyfood.shopping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("shopping_list")
@Schema(description = "采购清单")
public class ShoppingList {
    @TableId(type = IdType.AUTO)
    @Schema(description = "采购清单 ID", example = "1")
    private Long id;
    @Schema(description = "家庭 ID", example = "1")
    private Long familyId;
    @Schema(description = "餐次 ID", example = "1")
    private Long mealSessionId;
    @Schema(description = "采购清单标题", example = "周三晚餐采购清单")
    private String title;
    @Schema(description = "采购清单状态", example = "ACTIVE")
    private String status;
    @Schema(description = "是否由 AI 生成，1 表示是", example = "0")
    private Integer generatedByAi;
    @Schema(description = "创建时间", format = "date-time")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间", format = "date-time")
    private LocalDateTime updatedAt;
}
