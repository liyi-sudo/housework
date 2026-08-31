package com.ecommerce.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Shop {
    private Long id;
    private Long merchantId;
    private String name;
    private String logo;
    private String banner;
    private String intro;
    private Integer status;
    private LocalDateTime createTime;
}