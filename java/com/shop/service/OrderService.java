package com.shop.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.shop.entity.OrdersTbl;

import java.util.List;
import java.util.Map;

/**
 * 订单：下单、列表、详情、取消、支付
 */
public interface OrderService extends IService<OrdersTbl> {

    OrdersTbl create(Integer userId, Integer addressId);

    List<OrdersTbl> list(Integer userId);

    Map<String, Object> detail(Integer userId, Integer id);

    void cancel(Integer userId, Integer id);

    void pay(Integer userId, Integer id);
}
