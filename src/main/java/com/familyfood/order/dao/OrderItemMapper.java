package com.familyfood.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familyfood.order.entity.OrderItem;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
    List<OrderItem> selectByOrderIds(@Param("orderIds") Collection<Long> orderIds);
}
