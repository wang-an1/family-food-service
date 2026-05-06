package com.familyfood.ai.api;

import com.familyfood.ai.dto.AiTaskDetail;
import com.familyfood.ai.dto.ParseLinkRequest;
import com.familyfood.ai.dto.ParseLinkResponse;
import com.familyfood.ai.dto.RecommendationRequest;
import com.familyfood.ai.dto.RecommendationResponse;
import com.familyfood.common.context.ActorContext;

public interface AiTaskApi {
    ParseLinkResponse parseLink(ActorContext actor, ParseLinkRequest request);

    RecommendationResponse recommend(ActorContext actor, RecommendationRequest request);

    AiTaskDetail detail(ActorContext actor, Long taskId);
}
