package com.familyfood.shopping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.familyfood.shopping.entity.ShoppingListItem;
import java.util.List;

@Schema(description = "采购清单响应")
public record ShoppingResponse(
        @Schema(description = "采购清单 ID", example = "1")
        Long id,
        @Schema(description = "餐次 ID", example = "1")
        Long mealSessionId,
        @Schema(description = "采购清单标题", example = "周三晚餐采购清单")
        String title,
        @Schema(description = "采购条目列表")
        List<ShoppingListItem> items) {
}
