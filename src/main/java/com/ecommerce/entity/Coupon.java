package com.ecommerce.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Coupon {
    private Long id;
    private String name;
    private BigDecimal threshold;
    private BigDecimal amount;
    private Integer total;
    private Integer received;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private boolean claimed;
}