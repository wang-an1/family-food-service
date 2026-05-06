package com.familyfood.auth.service;

import com.familyfood.auth.security.UserPrincipal;

public interface JwtService {
    String generate(UserPrincipal principal);

    UserPrincipal parse(String token);
}
