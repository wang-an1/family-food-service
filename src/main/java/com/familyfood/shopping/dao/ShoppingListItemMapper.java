package com.familyfood.shopping.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familyfood.shopping.dto.ShoppingIngredientSummary;
import com.familyfood.shopping.entity.ShoppingListItem;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ShoppingListItemMapper extends BaseMapper<ShoppingListItem> {
    List<ShoppingIngredientSummary> selectDishIngredientSummaries(@Param("familyId") Long familyId,
                                                                  @Param("mealSessionId") Long mealSessionId);
}
