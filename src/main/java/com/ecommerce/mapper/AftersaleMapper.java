package com.ecommerce.mapper;

import com.ecommerce.entity.Aftersale;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AftersaleMapper {
    int insert(Aftersale aftersale);
    List<Aftersale> selectByUser(Long userId);
    Aftersale selectById(Long id);
    int countActiveByOrder(@Param("orderId") Long orderId);
    List<Aftersale> selectByShop(@Param("shopId") Long shopId, @Param("status") Integer status,
                                 @Param("offset") int offset, @Param("size") int size);
    long countByShop(@Param("shopId") Long shopId, @Param("status") Integer status);
    int countPendingByShop(@Param("shopId") Long shopId);
    int handle(@Param("id") Long id, @Param("shopId") Long shopId,
               @Param("status") Integer status, @Param("reply") String reply);
}