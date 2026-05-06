package com.familyfood.dish.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familyfood.dish.entity.Dish;
import com.familyfood.dish.entity.DishTagRelation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishTagRelationMapper extends BaseMapper<DishTagRelation> {
}
