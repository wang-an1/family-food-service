package com.familyfood.auth.service;

import com.familyfood.auth.api.UserLookupApi;
import com.familyfood.auth.dao.UserMapper;
import com.familyfood.auth.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserLookupService implements UserLookupApi {
    private final UserMapper userMapper;

    public UserLookupService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User getById(Long userId) {
        return userMapper.selectById(userId);
    }
}
