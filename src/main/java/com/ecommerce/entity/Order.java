package com.ecommerce.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Order {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long shopId;
    private BigDecimal totalAmount;
    private BigDecimal freight;
    private BigDecimal discount;
    private BigDecimal payAmount;
    private String addrSnapshot;
    private Long couponId;
    private String remark;
    private Integer status;
    private LocalDateTime payTime;
    private LocalDateTime shipTime;
    private LocalDateTime finishTime;
    private LocalDateTime createTime;
    private List<OrderItem> items;
    private String shopName;
    private String statusText;
}