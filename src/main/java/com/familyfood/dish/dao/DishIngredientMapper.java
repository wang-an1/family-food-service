package com.familyfood.dish.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familyfood.dish.entity.DishIngredient;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DishIngredientMapper extends BaseMapper<DishIngredient> {
    List<DishIngredient> selectByDishIds(@Param("dishIds") Collection<Long> dishIds);
}
