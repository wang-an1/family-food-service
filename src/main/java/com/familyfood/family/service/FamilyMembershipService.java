package com.familyfood.family.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.familyfood.family.api.FamilyMembershipApi;
import com.familyfood.family.dao.FamilyMemberMapper;
import com.familyfood.family.entity.FamilyMember;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FamilyMembershipService implements FamilyMembershipApi {
    private final FamilyMemberMapper memberMapper;

    public FamilyMembershipService(FamilyMemberMapper memberMapper) {
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
