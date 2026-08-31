package com.ecommerce.service;

import com.ecommerce.common.BizException;
import com.ecommerce.common.UserContext;
import com.ecommerce.entity.User;
import com.ecommerce.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public User current() {
        User user = userMapper.selectById(UserContext.get());
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    public void updateInfo(User user) {
        user.setId(UserContext.get());
        userMapper.updateInfo(user);
    }

    public void changePassword(String oldPassword, String newPassword) {
        User user = userMapper.selectById(UserContext.get());
        if (!new BCryptPasswordEncoder().matches(oldPassword, user.getPassword())) {
            throw new BizException("原密码不正确");
        }
        userMapper.updatePassword(user.getId(), new BCryptPasswordEncoder().encode(newPassword));
    }
}