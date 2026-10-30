package com.shop.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.shop.common.BusinessException;
import com.shop.entity.CartItemTbl;
import com.shop.entity.ProductTbl;
import com.shop.mapper.CartItemMapper;
import com.shop.mapper.ProductMapper;
import com.shop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartItemMapper, CartItemTbl> implements CartService {

    private final ProductMapper productMapper;

    @Override
    @Transactional
    public void add(Integer userId, CartItemTbl form) {
        ProductTbl product = productMapper.selectById(form.getProductId());
        if (product == null || product.getStatus() == null || product.getStatus() == 0) {
            throw new BusinessException("商品已下架");
        }

        int quantity = form.getQuantity() == null ? 1 : form.getQuantity();
        CartItemTbl exists = getOne(Wrappers.<CartItemTbl>lambdaQuery()
                .eq(CartItemTbl::getUserId, userId)
                .eq(CartItemTbl::getProductId, form.getProductId()), false);

        if (exists == null) {
            if (quantity > product.getStock()) {
                throw new BusinessException("库存不足，仅剩 " + product.getStock() + " 件");
            }
            CartItemTbl item = new CartItemTbl();
            item.setUserId(userId);
            item.setProductId(form.getProductId());
            item.setQuantity(quantity);
            item.setChecked(1);
            save(item);
        } else {
            int newQuantity = exists.getQuantity() + quantity;
            if (newQuantity > product.getStock()) {
                throw new BusinessException("库存不足，仅剩 " + product.getStock() + " 件");
            }
            exists.setQuantity(newQuantity);
            updateById(exists);
        }
    }

    @Override
    public List<Map<String, Object>> list(Integer userId) {
        List<CartItemTbl> items = list(Wrappers.<CartItemTbl>lambdaQuery()
                .eq(CartItemTbl::getUserId, userId)
                .orderByDesc(CartItemTbl::getId));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (CartItemTbl item : items) {
            ProductTbl product = productMapper.selectById(item.getProductId());
            if (product == null) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("productId", item.getProductId());
            row.put("name", product.getName());
            row.put("image", product.getMainImage());
            row.put("price", product.getPrice());
            row.put("quantity", item.getQuantity());
            row.put("checked", item.getChecked());
            row.put("stock", product.getStock());
            row.put("status", product.getStatus());
            rows.add(row);
        }
        return rows;
    }

    @Override
    public void update(Integer userId, Integer id, Integer quantity, Integer checked) {
        CartItemTbl item = getById(id);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BusinessException("购物车记录不存在");
        }
        if (quantity != null) {
            ProductTbl product = productMapper.selectById(item.getProductId());
            if (product != null && quantity > product.getStock()) {
                throw new BusinessException("库存不足");
            }
            item.setQuantity(quantity);
        }
        if (checked != null) {
            item.setChecked(checked);
        }
        updateById(item);
    }

    @Override
    public void delete(Integer userId, Integer id) {
        CartItemTbl item = getById(id);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BusinessException("购物车记录不存在");
        }
        removeById(id);
    }

    @Override
    public void clear(Integer userId) {
        remove(Wrappers.<CartItemTbl>lambdaQuery().eq(CartItemTbl::getUserId, userId));
    }
}
