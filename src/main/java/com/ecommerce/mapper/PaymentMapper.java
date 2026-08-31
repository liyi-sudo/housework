package com.ecommerce.mapper;

import com.ecommerce.entity.Payment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PaymentMapper {
    int insert(Payment payment);
    Payment selectByOrderId(Long orderId);
    List<Payment> selectPage(@Param("offset") int offset, @Param("size") int size);
    long countPage();
}