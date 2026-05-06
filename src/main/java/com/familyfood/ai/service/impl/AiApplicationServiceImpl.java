package com.familyfood.ai.service.impl;

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
import com.familyfood.ai.service.AiTaskService;
import com.familyfood.common.context.ActorContextProvider;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AiApplicationServiceImpl implements AiApplicationService {
    private final ActorContextProvider actorProvider;
    private final AiTaskService aiTaskService;

    @Autowired
    public AiApplicationServiceImpl(ActorContextProvider actorProvider, AiTaskService aiTaskService) {
        this.actorProvider = actorProvider;
        this.aiTaskService = aiTaskService;
    }

    public ParseLinkResponse parseLink(ParseLinkRequest request) {
        return aiTaskService.parseLink(actorProvider.current(), request);
    }

    public RecommendationResponse recommend(RecommendationRequest request) {
        return aiTaskService.recommend(actorProvider.current(), request);
    }

    public List<AiTask> tasks(String status, String sourceType) {
        return aiTaskService.tasks(actorProvider.current(), status, sourceType);
    }

    public AiTaskDetail detail(Long taskId) {
        return aiTaskService.detail(actorProvider.current(), taskId);
    }

    public AiTask retry(Long taskId) {
        return aiTaskService.retry(actorProvider.current(), taskId);
    }

    public List<AiExtractedDish> drafts(String reviewStatus) {
        return aiTaskService.drafts(actorProvider.current(), reviewStatus);
    }

    public ConvertResponse convert(Long id, ConvertRequest request) {
        return aiTaskService.convert(actorProvider.current(), id, request);
    }

    public AiProvider.AiMenuPlanResult menuPlan(MenuPlanRequest request) {
        return aiTaskService.menuPlan(actorProvider.current(), request);
    }
}
