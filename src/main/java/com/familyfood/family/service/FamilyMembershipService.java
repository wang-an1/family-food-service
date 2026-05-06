package com.familyfood.family.service;

import com.familyfood.family.api.FamilyMembershipApi;
import com.familyfood.family.entity.FamilyMember;

public interface FamilyMembershipService extends FamilyMembershipApi {
    FamilyMember activeMembershipForUser(Long userId);
}
