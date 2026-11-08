package com.shop.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shop.common.Result;
import com.shop.entity.BannerTbl;
import com.shop.entity.ProductTbl;
import com.shop.mapper.BannerMapper;
import com.shop.mapper.CategoryMapper;
import com.shop.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 首页聚合：一次请求拿到轮播、分类、热销，前端少发三次请求。
 */
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final BannerMapper bannerMapper;
    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;

    @GetMapping
    public Result<Map<String, Object>> index() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("banners", bannerMapper.selectList(Wrappers.<BannerTbl>lambdaQuery()
                .eq(BannerTbl::getStatus, 1)
                .orderByAsc(BannerTbl::getSort)));
        data.put("categories", categoryMapper.selectList(null));
        data.put("hotProducts", productMapper.selectList(Wrappers.<ProductTbl>lambdaQuery()
                .eq(ProductTbl::getStatus, 1)
                .orderByDesc(ProductTbl::getSales)
                .last("limit 8")));
        return Result.ok(data);
    }
}
