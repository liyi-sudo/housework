package com.ecommerce.mapper;

import com.ecommerce.entity.Category;

import java.util.List;

public interface CategoryMapper {
    List<Category> selectAll();
}