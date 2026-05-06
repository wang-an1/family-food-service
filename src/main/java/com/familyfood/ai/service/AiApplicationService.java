package com.familyfood.ai.service;

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
import java.util.List;

public interface AiApplicationService {
    ParseLinkResponse parseLink(ParseLinkRequest request);

    RecommendationResponse recommend(RecommendationRequest request);

    List<AiTask> tasks(String status, String sourceType);

    AiTaskDetail detail(Long taskId);

    AiTask retry(Long taskId);

    List<AiExtractedDish> drafts(String reviewStatus);

    ConvertResponse convert(Long id, ConvertRequest request);

    AiProvider.AiMenuPlanResult menuPlan(MenuPlanRequest request);
}
