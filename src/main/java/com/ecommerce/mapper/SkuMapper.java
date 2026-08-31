package com.ecommerce.mapper;

import com.ecommerce.entity.Sku;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SkuMapper {
    List<Sku> selectByGoodsId(Long goodsId);
    Sku selectById(Long id);
    int reduceStock(@Param("id") Long id, @Param("n") int n);
    int insertBatch(@Param("list") List<Sku> list);
    int deleteByGoodsId(Long goodsId);
    int countLowStockByShop(@Param("shopId") Long shopId, @Param("threshold") int threshold);
}