package com.ecommerce.mapper;

import com.ecommerce.entity.Coupon;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CouponMapper {
    List<Coupon> selectClaimable();
    Coupon selectById(Long id);
    int incrementReceived(Long id);
    int incrementReceivedLimit(@Param("id") Long id, @Param("total") Integer total);

    List<Coupon> selectPage(@Param("keyword") String keyword, @Param("status") Integer status,
                            @Param("offset") int offset, @Param("size") int size);
    long countPage(@Param("keyword") String keyword, @Param("status") Integer status);
    int insert(Coupon coupon);
    int update(Coupon coupon);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    int deleteById(@Param("id") Long id);
}