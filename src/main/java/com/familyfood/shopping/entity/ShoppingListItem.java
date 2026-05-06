package com.familyfood.shopping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("shopping_list_item")
@Schema(description = "采购清单条目")
public class ShoppingListItem {
    @TableId(type = IdType.AUTO)
    @Schema(description = "采购条目 ID", example = "1")
    private Long id;
    @Schema(description = "采购清单 ID", example = "1")
    private Long shoppingListId;
    @Schema(description = "采购项名称", example = "鸡蛋")
    private String name;
    @Schema(description = "采购数量", example = "6")
    private BigDecimal amount;
    @Schema(description = "单位", example = "个")
    private String unit;
    @Schema(description = "分类", example = "蛋奶")
    private String category;
    @Schema(description = "是否已购买，1 表示已购买", example = "0")
    private Integer checked;
    @Schema(description = "来源", example = "ORDER")
    private String source;
    @Schema(description = "来源菜品 ID 列表")
    private String sourceDishIds;
    @Schema(description = "备注")
    private String note;
    @Schema(description = "创建时间", format = "date-time")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间", format = "date-time")
    private LocalDateTime updatedAt;
}
