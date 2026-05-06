package com.familyfood.ai.controller;

import com.familyfood.ai.dto.AiTaskDetail;
import com.familyfood.ai.dto.ConvertRequest;
import com.familyfood.ai.dto.ConvertResponse;
import com.familyfood.ai.dto.MenuPlanRequest;
import com.familyfood.ai.dto.ParseLinkRequest;
import com.familyfood.ai.dto.ParseLinkResponse;
import com.familyfood.ai.dto.RecommendationRequest;
import com.familyfood.ai.dto.RecommendationResponse;
import com.familyfood.ai.entity.AiExtractedDish;
import com.familyfood.ai.entity.AiTask;
import com.familyfood.ai.provider.AiProvider;
import com.familyfood.ai.service.AiApplicationService;
import com.familyfood.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI 助手", description = "链接解析、菜品推荐、AI 任务和菜品草稿转换")
@SecurityRequirement(name = "bearerAuth")
public class AiController {
    private final AiApplicationService aiService;

    @Autowired
    public AiController(AiApplicationService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/parse-link")
    @Operation(summary = "解析链接或素材", description = "提交链接、文本或图片地址，创建 AI 菜品解析任务")
    public ApiResponse<ParseLinkResponse> parseLink(@Valid @RequestBody ParseLinkRequest request) {
        return ApiResponse.ok(aiService.parseLink(request));
    }

    @PostMapping("/recommendations")
    @Operation(summary = "生成菜品推荐", description = "根据用户口味、餐次和文本要求生成推荐菜品")
    public ApiResponse<RecommendationResponse> recommendations(@Valid @RequestBody RecommendationRequest request) {
        return ApiResponse.ok(aiService.recommend(request));
    }

    @GetMapping("/tasks")
    @Operation(summary = "查询 AI 任务", description = "按任务状态或来源类型筛选 AI 任务")
    public ApiResponse<List<AiTask>> tasks(
            @Parameter(description = "任务状态", example = "SUCCESS") @RequestParam(required = false) String status,
            @Parameter(description = "来源类型", example = "LINK") @RequestParam(required = false) String sourceType) {
        return ApiResponse.ok(aiService.tasks(status, sourceType));
    }

    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "获取 AI 任务详情", description = "返回任务、来源内容、抽取菜品和推荐结果")
    public ApiResponse<AiTaskDetail> detail(@Parameter(description = "AI 任务 ID", required = true) @PathVariable Long taskId) {
        return ApiResponse.ok(aiService.detail(taskId));
    }

    @PostMapping("/tasks/{taskId}/retry")
    @Operation(summary = "重试 AI 任务", description = "重新执行失败的 AI 任务")
    public ApiResponse<AiTask> retry(@Parameter(description = "AI 任务 ID", required = true) @PathVariable Long taskId) {
        return ApiResponse.ok(aiService.retry(taskId));
    }

    @GetMapping("/extracted-dishes")
    @Operation(summary = "查询 AI 菜品草稿", description = "按审核状态查询 AI 抽取出的菜品草稿")
    public ApiResponse<List<AiExtractedDish>> drafts(
            @Parameter(description = "审核状态", example = "PENDING") @RequestParam(required = false) String reviewStatus) {
        return ApiResponse.ok(aiService.drafts(reviewStatus));
    }

    @PostMapping("/extracted-dishes/{id}/convert")
    @Operation(summary = "转换 AI 菜品草稿", description = "将 AI 抽取出的菜品草稿转换为正式菜品或更新已有菜品")
    public ApiResponse<ConvertResponse> convert(@Parameter(description = "AI 菜品草稿 ID", required = true) @PathVariable Long id,
                                                @Valid @RequestBody ConvertRequest request) {
        return ApiResponse.ok(aiService.convert(id, request));
    }

    @PostMapping("/menu-plan")
    @Operation(summary = "生成菜单计划", description = "根据文本要求、餐次、人数和偏好菜品生成菜单建议")
    public ApiResponse<AiProvider.AiMenuPlanResult> menuPlan(@Valid @RequestBody MenuPlanRequest request) {
        return ApiResponse.ok(aiService.menuPlan(request));
    }
}
