package com.familyfood.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familyfood.order.dto.DishQuantitySummary;
import com.familyfood.order.dto.OrderView;
import com.familyfood.order.entity.PersonalOrder;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PersonalOrderMapper extends BaseMapper<PersonalOrder> {
    List<OrderView> selectOrderViews(@Param("familyId") Long familyId,
                                     @Param("mealSessionId") Long mealSessionId,
                                     @Param("status") String status,
                                     @Param("userId") Long userId);

    OrderView selectOrderViewById(@Param("id") Long id, @Param("familyId") Long familyId);

    List<DishQuantitySummary> selectDishQuantitySummary(@Param("familyId") Long familyId,
                                                        @Param("mealSessionId") Long mealSessionId);
}
