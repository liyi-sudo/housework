package com.ecommerce.common;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private List<T> list;
    private long total;
    private int page;
    private int size;
    private long pages;

    public static <T> PageResult<T> of(List<T> list, long total, int page, int size) {
        PageResult<T> p = new PageResult<>();
        p.setList(list);
        p.setTotal(total);
        p.setPage(page);
        p.setSize(size);
        p.setPages(size == 0 ? 0 : (total + size - 1) / size);
        return p;
    }
}