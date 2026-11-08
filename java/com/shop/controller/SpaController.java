package com.shop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Vue 用的是 history 模式路由，浏览器直接访问或刷新 /product/1 这类地址时，
 * 请求会打到后端；后端把它转发回 index.html，前端路由再接管。
 *
 * 只映射前端真实存在的几条路由，不用通配符，避免把 /api、/images 也吃掉。
 */
@Controller
public class SpaController {

    @GetMapping({"/category", "/cart", "/login", "/orders", "/mine"})
    public String forwardPage() {
        return "forward:/index.html";
    }

    @GetMapping("/product/{id}")
    public String forwardProduct(@PathVariable String id) {
        return "forward:/index.html";
    }
}
