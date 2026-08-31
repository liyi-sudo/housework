package com.ecommerce.service;

import com.ecommerce.common.BizException;
import com.ecommerce.common.UserContext;
import com.ecommerce.entity.Coupon;
import com.ecommerce.entity.UserCoupon;
import com.ecommerce.mapper.CouponMapper;
import com.ecommerce.mapper.UserCouponMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;

    public List<Coupon> claimable() {
        List<Coupon> list = couponMapper.selectClaimable();
        Long userId = UserContext.get();
        if (userId != null) {
            Set<Long> claimed = userCouponMapper.selectClaimedCouponIds(userId).stream().collect(Collectors.toSet());
            list.forEach(c -> c.setClaimed(claimed.contains(c.getId())));
        }
        return list;
    }

    public void claim(Long couponId) {
        Long userId = UserContext.get();
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || coupon.getStatus() != 1) {
            throw new BizException("优惠券不存在或已停用");
        }
        if (userCouponMapper.countByUserAndCoupon(userId, couponId) > 0) {
            throw new BizException("您已领取过该优惠券");
        }
        int rows = couponMapper.incrementReceivedLimit(couponId, coupon.getTotal());
        if (rows == 0) {
            throw new BizException("优惠券已被领完");
        }
        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(couponId);
        userCouponMapper.insert(uc);
    }

    public List<UserCoupon> mine(Integer status) {
        return userCouponMapper.selectByUser(UserContext.get(), status);
    }
}