package com.familyfood.family.service.impl;

import com.familyfood.auth.api.UserLookupApi;
import com.familyfood.auth.entity.User;
import com.familyfood.common.AppException;
import com.familyfood.common.context.ActorContext;
import com.familyfood.common.context.ActorContextProvider;
import com.familyfood.family.dao.FamilyMapper;
import com.familyfood.family.dao.FamilyMemberMapper;
import com.familyfood.family.dto.MemberResponse;
import com.familyfood.family.dto.UpdateMemberRequest;
import com.familyfood.family.entity.Family;
import com.familyfood.family.entity.FamilyMember;
import com.familyfood.family.service.FamilyService;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FamilyServiceImpl implements FamilyService {
    private final FamilyMapper familyMapper;
    private final FamilyMemberMapper memberMapper;
    private final UserLookupApi userLookupApi;
    private final ActorContextProvider actorProvider;

    @Autowired
    public FamilyServiceImpl(FamilyMapper familyMapper, FamilyMemberMapper memberMapper, UserLookupApi userLookupApi,
                         ActorContextProvider actorProvider) {
        this.familyMapper = familyMapper;
        this.memberMapper = memberMapper;
        this.userLookupApi = userLookupApi;
        this.actorProvider = actorProvider;
    }

    public Family currentFamily() {
        ActorContext actor = actorProvider.current();
        Family family = familyMapper.selectById(actor.familyId());
        if (family == null) {
            throw AppException.notFound("未找到家庭");
        }
        return family;
    }

    public List<MemberResponse> members() {
        ActorContext actor = actorProvider.current();
        actor.requireAdmin();
        return memberMapper.selectMemberResponses(actor.familyId());
    }

    @Transactional
    public MemberResponse updateMember(Long id, UpdateMemberRequest request) {
        ActorContext actor = actorProvider.current();
        actor.requireAdmin();
        FamilyMember member = memberMapper.selectById(id);
        if (member == null || !Objects.equals(member.getFamilyId(), actor.familyId())) {
            throw AppException.notFound("未找到家庭成员");
        }
        if (request.role() != null) {
            member.setRole(request.role());
        }
        if (request.displayName() != null) {
            member.setDisplayName(request.displayName());
        }
        if (request.status() != null) {
            member.setStatus(request.status());
        }
        memberMapper.updateById(member);
        User user = userLookupApi.getById(member.getUserId());
        return new MemberResponse(member.getId(), member.getUserId(), user == null ? null : user.getUsername(),
                user == null ? null : user.getNickname(), member.getRole(), member.getDisplayName(), member.getStatus());
    }
}
