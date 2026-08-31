package com.ecommerce.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Goods {
    private Long id;
    private Long shopId;
    private Long categoryId;
    private String name;
    private String subtitle;
    private String mainImage;
    private String images;
    private String detail;
    private BigDecimal price;
    private Integer sales;
    private Integer status;
    private LocalDateTime createTime;
    private List<Sku> skuList;
    private String shopName;
    private Integer stock;
}