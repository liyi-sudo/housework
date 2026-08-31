package com.ecommerce.mapper;

import com.ecommerce.entity.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    List<Cart> selectByUserId(Long userId);
    Cart selectById(Long id);
    Cart selectByUserAndSku(@Param("userId") Long userId, @Param("skuId") Long skuId);
    int insert(Cart cart);
    int updateQuantity(@Param("id") Long id, @Param("userId") Long userId, @Param("quantity") int quantity);
    int updateChecked(@Param("userId") Long userId, @Param("checked") Integer checked, @Param("ids") List<Long> ids);
    int deleteByUserAndId(@Param("userId") Long userId, @Param("id") Long id);
    List<Cart> selectCheckoutItems(@Param("userId") Long userId, @Param("ids") List<Long> ids);
}