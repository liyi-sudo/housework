package com.ecommerce.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Review {
    private Long id;
    private Long userId;
    private Long orderItemId;
    private Long goodsId;
    private Long shopId;
    private Integer rating;
    private String content;
    private String images;
    private String reply;
    private LocalDateTime createTime;
    private String nickname;
    private String avatar;
    private String goodsName;
}