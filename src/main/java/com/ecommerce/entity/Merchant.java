package com.ecommerce.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Merchant {
    private Long id;
    private String account;
    private String password;
    private String contactName;
    private String contactPhone;
    private Integer status;
    private LocalDateTime createTime;
    private String shopName;
}