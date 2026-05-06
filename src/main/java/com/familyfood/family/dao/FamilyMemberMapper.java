package com.familyfood.family.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familyfood.family.dto.MemberResponse;
import com.familyfood.family.entity.FamilyMember;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FamilyMemberMapper extends BaseMapper<FamilyMember> {
    List<MemberResponse> selectMemberResponses(@Param("familyId") Long familyId);
}
