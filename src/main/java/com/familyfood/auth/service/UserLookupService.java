package com.familyfood.auth.service;

import com.familyfood.auth.api.UserLookupApi;
import com.familyfood.auth.entity.User;

public interface UserLookupService extends UserLookupApi {
    User getById(Long userId);
}
