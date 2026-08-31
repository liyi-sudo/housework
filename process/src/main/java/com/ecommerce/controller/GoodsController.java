package com.ecommerce.controller;

import com.ecommerce.common.PageResult;
import com.ecommerce.common.Result;
import com.ecommerce.common.UserContext;
import com.ecommerce.entity.*;
import com.ecommerce.service.GoodsService;
import com.ecommerce.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;
    private final ReviewService reviewService;

    @GetMapping("/category/tree")
    public Result<List<Category>> categoryTree() {
        return Result.ok(goodsService.categoryTree());
    }

    @GetMapping("/goods")
    public Result<PageResult<Goods>> list(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) Long categoryId,
                                          @RequestParam(defaultValue = "default") String sort,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return Result.ok(goodsService.list(keyword, categoryId, sort, page, size));
    }

    @GetMapping("/goods/{id}")
    public Result<Goods> detail(@PathVariable Long id) {
        return Result.ok(goodsService.detail(id));
    }

    @GetMapping("/goods/{id}/reviews")
    public Result<PageResult<Review>> reviews(@PathVariable Long id,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return Result.ok(reviewService.pageByGoods(id, page, size));
    }

    @PostMapping("/goods/{id}/browse")
    public Result<Void> browse(@PathVariable Long id) {
        goodsService.recordBrowse(UserContext.get(), id);
        return Result.ok();
    }

    @GetMapping("/banner")
    public Result<List<Banner>> banners() {
        return Result.ok(goodsService.banners());
    }

    @GetMapping("/notice")
    public Result<List<Notice>> notices() {
        return Result.ok(goodsService.notices());
    }

    @GetMapping("/favorites")
    public Result<PageResult<Favorite>> favorites(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return Result.ok(goodsService.favorites(UserContext.get(), page, size));
    }

    @PostMapping("/favorites/{goodsId}")
    public Result<Void> addFavorite(@PathVariable Long goodsId) {
        goodsService.addFavorite(UserContext.get(), goodsId);
        return Result.ok();
    }

    @DeleteMapping("/favorites/{goodsId}")
    public Result<Void> removeFavorite(@PathVariable Long goodsId) {
        goodsService.removeFavorite(UserContext.get(), goodsId);
        return Result.ok();
    }

    @GetMapping("/favorites/{goodsId}/status")
    public Result<Boolean> favoriteStatus(@PathVariable Long goodsId) {
        return Result.ok(goodsService.isFavorite(UserContext.get(), goodsId));
    }
}