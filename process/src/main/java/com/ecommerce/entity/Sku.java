package com.ecommerce.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Sku {
    private Long id;
    private Long goodsId;
    private String spec;
    private BigDecimal price;
    private Integer stock;
    private String image;
}