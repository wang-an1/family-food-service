package com.familyfood.shopping.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familyfood.shopping.entity.ShoppingList;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShoppingListMapper extends BaseMapper<ShoppingList> {
}
