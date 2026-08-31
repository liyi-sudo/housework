package com.ecommerce.mapper;

import com.ecommerce.entity.Admin;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdminMapper {
    Admin selectByUsername(String username);
    Admin selectById(Long id);
    List<Admin> selectPage(@Param("keyword") String keyword,
                           @Param("status") Integer status,
                           @Param("offset") int offset, @Param("size") int size);
    long countPage(@Param("keyword") String keyword, @Param("status") Integer status);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}