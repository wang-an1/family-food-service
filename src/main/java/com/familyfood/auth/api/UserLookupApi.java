package com.familyfood.auth.api;

import com.familyfood.auth.entity.User;

public interface UserLookupApi {
    User getById(Long userId);
}
