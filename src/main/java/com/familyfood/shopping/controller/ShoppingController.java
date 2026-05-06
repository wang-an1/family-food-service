package com.familyfood.shopping.controller;

import com.familyfood.common.ApiResponse;
import com.familyfood.shopping.dto.CheckRequest;
import com.familyfood.shopping.dto.ItemRequest;
import com.familyfood.shopping.dto.ShoppingResponse;
import com.familyfood.shopping.entity.ShoppingListItem;
import com.familyfood.shopping.service.ShoppingApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "采购清单", description = "按餐次生成和维护采购清单")
@SecurityRequirement(name = "bearerAuth")
public class ShoppingController {
    private final ShoppingApplicationService shoppingService;

    @Autowired
    public ShoppingController(ShoppingApplicationService shoppingService) {
        this.shoppingService = shoppingService;
    }

    @PostMapping("/shopping-lists/{mealSessionId}/generate")
    @Operation(summary = "生成采购清单", description = "根据指定餐次订单生成采购清单")
    public ApiResponse<ShoppingResponse> generate(
            @Parameter(description = "餐次 ID", required = true) @PathVariable Long mealSessionId) {
        return ApiResponse.ok(shoppingService.generate(mealSessionId));
    }

    @GetMapping("/shopping-lists/{mealSessionId}")
    @Operation(summary = "获取采购清单", description = "返回指定餐次的采购清单及条目")
    public ApiResponse<ShoppingResponse> get(
            @Parameter(description = "餐次 ID", required = true) @PathVariable Long mealSessionId) {
        return ApiResponse.ok(shoppingService.get(mealSessionId));
    }

    @PutMapping("/shopping-list-items/{id}")
    @Operation(summary = "更新采购条目", description = "更新采购条目的名称、数量、单位、分类和备注")
    public ApiResponse<ShoppingListItem> update(@Parameter(description = "采购条目 ID", required = true) @PathVariable Long id,
                                                @Valid @RequestBody ItemRequest request) {
        return ApiResponse.ok(shoppingService.updateItem(id, request));
    }

    @PostMapping("/shopping-list-items/{id}/check")
    @Operation(summary = "勾选采购条目", description = "标记采购条目是否已购买")
    public ApiResponse<ShoppingListItem> check(@Parameter(description = "采购条目 ID", required = true) @PathVariable Long id,
                                               @Valid @RequestBody CheckRequest request) {
        return ApiResponse.ok(shoppingService.check(id, request.checked()));
    }
}
