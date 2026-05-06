package com.familyfood.family.api;

import com.familyfood.family.entity.FamilyMember;

public interface FamilyMembershipApi {
    FamilyMember activeMembershipForUser(Long userId);
}
