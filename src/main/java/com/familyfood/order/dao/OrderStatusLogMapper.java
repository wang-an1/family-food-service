package com.familyfood.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familyfood.order.entity.OrderStatusLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderStatusLogMapper extends BaseMapper<OrderStatusLog> {
}
