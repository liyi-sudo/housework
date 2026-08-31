package com.ecommerce.mapper;

import com.ecommerce.entity.Address;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AddressMapper {
    List<Address> selectByUserId(Long userId);
    Address selectById(Long id);
    int insert(Address address);
    int update(Address address);
    int deleteByUserIdAndId(@Param("userId") Long userId, @Param("id") Long id);
    int clearDefault(Long userId);
    int setDefault(@Param("userId") Long userId, @Param("id") Long id);
    int countByUser(Long userId);
}