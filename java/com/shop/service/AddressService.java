package com.shop.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.shop.entity.AddressTbl;

import java.util.List;

/**
 * 收货地址。1111112222222
 */
public interface AddressService extends IService<AddressTbl> {

    List<AddressTbl> list(Integer userId);

    AddressTbl add(Integer userId, AddressTbl address);

    void update(Integer userId, AddressTbl address);

    void delete(Integer userId, Integer id);

    void setDefault(Integer userId, Integer id);
}
