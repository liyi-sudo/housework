package com.ecommerce.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Aftersale {
    private Long id;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private Long shopId;
    private Integer type;
    private String reason;
    private BigDecimal refundAmount;
    private String images;
    private Integer status;
    private String reply;
    private LocalDateTime createTime;
    private LocalDateTime handleTime;
    private String statusText;
    private String typeText;
}