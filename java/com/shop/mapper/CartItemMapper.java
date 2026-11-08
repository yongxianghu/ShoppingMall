package com.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.entity.CartItemTbl;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartItemMapper extends BaseMapper<CartItemTbl> {
}
