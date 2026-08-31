package com.ecommerce.mapper;

import com.ecommerce.entity.Review;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ReviewMapper {
    int insert(Review review);
    List<Review> selectByGoodsId(@Param("goodsId") Long goodsId, @Param("offset") int offset, @Param("size") int size);
    List<Review> selectByUser(Long userId);
    int countByGoodsId(Long goodsId);
    List<Review> selectByShop(@Param("shopId") Long shopId,
                              @Param("offset") int offset, @Param("size") int size);
    int countByShop(@Param("shopId") Long shopId);
    int updateReply(@Param("id") Long id, @Param("shopId") Long shopId, @Param("content") String content);
    Review selectByOrderItemId(@Param("orderItemId") Long orderItemId, @Param("userId") Long userId);
    int updateFollowUp(@Param("id") Long id, @Param("content") String content, @Param("images") String images);
    Map<String, Object> selectRatingStatsByShop(@Param("shopId") Long shopId);
}