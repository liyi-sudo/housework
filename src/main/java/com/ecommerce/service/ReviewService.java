package com.ecommerce.service;

import com.ecommerce.common.BizException;
import com.ecommerce.common.PageResult;
import com.ecommerce.common.UserContext;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Review;
import com.ecommerce.mapper.OrderItemMapper;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    public void publish(Long orderId, Long orderItemId, Integer rating, String content, String images, boolean followUp) {
        Long userId = UserContext.get();
        if (followUp) {
            Review existed = reviewMapper.selectByOrderItemId(orderItemId, userId);
            if (existed == null) {
                throw new BizException("请先发表初评后再追评");
            }
            String prefix = existed.getContent() == null ? "" : existed.getContent();
            String mergedImages = existed.getImages();
            if (images != null && !images.isEmpty()) {
                mergedImages = (mergedImages == null || mergedImages.isEmpty()) ? images : mergedImages + "," + images;
            }
            String text = (content == null ? "" : content.trim());
            String newContent = prefix + "\n\n【追评】" + text;
            reviewMapper.updateFollowUp(existed.getId(), newContent, mergedImages);
            return;
        }

        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException("订单不存在");
        }
        if (order.getStatus() != OrderService.STATUS_FINISHED) {
            throw new BizException("订单完成后才能评价");
        }
        OrderItem item = orderItemMapper.selectById(orderItemId);
        if (item == null) {
            throw new BizException("订单商品不存在");
        }
        if (item.getReviewStatus() == 1) {
            throw new BizException("该商品已评价");
        }
        Review review = new Review();
        review.setUserId(userId);
        review.setOrderItemId(orderItemId);
        review.setGoodsId(item.getGoodsId());
        review.setShopId(order.getShopId());
        review.setRating(rating == null ? 5 : rating);
        review.setContent(content);
        review.setImages(images);
        reviewMapper.insert(review);
        orderItemMapper.updateReviewStatus(orderItemId);
    }

    public PageResult<Review> pageByGoods(Long goodsId, int page, int size) {
        int offset = (page - 1) * size;
        List<Review> list = reviewMapper.selectByGoodsId(goodsId, offset, size);
        long total = reviewMapper.countByGoodsId(goodsId);
        return PageResult.of(list, total, page, size);
    }

    public Map<String, Object> reviewTarget(Long goodsId) {
        Long userId = UserContext.get();
        Map<String, Object> res = new HashMap<>();
        res.put("canReview", false);
        res.put("alreadyReviewed", false);
        OrderItem item = orderItemMapper.selectReviewableByUserAndGoods(userId, goodsId);
        if (item == null) {
            OrderItem any = orderItemMapper.selectByGoodsAndUser(userId, goodsId);
            res.put("alreadyReviewed", any != null);
            return res;
        }
        res.put("canReview", true);
        res.put("orderId", item.getOrderId());
        res.put("orderItemId", item.getId());
        return res;
    }

    public List<Review> myReviews() {
        return reviewMapper.selectByUser(UserContext.get());
    }
}