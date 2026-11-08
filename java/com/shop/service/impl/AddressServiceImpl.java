package com.shop.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.shop.common.BusinessException;
import com.shop.entity.AddressTbl;
import com.shop.mapper.AddressMapper;
import com.shop.service.AddressService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, AddressTbl> implements AddressService {

    @Override
    public List<AddressTbl> list(Integer userId) {
        return list(Wrappers.<AddressTbl>lambdaQuery()
                .eq(AddressTbl::getUserId, userId)
                .orderByDesc(AddressTbl::getIsDefault)
                .orderByDesc(AddressTbl::getId));
    }

    @Override
    public AddressTbl add(Integer userId, AddressTbl address) {
        address.setId(null);
        address.setUserId(userId);
        if (address.getIsDefault() == null) {
            address.setIsDefault(0);
        }
        if (address.getIsDefault() == 1) {
            clearDefault(userId);
        }
        save(address);
        return address;
    }

    @Override
    public void update(Integer userId, AddressTbl address) {
        AddressTbl exists = getById(address.getId());
        if (exists == null || !exists.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearDefault(userId);
        }
        address.setUserId(userId);
        updateById(address);
    }

    @Override
    public void delete(Integer userId, Integer id) {
        AddressTbl exists = getById(id);
        if (exists == null || !exists.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }
        removeById(id);
    }

    @Override
    public void setDefault(Integer userId, Integer id) {
        AddressTbl exists = getById(id);
        if (exists == null || !exists.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }
        clearDefault(userId);
        AddressTbl update = new AddressTbl();
        update.setId(id);
        update.setIsDefault(1);
        updateById(update);
    }

    private void clearDefault(Integer userId) {
        AddressTbl reset = new AddressTbl();
        reset.setIsDefault(0);
        update(reset, Wrappers.<AddressTbl>lambdaUpdate().eq(AddressTbl::getUserId, userId));
    }
}
