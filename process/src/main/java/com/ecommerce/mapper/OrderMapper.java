package com.ecommerce.mapper;

import com.ecommerce.entity.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface OrderMapper {
    int insert(Order order);
    Order selectById(Long id);
    Order selectFullById(@Param("id") Long id, @Param("userId") Long userId);
    List<Order> selectPageByUser(@Param("userId") Long userId, @Param("status") Integer status,
                                 @Param("offset") int offset, @Param("size") int size);
    long countByUser(@Param("userId") Long userId, @Param("status") Integer status);
    int updateStatus(@Param("id") Long id, @Param("fromStatus") Integer fromStatus, @Param("toStatus") Integer toStatus,
                     @Param("userId") Long userId);
    List<Order> selectTimeoutOrders(@Param("timeoutMin") int timeoutMin);
    int cancelTimeoutOrder(@Param("id") Long id);

    List<Order> selectPageByShop(@Param("shopId") Long shopId, @Param("status") Integer status,
                                 @Param("offset") int offset, @Param("size") int size);
    long countByShop(@Param("shopId") Long shopId, @Param("status") Integer status);
    int ship(@Param("id") Long id, @Param("shopId") Long shopId);
    int updateStatusByShop(@Param("id") Long id, @Param("shopId") Long shopId,
                           @Param("fromStatus") Integer fromStatus, @Param("toStatus") Integer toStatus);
    int countTodayByShop(@Param("shopId") Long shopId);
    long sumPaidByShop(@Param("shopId") Long shopId);

    List<Order> selectAllPage(@Param("orderNo") String orderNo, @Param("status") Integer status,
                              @Param("offset") int offset, @Param("size") int size);
    long countAllPage(@Param("orderNo") String orderNo, @Param("status") Integer status);
    int countToday();
    long sumPaid();
    List<Map<String, Object>> select7DayTrend(@Param("days") int days);
    List<Map<String, Object>> selectStatusDist();
    List<Map<String, Object>> selectTrendByShop(@Param("shopId") Long shopId, @Param("days") int days);
    List<Map<String, Object>> selectStatusDistByShop(@Param("shopId") Long shopId);
    Map<String, Object> selectStatsByShop(@Param("shopId") Long shopId);
}