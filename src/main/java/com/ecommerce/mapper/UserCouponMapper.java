package com.ecommerce.mapper;

import com.ecommerce.entity.UserCoupon;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserCouponMapper {
    int insert(UserCoupon userCoupon);
    List<UserCoupon> selectByUser(@Param("userId") Long userId, @Param("status") Integer status);
    UserCoupon selectById(@Param("id") Long id, @Param("userId") Long userId);
    int markUsed(@Param("id") Long id, @Param("orderId") Long orderId);
    int countByUserAndCoupon(@Param("userId") Long userId, @Param("couponId") Long couponId);
    List<Long> selectClaimedCouponIds(@Param("userId") Long userId);
}