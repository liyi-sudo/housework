package com.ecommerce.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Logistics {
    private Long id;
    private Long orderId;
    private String logisticsNo;
    private String company;
    private Integer status;
    private String trace;
    private LocalDateTime createTime;
}