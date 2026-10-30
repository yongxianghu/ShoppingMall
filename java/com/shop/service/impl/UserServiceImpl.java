package com.shop.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.shop.common.BusinessException;
import com.shop.entity.UserTbl;
import com.shop.mapper.UserMapper;
import com.shop.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserTbl> implements UserService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public UserTbl register(UserTbl form) {
        if (!StringUtils.hasText(form.getUsername()) || !StringUtils.hasText(form.getPassword())) {
            throw new BusinessException("用户名和密码不能为空");
        }
        long exists = count(Wrappers.<UserTbl>lambdaQuery().eq(UserTbl::getUsername, form.getUsername()));
        if (exists > 0) {
            throw new BusinessException("用户名已被占用");
        }

        UserTbl user = new UserTbl();
        user.setUsername(form.getUsername());
        user.setPassword(encoder.encode(form.getPassword()));
        user.setNickname(StringUtils.hasText(form.getNickname()) ? form.getNickname() : form.getUsername());
        user.setTel(form.getTel());
        user.setStatus(1);
        save(user);

        return hidePassword(user);
    }

    @Override
    public UserTbl login(String username, String password) {
        UserTbl user = getOne(Wrappers.<UserTbl>lambdaQuery().eq(UserTbl::getUsername, username), false);
        if (user == null || !encoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        return hidePassword(user);
    }

    @Override
    public UserTbl get(Integer id) {
        UserTbl user = getById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return hidePassword(user);
    }

    @Override
    public void update(Integer id, UserTbl form) {
        UserTbl user = new UserTbl();
        user.setId(id);
        user.setNickname(form.getNickname());
        user.setAvatar(form.getAvatar());
        user.setTel(form.getTel());
        updateById(user);
    }

    /** 返回给前端之前，把密码抹掉。 */
    private UserTbl hidePassword(UserTbl user) {
        user.setPassword(null);
        return user;
    }
}
