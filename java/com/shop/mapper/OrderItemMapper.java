package com.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.entity.OrderItemTbl;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItemTbl> {
}
