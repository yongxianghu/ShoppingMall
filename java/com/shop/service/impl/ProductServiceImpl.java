package com.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.shop.common.BusinessException;
import com.shop.entity.ProductTbl;
import com.shop.mapper.ProductMapper;
import com.shop.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, ProductTbl> implements ProductService {

    @Override
    public Page<ProductTbl> pageQuery(int page, int size, Long categoryId, String keyword, String sort) {
        int current = Math.max(page, 1);
        int pageSize = (size < 1 || size > 100) ? 12 : size;

        LambdaQueryWrapper<ProductTbl> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ProductTbl::getStatus, 1);

        if (categoryId != null) {
            wrapper.eq(ProductTbl::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ProductTbl::getName, keyword).or().like(ProductTbl::getSubtitle, keyword));
        }

        if ("price_asc".equals(sort)) {
            wrapper.orderByAsc(ProductTbl::getPrice);
        } else if ("price_desc".equals(sort)) {
            wrapper.orderByDesc(ProductTbl::getPrice);
        } else if ("new".equals(sort)) {
            wrapper.orderByDesc(ProductTbl::getCreatedAt);
        } else {
            wrapper.orderByDesc(ProductTbl::getSales);
        }
        // 分页插件会在这条查询上自动加 limit，并额外跑一次 count
        return page(new Page<>(current, pageSize), wrapper);
    }

    @Override
    public ProductTbl detail(Long id) {
        ProductTbl product = getById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        return product;
    }
}
