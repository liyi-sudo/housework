package com.ecommerce.mapper;

import com.ecommerce.entity.Notice;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NoticeMapper {
    List<Notice> selectShowList();
    Notice selectById(Long id);

    List<Notice> selectPage(@Param("keyword") String keyword, @Param("status") Integer status,
                            @Param("offset") int offset, @Param("size") int size);
    long countPage(@Param("keyword") String keyword, @Param("status") Integer status);
    int insert(Notice notice);
    int update(Notice notice);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    int deleteById(@Param("id") Long id);
}