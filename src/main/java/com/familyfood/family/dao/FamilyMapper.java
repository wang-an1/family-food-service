package com.familyfood.family.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familyfood.family.entity.Family;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FamilyMapper extends BaseMapper<Family> {
}
