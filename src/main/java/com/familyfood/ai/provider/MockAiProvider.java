package com.familyfood.ai.provider;

import com.familyfood.ai.dto.RecommendationDto;
import com.familyfood.dish.entity.Dish;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MockAiProvider implements AiProvider {
    @Override
    public String modelName() {
        return "mock";
    }

    @Override
    public AiStructuredResult extractDishes(AiExtractionRequest request) {
        String text = String.join(" ", safe(request.title()), safe(request.description()), safe(request.contentText()), safe(request.fallbackText()));
        String name = text.contains("鸡翅") ? "空气炸锅鸡翅" : text.contains("虾") ? "虾仁蒸蛋" : "清淡家常菜";
        ExtractedDishDto dish = new ExtractedDishDto(
                name,
                List.of("AI生成", "快手菜", text.contains("孩子") ? "儿童可吃" : "清淡"),
                text.contains("辣") ? "微辣" : "清淡",
                List.of("LUNCH", "DINNER"),
                "EASY",
                name.contains("鸡翅") ? 25 : 18,
                name.contains("鸡翅")
                        ? List.of(new IngredientDto("鸡翅", 500, "克", "肉类", true), new IngredientDto("奥尔良腌料", 30, "克", "调料", false))
                        : List.of(new IngredientDto("鸡蛋", 3, "个", "蛋类", true), new IngredientDto("青菜", 300, "克", "蔬菜", true)),
                name.contains("鸡翅") ? "鸡翅划刀腌制后，空气炸锅 180 度烤 15 分钟，中途翻面。" : "食材处理后清炒或蒸制，少油调味。",
                "Mock AI 根据输入内容生成，仅供参考。",
                0.86
        );
        return new AiStructuredResult("识别到候选菜品：" + name, List.of(dish));
    }

    @Override
    public AiRecommendationResult recommend(AiRecommendationRequest request) {
        String prompt = safe(request.prompt());
        List<RecommendationDto> recommendations = request.candidateDishes().stream()
                .sorted(Comparator.comparing((CandidateDish d) -> score(prompt, d)).reversed())
                .limit(Math.max(3, request.maxResults()))
                .map(d -> new RecommendationDto("EXISTING_DISH", d.dishId(), d.name(), reason(prompt, d), score(prompt, d)))
                .toList();
        return new AiRecommendationResult(recommendations, recommendations.isEmpty() ? "暂无匹配菜品，可补充口味或食材关键词。" : null);
    }

    @Override
    public AiMenuPlanResult planMenu(AiMenuPlanRequest request) {
        List<MenuPlanItem> items = request.candidates().stream().limit(3)
                .map(d -> new MenuPlanItem(d.dishId(), d.name(), d.name().contains("汤") ? "汤羹" : "菜品", "匹配当前餐次和家庭常用菜。"))
                .toList();
        return new AiMenuPlanResult("家常" + request.mealType() + "搭配", items, "根据已选菜品检查鸡蛋、青菜、肉类是否充足。");
    }

    @Override
    public String summarize(AiSummarizeRequest request) {
        String title = safe(request.title());
        if (!title.isBlank()) {
            return "公开页面标题：" + title;
        }
        return safe(request.contentText()).length() > 80 ? request.contentText().substring(0, 80) : safe(request.contentText());
    }

    private double score(String prompt, CandidateDish dish) {
        double score = 0.72;
        String joined = (dish.name() + " " + dish.taste() + " " + String.join(" ", dish.tags())).toLowerCase();
        if (prompt.contains("清淡") && joined.contains("清淡")) score += 0.14;
        if (prompt.contains("孩子") && joined.contains("儿童")) score += 0.10;
        if (prompt.contains("快") && joined.contains("快手")) score += 0.06;
        if (prompt.contains("鸡") && joined.contains("鸡")) score += 0.05;
        return Math.min(0.98, score);
    }

    private String reason(String prompt, CandidateDish dish) {
        if (prompt.contains("孩子")) {
            return "口味温和，适合孩子，且制作难度低。";
        }
        if (prompt.contains("清淡")) {
            return "口味偏清淡，适合当前需求，制作时间可控。";
        }
        return "与家庭菜品库和当前餐次匹配度较高。";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
