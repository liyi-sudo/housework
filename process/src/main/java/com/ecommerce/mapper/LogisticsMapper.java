package com.ecommerce.mapper;

import com.ecommerce.entity.Logistics;

public interface LogisticsMapper {
    int insert(Logistics logistics);
    Logistics selectByOrderId(Long orderId);
}