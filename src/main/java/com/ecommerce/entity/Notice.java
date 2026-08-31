package com.ecommerce.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Notice {
    private Long id;
    private String title;
    private String content;
    private Integer status;
    private Integer sort;
    private LocalDateTime createTime;
}