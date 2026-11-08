package com.shop.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;
import com.shop.entity.ProductTbl;

/**
 * 商品：分页列表 + 详情。
 */
public interface ProductService extends IService<ProductTbl> {

    /**
     * 分页查询上架商品。
     *
     * @param sort sales（默认，按销量）/ price_asc / price_desc / new
     */
    Page<ProductTbl> pageQuery(int page, int size, Long categoryId, String keyword, String sort);

    ProductTbl detail(Long id);
}
