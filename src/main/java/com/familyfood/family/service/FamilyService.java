package com.familyfood.family.service;

import com.familyfood.family.dto.MemberResponse;
import com.familyfood.family.dto.UpdateMemberRequest;
import com.familyfood.family.entity.Family;
import java.util.List;

public interface FamilyService {
    Family currentFamily();

    List<MemberResponse> members();

    MemberResponse updateMember(Long id, UpdateMemberRequest request);
}
