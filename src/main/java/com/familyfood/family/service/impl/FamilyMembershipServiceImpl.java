package com.familyfood.family.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.familyfood.family.dao.FamilyMemberMapper;
import com.familyfood.family.entity.FamilyMember;
import com.familyfood.family.service.FamilyMembershipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FamilyMembershipServiceImpl implements FamilyMembershipService {
    private final FamilyMemberMapper memberMapper;

    @Autowired
    public FamilyMembershipServiceImpl(FamilyMemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    @Override
    public FamilyMember activeMembershipForUser(Long userId) {
        return memberMapper.selectOne(new LambdaQueryWrapper<FamilyMember>()
                .eq(FamilyMember::getUserId, userId)
                .eq(FamilyMember::getStatus, "ACTIVE")
                .last("limit 1"));
    }
}
