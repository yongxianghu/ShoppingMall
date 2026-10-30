package com.shop.controller;

import com.shop.common.Result;
import com.shop.entity.CartItemTbl;
import com.shop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 购物车。未登录时前端会先去做登录，登录后带上 userId 再来加购。
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestParam Integer userId) {
        return Result.ok(cartService.list(userId));
    }

    @PostMapping
    public Result<Void> add(@RequestParam Integer userId, @RequestBody CartItemTbl form) {
        cartService.add(userId, form);
        return Result.ok("已加入购物车", null);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@RequestParam Integer userId,
                               @PathVariable Integer id,
                               @RequestParam(required = false) Integer quantity,
                               @RequestParam(required = false) Integer checked) {
        cartService.update(userId, id, quantity, checked);
        return Result.ok("已更新", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestParam Integer userId, @PathVariable Integer id) {
        cartService.delete(userId, id);
        return Result.ok("已删除", null);
    }

    @DeleteMapping
    public Result<Void> clear(@RequestParam Integer userId) {
        cartService.clear(userId);
        return Result.ok("购物车已清空", null);
    }
}
