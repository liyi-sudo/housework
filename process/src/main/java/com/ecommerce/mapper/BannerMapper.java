package com.ecommerce.mapper;

import com.ecommerce.entity.Banner;

import java.util.List;

public interface BannerMapper {
    List<Banner> selectShowList();
}