package com.ecommerce.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MerchantApply {
    private Long id;
    private Long merchantId;
    private String shopName;
    private String categoryIds;
    private String contactName;
    private String contactPhone;
    private String qualification;
    private Integer status;
    private String reason;
    private LocalDateTime createTime;
    private LocalDateTime auditTime;
    private String merchantAccount;
    private String statusText;
}