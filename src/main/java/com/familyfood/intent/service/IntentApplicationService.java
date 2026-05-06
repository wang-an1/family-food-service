package com.familyfood.intent.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.familyfood.ai.api.AiTaskApi;
import com.familyfood.ai.dto.ParseLinkRequest;
import com.familyfood.ai.dto.RecommendationRequest;
import com.familyfood.ai.support.SourceTypeDetector;
import com.familyfood.common.AppException;
import com.familyfood.common.Enums.SourceType;
import com.familyfood.common.context.ActorContext;
import com.familyfood.common.context.ActorContextProvider;
import com.familyfood.intent.dao.IntentRequestMapper;
import com.familyfood.intent.dto.IntentResponse;
import com.familyfood.intent.dto.IntentSubmitRequest;
import com.familyfood.intent.entity.IntentRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IntentApplicationService {
    private final ActorContextProvider actorProvider;
    private final IntentRequestMapper intentMapper;
    private final AiTaskApi aiTaskApi;
    private final SourceTypeDetector detector;

    public IntentApplicationService(ActorContextProvider actorProvider, IntentRequestMapper intentMapper,
                                    AiTaskApi aiTaskApi, SourceTypeDetector detector) {
        this.actorProvider = actorProvider;
        this.intentMapper = intentMapper;
        this.aiTaskApi = aiTaskApi;
        this.detector = detector;
    }

    @Transactional
    public IntentResponse submit(IntentSubmitRequest request) {
        ActorContext actor = actorProvider.current();
        if (blank(request.inputText()) && blank(request.sourceUrl()) && blank(request.imageUrl())) {
            throw AppException.validation("inputText, sourceUrl, or imageUrl is required");
        }
        SourceType sourceType = detector.detect(request.sourceUrl(), request.inputText(), request.imageUrl());
        Long taskId;
        if (!blank(request.sourceUrl()) || sourceType == SourceType.IMAGE) {
            taskId = aiTaskApi.parseLink(actor,
                    new ParseLinkRequest(request.sourceUrl(), request.inputText(), request.imageUrl())).taskId();
        } else {
            taskId = aiTaskApi.recommend(actor,
                    new RecommendationRequest(request.inputText(), "DINNER", 6)).taskId();
        }
        LocalDateTime now = LocalDateTime.now();
        IntentRequest intent = new IntentRequest();
        intent.setFamilyId(actor.familyId());
        intent.setUserId(actor.userId());
        intent.setSourceType(sourceType.name());
        intent.setInputText(request.inputText());
        intent.setSourceUrl(request.sourceUrl());
        intent.setImageUrl(request.imageUrl());
        intent.setNote(request.note());
        intent.setStatus("PROCESSING");
        intent.setAiTaskId(taskId);
        intent.setCreatedAt(now);
        intent.setUpdatedAt(now);
        intentMapper.insert(intent);
        return new IntentResponse(intent.getId(), intent.getStatus(), taskId);
    }

    public List<IntentRequest> my() {
        ActorContext actor = actorProvider.current();
        return intentMapper.selectList(new QueryWrapper<IntentRequest>()
                .eq("family_id", actor.familyId())
                .eq("user_id", actor.userId())
                .orderByDesc("created_at"));
    }

    public IntentRequest detail(Long id) {
        ActorContext actor = actorProvider.current();
        IntentRequest intent = intentMapper.selectById(id);
        if (intent == null || !Objects.equals(intent.getFamilyId(), actor.familyId())) {
            throw AppException.notFound("Intent not found");
        }
        if (!actor.admin() && !Objects.equals(intent.getUserId(), actor.userId())) {
            throw AppException.forbidden("No permission to access this intent");
        }
        return intent;
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
