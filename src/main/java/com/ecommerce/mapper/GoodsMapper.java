package com.ecommerce.mapper;

import com.ecommerce.entity.Goods;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GoodsMapper {
    List<Goods> selectPage(@Param("keyword") String keyword,
                           @Param("categoryId") Long categoryId,
                           @Param("sort") String sort,
                           @Param("offset") int offset,
                           @Param("size") int size);
    long countPage(@Param("keyword") String keyword, @Param("categoryId") Long categoryId);
    Goods selectById(Long id);
    Goods selectDetail(Long id);
    List<Goods> selectByIds(@Param("ids") List<Long> ids);
    int addSales(@Param("goodsId") Long goodsId, @Param("n") int n);

    int insert(Goods goods);
    int update(Goods goods);
    int updateStatus(@Param("id") Long id, @Param("shopId") Long shopId, @Param("status") Integer status);
    List<Goods> selectByShop(@Param("shopId") Long shopId,
                             @Param("keyword") String keyword,
                             @Param("status") Integer status,
                             @Param("offset") int offset,
                             @Param("size") int size);
    long countByShop(@Param("shopId") Long shopId,
                     @Param("keyword") String keyword,
                     @Param("status") Integer status);

    int updateAdminStatus(@Param("id") Long id, @Param("status") Integer status);
    List<Goods> selectAllPage(@Param("keyword") String keyword,
                              @Param("status") Integer status,
                              @Param("offset") int offset,
                              @Param("size") int size);
    long countAllPage(@Param("keyword") String keyword, @Param("status") Integer status);
}