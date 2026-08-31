package com.ecommerce.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserCoupon {
    private Long id;
    private Long userId;
    private Long couponId;
    private Integer status;
    private LocalDateTime receiveTime;
    private LocalDateTime useTime;
    private Long orderId;
    private String name;
    private java.math.BigDecimal threshold;
    private java.math.BigDecimal amount;
    private LocalDateTime endTime;
}