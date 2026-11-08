package com.shop.controller;

import com.shop.common.Result;
import com.shop.entity.OrdersTbl;
import com.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单：下单、列表、详情、取消、支付（模拟）。
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<OrdersTbl> create(@RequestParam Integer userId, @RequestParam Integer addressId) {
        return Result.ok("下单成功", orderService.create(userId, addressId));
    }

    @GetMapping
    public Result<List<OrdersTbl>> list(@RequestParam Integer userId) {
        return Result.ok(orderService.list(userId));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@RequestParam Integer userId, @PathVariable Integer id) {
        return Result.ok(orderService.detail(userId, id));
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@RequestParam Integer userId, @PathVariable Integer id) {
        orderService.cancel(userId, id);
        return Result.ok("订单已取消", null);
    }

    @PutMapping("/{id}/pay")
    public Result<Void> pay(@RequestParam Integer userId, @PathVariable Integer id) {
        orderService.pay(userId, id);
        return Result.ok("支付成功", null);
    }
}
