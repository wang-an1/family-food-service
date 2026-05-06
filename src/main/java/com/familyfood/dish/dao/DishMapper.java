package com.familyfood.dish.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familyfood.dish.dto.DishView;
import com.familyfood.dish.entity.Dish;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
    List<DishView> selectDishViews(@Param("familyId") Long familyId,
                                   @Param("keyword") String keyword,
                                   @Param("categoryId") Long categoryId,
                                   @Param("tagId") Long tagId,
                                   @Param("status") String status,
                                   @Param("activeOnly") boolean activeOnly);

    DishView selectDishViewById(@Param("id") Long id,
                                @Param("familyId") Long familyId,
                                @Param("activeOnly") boolean activeOnly);
}
