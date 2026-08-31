package com.ecommerce.mapper;

import com.ecommerce.entity.BrowseHistory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BrowseHistoryMapper {
    int insert(BrowseHistory history);
    List<BrowseHistory> selectByUser(@Param("userId") Long userId, @Param("limit") int limit);
    int deleteByUser(Long userId);
}