package com.shop.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.shop.entity.UserTbl;

/**
 * 用户：注册、登录、查资料、改资料。
 */
public interface UserService extends IService<UserTbl> {

    UserTbl register(UserTbl form);

    UserTbl login(String username, String password);

    UserTbl get(Integer id);

    void update(Integer id, UserTbl form);
}
