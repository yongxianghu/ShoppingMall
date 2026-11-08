package com.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.entity.OrdersTbl;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrdersMapper extends BaseMapper<OrdersTbl> {
}
