package com.familyfood.dish.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familyfood.dish.dto.DishTagView;
import com.familyfood.dish.entity.DishTag;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DishTagMapper extends BaseMapper<DishTag> {
    List<DishTagView> selectByDishIds(@Param("dishIds") Collection<Long> dishIds);
}
