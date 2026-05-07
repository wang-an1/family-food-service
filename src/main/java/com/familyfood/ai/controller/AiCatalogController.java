package com.familyfood.ai.controller;

import com.familyfood.ai.dto.AiModelCatalogResponse;
import com.familyfood.ai.dto.AiModelRequest;
import com.familyfood.ai.dto.AiProviderCatalogResponse;
import com.familyfood.ai.dto.AiProviderCreateRequest;
import com.familyfood.ai.dto.AiProviderUpdateRequest;
import com.familyfood.ai.service.AiCatalogService;
import com.familyfood.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-catalog")
@Tag(name = "AI 模型目录", description = "全系统 AI 供应商和模型目录维护")
@SecurityRequirement(name = "bearerAuth")
public class AiCatalogController {
    private final AiCatalogService catalogService;

    @Autowired
    public AiCatalogController(AiCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/providers")
    @Operation(summary = "查询 AI 供应商目录", description = "返回供应商及其模型清单")
    public ApiResponse<List<AiProviderCatalogResponse>> providers() {
        return ApiResponse.ok(catalogService.listProviders());
    }

    @PostMapping("/providers")
    @Operation(summary = "创建 AI 供应商", description = "新增全系统可选供应商")
    public ApiResponse<AiProviderCatalogResponse> createProvider(@Valid @RequestBody AiProviderCreateRequest request) {
        return ApiResponse.ok(catalogService.createProvider(request));
    }

    @PutMapping("/providers/{id}")
    @Operation(summary = "更新 AI 供应商", description = "维护供应商展示名、调用类型、Base URL、状态和排序；供应商编码不可修改")
    public ApiResponse<AiProviderCatalogResponse> updateProvider(
            @Parameter(description = "供应商 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody AiProviderUpdateRequest request) {
        return ApiResponse.ok(catalogService.updateProvider(id, request));
    }

    @PostMapping("/providers/{id}/models")
    @Operation(summary = "创建 AI 模型", description = "在指定供应商下新增模型")
    public ApiResponse<AiModelCatalogResponse> createModel(
            @Parameter(description = "供应商 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody AiModelRequest request) {
        return ApiResponse.ok(catalogService.createModel(id, request));
    }

    @PutMapping("/models/{id}")
    @Operation(summary = "更新 AI 模型", description = "维护模型名称、展示名、默认状态、启停状态和排序")
    public ApiResponse<AiModelCatalogResponse> updateModel(
            @Parameter(description = "模型 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody AiModelRequest request) {
        return ApiResponse.ok(catalogService.updateModel(id, request));
    }
}
