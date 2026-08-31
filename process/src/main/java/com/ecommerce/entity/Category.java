package com.ecommerce.entity;

import lombok.Data;

@Data
public class Category {
    private Long id;
    private Long parentId;
    private String name;
    private Integer sort;
    private String icon;
}