package com.familyfood.intent.controller;

import com.familyfood.common.ApiResponse;
import com.familyfood.intent.dto.IntentResponse;
import com.familyfood.intent.dto.IntentSubmitRequest;
import com.familyfood.intent.entity.IntentRequest;
import com.familyfood.intent.service.IntentApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/intents")
@Tag(name = "点餐意图", description = "用户点餐意图提交与查询")
@SecurityRequirement(name = "bearerAuth")
public class IntentController {
    private final IntentApplicationService intentService;

    @Autowired
    public IntentController(IntentApplicationService intentService) {
        this.intentService = intentService;
    }

    @PostMapping
    @Operation(summary = "提交点餐意图", description = "提交文本、链接或图片形式的点餐意图")
    public ApiResponse<IntentResponse> submit(@Valid @RequestBody IntentSubmitRequest request) {
        return ApiResponse.ok(intentService.submit(request));
    }

    @GetMapping("/my")
    @Operation(summary = "查询我的点餐意图", description = "返回当前用户提交过的点餐意图")
    public ApiResponse<List<IntentRequest>> my() {
        return ApiResponse.ok(intentService.my());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取点餐意图详情", description = "返回指定点餐意图详情")
    public ApiResponse<IntentRequest> detail(@Parameter(description = "点餐意图 ID", required = true) @PathVariable Long id) {
        return ApiResponse.ok(intentService.detail(id));
    }
}
