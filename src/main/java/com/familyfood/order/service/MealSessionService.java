package com.familyfood.order.service;

import com.familyfood.common.context.ActorContext;
import com.familyfood.order.api.MealSessionApi;
import com.familyfood.order.dto.MealSessionRequest;
import com.familyfood.order.entity.MealSession;
import java.util.List;

public interface MealSessionService extends MealSessionApi {
    List<MealSession> sessions(ActorContext actor);

    MealSession current(ActorContext actor);

    MealSession create(ActorContext actor, MealSessionRequest request);

    MealSession requireFamilySession(ActorContext actor, Long mealSessionId);
}
