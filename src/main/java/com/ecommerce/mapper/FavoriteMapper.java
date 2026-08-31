package com.ecommerce.mapper;

import com.ecommerce.entity.Favorite;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FavoriteMapper {
    int insert(Favorite favorite);
    int delete(@Param("userId") Long userId, @Param("goodsId") Long goodsId);
    int countByUserGoods(@Param("userId") Long userId, @Param("goodsId") Long goodsId);
    List<Favorite> selectByUser(@Param("userId") Long userId, @Param("offset") int offset, @Param("size") int size);
    long countByUser(Long userId);
}