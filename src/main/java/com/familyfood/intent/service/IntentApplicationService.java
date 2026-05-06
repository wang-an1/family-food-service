package com.familyfood.intent.service;

import com.familyfood.intent.dto.IntentResponse;
import com.familyfood.intent.dto.IntentSubmitRequest;
import com.familyfood.intent.entity.IntentRequest;
import java.util.List;

public interface IntentApplicationService {
    IntentResponse submit(IntentSubmitRequest request);

    List<IntentRequest> my();

    IntentRequest detail(Long id);
}
