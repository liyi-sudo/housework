package com.ecommerce.mapper;

import com.ecommerce.entity.Merchant;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MerchantMapper {
    Merchant selectByAccount(String account);
    Merchant selectById(Long id);
    List<Merchant> selectPage(@Param("keyword") String keyword,
                              @Param("status") Integer status,
                              @Param("offset") int offset, @Param("size") int size);
    long countPage(@Param("keyword") String keyword, @Param("status") Integer status);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}