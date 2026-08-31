package com.ecommerce.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItem {
    private Long id;
    private Long orderId;
    private Long goodsId;
    private String goodsName;
    private Long skuId;
    private String skuSpec;
    private String coverImage;
    private BigDecimal price;
    private Integer quantity;
    private Integer reviewStatus;
}