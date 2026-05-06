package com.familyfood.auth.service.impl;

import com.familyfood.auth.dao.UserMapper;
import com.familyfood.auth.entity.User;
import com.familyfood.auth.service.UserLookupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserLookupServiceImpl implements UserLookupService {
    private final UserMapper userMapper;

    @Autowired
    public UserLookupServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User getById(Long userId) {
        return userMapper.selectById(userId);
    }
}
