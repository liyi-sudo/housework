package com.ecommerce.controller;

import com.ecommerce.common.PageResult;
import com.ecommerce.common.Result;
import com.ecommerce.entity.Aftersale;
import com.ecommerce.entity.Goods;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.Review;
import com.ecommerce.entity.Shop;
import com.ecommerce.entity.Sku;
import com.ecommerce.service.MerchantService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;
    private final ObjectMapper objectMapper;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        return Result.ok(merchantService.login(body.get("account"), body.get("password"), request));
    }

    @GetMapping("/shop")
    public Result<Shop> shop() {
        return Result.ok(merchantService.currentShop());
    }

    @PutMapping("/shop")
    public Result<Void> updateShop(@RequestBody Shop shop) {
        merchantService.updateShop(shop.getName(), shop.getLogo(), shop.getBanner(), shop.getIntro());
        return Result.ok();
    }

    @GetMapping("/goods")
    public Result<PageResult<Goods>> goods(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Integer status,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return Result.ok(merchantService.goodsPage(keyword, status, page, size));
    }

    @GetMapping("/goods/{id}")
    public Result<Goods> goodsDetail(@PathVariable Long id) {
        return Result.ok(merchantService.goodsDetail(id));
    }

    @PostMapping("/goods")
    public Result<Void> createGoods(@RequestBody Map<String, Object> body) {
        Goods goods = buildGoods(body);
        List<Sku> skuList = body.get("skuList") == null
                ? List.of()
                : objectMapper.convertValue(body.get("skuList"), new TypeReference<List<Sku>>() {});
        merchantService.saveGoods(goods, skuList);
        return Result.ok();
    }

    @PutMapping("/goods/{id}")
    public Result<Void> updateGoods(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Goods goods = buildGoods(body);
        List<Sku> skuList = body.get("skuList") == null
                ? List.of()
                : objectMapper.convertValue(body.get("skuList"), new TypeReference<List<Sku>>() {});
        merchantService.updateGoods(id, goods, skuList);
        return Result.ok();
    }

    @PutMapping("/goods/{id}/status")
    public Result<Void> updateGoodsStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        merchantService.updateGoodsStatus(id, body.get("status"));
        return Result.ok();
    }

    @GetMapping("/orders")
    public Result<PageResult<Order>> orders(@RequestParam(required = false) Integer status,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(merchantService.ordersPage(status, page, size));
    }

    @PostMapping("/order/{id}/ship")
    public Result<Void> ship(@PathVariable Long id) {
        merchantService.ship(id);
        return Result.ok();
    }

    @GetMapping("/aftersales")
    public Result<PageResult<Aftersale>> aftersales(@RequestParam(required = false) Integer status,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return Result.ok(merchantService.aftersalesPage(status, page, size));
    }

    @PostMapping("/aftersale/{id}/handle")
    public Result<Void> handleAftersale(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Boolean agree = body.get("agree") == null ? null : Boolean.valueOf(String.valueOf(body.get("agree")));
        merchantService.handleAftersale(id, agree, (String) body.get("reply"));
        return Result.ok();
    }

    @GetMapping("/reviews")
    public Result<PageResult<Review>> reviews(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return Result.ok(merchantService.reviewsPage(page, size));
    }

    @PostMapping("/review/{id}/reply")
    public Result<Void> replyReview(@PathVariable Long id, @RequestBody Map<String, String> body) {
        merchantService.replyReview(id, body.get("content"));
        return Result.ok();
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        return Result.ok(merchantService.dashboard());
    }

    @GetMapping("/analytics")
    public Result<Map<String, Object>> analytics() {
        return Result.ok(merchantService.analytics());
    }

    private Goods buildGoods(Map<String, Object> body) {
        Goods g = new Goods();
        g.setName((String) body.get("name"));
        g.setSubtitle((String) body.get("subtitle"));
        if (body.get("categoryId") != null) {
            g.setCategoryId(Long.valueOf(String.valueOf(body.get("categoryId"))));
        }
        g.setMainImage((String) body.get("mainImage"));
        g.setImages((String) body.get("images"));
        g.setDetail((String) body.get("detail"));
        g.setPrice(new java.math.BigDecimal(String.valueOf(body.get("price"))));
        return g;
    }
}