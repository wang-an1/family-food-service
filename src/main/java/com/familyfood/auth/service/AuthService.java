package com.familyfood.auth.service;

import com.familyfood.auth.dto.LoginRequest;
import com.familyfood.auth.dto.LoginResponse;
import com.familyfood.auth.dto.MeResponse;
import com.familyfood.common.context.ActorContext;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    MeResponse me(ActorContext actor);
}
