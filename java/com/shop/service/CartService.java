package com.shop.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.shop.entity.CartItemTbl;

import java.util.List;
import java.util.Map;

/**
 * 购物车：加、查、改数量、删、清空。
 */
public interface CartService extends IService<CartItemTbl> {

    void add(Integer userId, CartItemTbl form);

    List<Map<String, Object>> list(Integer userId);

    void update(Integer userId, Integer id, Integer quantity, Integer checked);

    void delete(Integer userId, Integer id);

    void clear(Integer userId);
}
