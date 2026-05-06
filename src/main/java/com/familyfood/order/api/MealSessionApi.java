package com.familyfood.order.api;

import com.familyfood.common.context.ActorContext;
import com.familyfood.order.dto.MealSessionRequest;
import com.familyfood.order.entity.MealSession;
import java.util.List;

public interface MealSessionApi {
    List<MealSession> sessions(ActorContext actor);

    MealSession current(ActorContext actor);

    MealSession create(ActorContext actor, MealSessionRequest request);

    MealSession requireFamilySession(ActorContext actor, Long mealSessionId);
}
