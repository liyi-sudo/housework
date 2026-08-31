package com.ecommerce.mapper;

import com.ecommerce.entity.Shop;
import org.apache.ibatis.annotations.Param;

public interface ShopMapper {
    Shop selectByMerchantId(Long merchantId);
    Shop selectById(Long id);
    int insert(Shop shop);
    int updateByMerchantId(@Param("shopId") Long shopId, @Param("merchantId") Long merchantId, @Param("shop") Shop shop);
}