package com.ecommerce.mapper;

import com.ecommerce.entity.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface OrderItemMapper {
    int insertBatch(@Param("items") List<OrderItem> items);
    List<OrderItem> selectByOrderId(Long orderId);
    OrderItem selectById(Long id);
    int updateReviewStatus(@Param("orderItemId") Long orderItemId);
    OrderItem selectReviewableByUserAndGoods(@Param("userId") Long userId, @Param("goodsId") Long goodsId);
    OrderItem selectByGoodsAndUser(@Param("userId") Long userId, @Param("goodsId") Long goodsId);
    List<Map<String, Object>> selectGoodsSalesTop(@Param("shopId") Long shopId, @Param("top") int top);
    List<Map<String, Object>> selectCategoryShare(@Param("shopId") Long shopId);
}