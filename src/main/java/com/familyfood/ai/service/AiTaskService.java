package com.familyfood.ai.service;

import com.familyfood.ai.api.AiTaskApi;
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
import com.familyfood.common.context.ActorContext;
import java.util.List;

public interface AiTaskService extends AiTaskApi {
    ParseLinkResponse parseLink(ActorContext actor, ParseLinkRequest request);

    RecommendationResponse recommend(ActorContext actor, RecommendationRequest request);

    AiTaskDetail detail(ActorContext actor, Long taskId);

    List<AiTask> tasks(ActorContext actor, String status, String sourceType);

    List<AiExtractedDish> drafts(ActorContext actor, String reviewStatus);

    AiTask retry(ActorContext actor, Long taskId);

    ConvertResponse convert(ActorContext actor, Long id, ConvertRequest request);

    AiProvider.AiMenuPlanResult menuPlan(ActorContext actor, MenuPlanRequest request);
}
