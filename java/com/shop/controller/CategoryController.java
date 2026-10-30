package com.shop.controller;

import com.shop.common.Result;
import com.shop.entity.CategoryTbl;
import com.shop.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类列表（数据量小，直接查 Mapper，不再套一层 Service）。
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryMapper categoryMapper;

    @GetMapping
    public Result<List<CategoryTbl>> list() {
        return Result.ok(categoryMapper.selectList(null));
    }
}
