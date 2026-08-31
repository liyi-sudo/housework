package com.ecommerce.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginLog {
    private Long id;
    private String username;
    private String userType;
    private String client;
    private String ip;
    private String result;
    private String message;
    private LocalDateTime createTime;
}