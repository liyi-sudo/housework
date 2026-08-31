package com.ecommerce.mapper;

import com.ecommerce.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {
    User selectByPhone(String phone);
    User selectById(Long id);
    int insert(User user);
    int updateInfo(User user);
    int updatePassword(@Param("id") Long id, @Param("password") String password);
    int updateOpenid(@Param("id") Long id, @Param("openid") String openid);
    User selectByOpenid(String openid);
    long countAll();
    List<User> selectPage(@Param("keyword") String keyword, @Param("status") Integer status,
                          @Param("offset") int offset, @Param("size") int size);
    long countPage(@Param("keyword") String keyword, @Param("status") Integer status);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}