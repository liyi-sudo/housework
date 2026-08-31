package com.ecommerce.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BrowseHistory {
    private Long id;
    private Long userId;
    private Long goodsId;
    private LocalDateTime createTime;
    private String goodsName;
    private String mainImage;
    private java.math.BigDecimal price;
    private Integer status;
}