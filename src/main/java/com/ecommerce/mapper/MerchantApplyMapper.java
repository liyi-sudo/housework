package com.ecommerce.mapper;

import com.ecommerce.entity.MerchantApply;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MerchantApplyMapper {
    int insert(MerchantApply apply);
    MerchantApply selectById(Long id);
    List<MerchantApply> selectPage(@Param("status") Integer status,
                                   @Param("offset") int offset, @Param("size") int size);
    long countPage(@Param("status") Integer status);
    int review(@Param("id") Long id, @Param("status") Integer status, @Param("reason") String reason);
}