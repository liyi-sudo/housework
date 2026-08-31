package com.ecommerce.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Cart {
    private Long id;
    private Long userId;
    private Long goodsId;
    private Long skuId;
    private Integer quantity;
    private Integer checked;
    private LocalDateTime createTime;
    private String goodsName;
    private String mainImage;
    private String skuSpec;
    private BigDecimal price;
    private Integer stock;
    private Integer goodsStatus;
    private Long shopId;
}