package com.ecommerce.mapper;

import com.ecommerce.entity.LoginLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LoginLogMapper {
    int insert(LoginLog log);
    List<LoginLog> selectPage(@Param("username") String username,
                              @Param("userType") String userType,
                              @Param("client") String client,
                              @Param("result") String result,
                              @Param("offset") int offset, @Param("size") int size);
    long countPage(@Param("username") String username,
                   @Param("userType") String userType,
                   @Param("client") String client,
                   @Param("result") String result);
}