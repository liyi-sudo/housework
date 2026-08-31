package com.ecommerce.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Payment {
    private Long id;
    private String tradeNo;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String method;
    private Integer status;
    private LocalDateTime payTime;
    private LocalDateTime createTime;
    private String userName;
    private String statusText;
}