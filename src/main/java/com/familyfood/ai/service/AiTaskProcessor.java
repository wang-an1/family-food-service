package com.familyfood.ai.service;

import com.familyfood.ai.dto.ParseLinkRequest;

public interface AiTaskProcessor {
    void processParseTask(Long taskId, ParseLinkRequest request);

    void markReviewRequired(Long taskId, String code, String message, String url, String fallbackText);

    void markFailed(Long taskId, String code, String message);
}
