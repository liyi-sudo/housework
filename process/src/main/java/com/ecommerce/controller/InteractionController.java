package com.ecommerce.controller;

import com.ecommerce.common.Result;
import com.ecommerce.entity.Aftersale;
import com.ecommerce.entity.Review;
import com.ecommerce.service.AftersaleService;
import com.ecommerce.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InteractionController {

    private final ReviewService reviewService;
    private final AftersaleService aftersaleService;

    @PostMapping("/review")
    public Result<Void> publishReview(@RequestBody ReviewForm form) {
        reviewService.publish(form.getOrderId(), form.getOrderItemId(), form.getRating(),
                form.getContent(), form.getImages(), form.isFollowUp());
        return Result.ok();
    }

    @GetMapping("/review/mine")
    public Result<List<Review>> myReviews() {
        return Result.ok(reviewService.myReviews());
    }

    @GetMapping("/review/goods/{goodsId}/target")
    public Result<java.util.Map<String, Object>> reviewTarget(@PathVariable Long goodsId) {
        return Result.ok(reviewService.reviewTarget(goodsId));
    }

    @PostMapping("/aftersale")
    public Result<Void> apply(@RequestBody Aftersale form) {
        aftersaleService.apply(form);
        return Result.ok();
    }

    @GetMapping("/aftersale")
    public Result<List<Aftersale>> aftersaleList() {
        return Result.ok(aftersaleService.list());
    }

    @GetMapping("/coupon/claimable")
    public Result<List<com.ecommerce.entity.Coupon>> claimable() {
        return Result.ok(couponService.claimable());
    }

    @PostMapping("/coupon/{id}/claim")
    public Result<Void> claim(@PathVariable Long id) {
        couponService.claim(id);
        return Result.ok();
    }

    @GetMapping("/coupon/mine")
    public Result<List<com.ecommerce.entity.UserCoupon>> myCoupons(@RequestParam(required = false) Integer status) {
        return Result.ok(couponService.mine(status));
    }

    private final com.ecommerce.service.CouponService couponService;

    public static class ReviewForm {
        private Long orderId;
        private Long orderItemId;
        private Integer rating;
        private String content;
        private String images;
        private boolean followUp;
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public Long getOrderItemId() { return orderItemId; }
        public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }
        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getImages() { return images; }
        public void setImages(String images) { this.images = images; }
        public boolean isFollowUp() { return followUp; }
        public void setFollowUp(boolean followUp) { this.followUp = followUp; }
    }
}