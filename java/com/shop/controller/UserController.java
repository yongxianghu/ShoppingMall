package com.shop.controller;

import com.shop.common.Result;
import com.shop.entity.UserTbl;
import com.shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 个人信息。
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public Result<UserTbl> get(@PathVariable Integer id) {
        return Result.ok(userService.get(id));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @RequestBody UserTbl form) {
        userService.update(id, form);
        return Result.ok("保存成功", null);
    }
}
