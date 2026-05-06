package com.familyfood.dish.controller;

import com.familyfood.common.ApiResponse;
import com.familyfood.dish.dto.DishRequest;
import com.familyfood.dish.dto.DishResponse;
import com.familyfood.dish.dto.NameRequest;
import com.familyfood.dish.dto.StatusRequest;
import com.familyfood.dish.dto.TagRequest;
import com.familyfood.dish.entity.Dish;
import com.familyfood.dish.entity.DishCategory;
import com.familyfood.dish.entity.DishTag;
import com.familyfood.dish.service.DishApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "菜品管理", description = "菜品、菜品分类和菜品标签管理")
@SecurityRequirement(name = "bearerAuth")
public class DishController {
    private final DishApplicationService dishService;

    public DishController(DishApplicationService dishService) {
        this.dishService = dishService;
    }

    @GetMapping("/dishes")
    @Operation(summary = "查询菜品列表", description = "按关键字、分类、标签或状态筛选当前家庭菜品")
    public ApiResponse<List<DishResponse>> list(
            @Parameter(description = "菜品名称、别名或描述关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类 ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "标签 ID") @RequestParam(required = false) Long tagId,
            @Parameter(description = "菜品状态", example = "ACTIVE") @RequestParam(required = false) String status) {
        return ApiResponse.ok(dishService.list(keyword, categoryId, tagId, status));
    }

    @GetMapping("/dishes/{id}")
    @Operation(summary = "获取菜品详情", description = "返回菜品基础信息、标签、适用餐次和食材明细")
    public ApiResponse<DishResponse> get(@Parameter(description = "菜品 ID", required = true) @PathVariable Long id) {
        return ApiResponse.ok(dishService.get(id));
    }

    @PostMapping("/dishes")
    @Operation(summary = "创建菜品", description = "创建当前家庭可点餐的菜品")
    public ApiResponse<DishResponse> create(@Valid @RequestBody DishRequest request) {
        return ApiResponse.ok(dishService.create(request));
    }

    @PutMapping("/dishes/{id}")
    @Operation(summary = "更新菜品", description = "更新指定菜品的基础信息、标签和食材")
    public ApiResponse<DishResponse> update(@Parameter(description = "菜品 ID", required = true) @PathVariable Long id,
                                            @Valid @RequestBody DishRequest request) {
        return ApiResponse.ok(dishService.update(id, request));
    }

    @PutMapping("/dishes/{id}/status")
    @Operation(summary = "更新菜品状态", description = "启用、停用或归档指定菜品")
    public ApiResponse<DishResponse> status(@Parameter(description = "菜品 ID", required = true) @PathVariable Long id,
                                            @Valid @RequestBody StatusRequest request) {
        return ApiResponse.ok(dishService.updateStatus(id, request.status()));
    }

    @DeleteMapping("/dishes/{id}")
    @Operation(summary = "删除菜品", description = "软删除指定菜品")
    public ApiResponse<Void> delete(@Parameter(description = "菜品 ID", required = true) @PathVariable Long id) {
        dishService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/dish-categories")
    @Operation(summary = "查询菜品分类", description = "返回当前家庭的菜品分类列表")
    public ApiResponse<List<DishCategory>> categories() {
        return ApiResponse.ok(dishService.categories());
    }

    @PostMapping("/dish-categories")
    @Operation(summary = "创建菜品分类", description = "新增当前家庭的菜品分类")
    public ApiResponse<DishCategory> createCategory(@Valid @RequestBody NameRequest request) {
        return ApiResponse.ok(dishService.createCategory(request.name()));
    }

    @GetMapping("/dish-tags")
    @Operation(summary = "查询菜品标签", description = "返回当前家庭的菜品标签列表")
    public ApiResponse<List<DishTag>> tags() {
        return ApiResponse.ok(dishService.tags());
    }

    @PostMapping("/dish-tags")
    @Operation(summary = "创建菜品标签", description = "新增当前家庭的菜品标签")
    public ApiResponse<DishTag> createTag(@Valid @RequestBody TagRequest request) {
        return ApiResponse.ok(dishService.createTag(request));
    }
}
