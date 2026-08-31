package com.ecommerce.entity;

import lombok.Data;

@Data
public class Banner {
    private Long id;
    private String image;
    private String link;
    private Integer sort;
    private Integer status;
}