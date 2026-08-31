package com.ecommerce.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String phone;
    private String password;
    private String nickname;
    private String avatar;
    private Integer gender;
    private String openid;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}