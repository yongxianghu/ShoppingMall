package com.shop.controller;

import com.shop.common.Result;
import com.shop.entity.UserTbl;
import com.shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 注册 / 登录。
 * 教学版：登录只校验用户名密码，成功后把用户信息（含 id）返回给前端，
 * 前端存在本地，后续请求带上 userId。真实项目这里要发 Token。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<UserTbl> register(@RequestBody UserTbl form) {
        return Result.ok("注册成功", userService.register(form));
    }

    @PostMapping("/login")
    public Result<UserTbl> login(@RequestBody UserTbl form) {
        return Result.ok("登录成功", userService.login(form.getUsername(), form.getPassword()));
    }
}
