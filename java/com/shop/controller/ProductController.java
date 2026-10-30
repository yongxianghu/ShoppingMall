package com.shop.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.common.Result;
import com.shop.entity.ProductTbl;
import com.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品：列表（分页/分类/搜索/排序）、详情。
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Result<Page<ProductTbl>> page(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "12") int size,
                                         @RequestParam(required = false) Long categoryId,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String sort) {
        return Result.ok(productService.pageQuery(page, size, categoryId, keyword, sort));
    }

    @GetMapping("/{id}")
    public Result<ProductTbl> detail(@PathVariable Long id) {
        return Result.ok(productService.detail(id));
    }
}
