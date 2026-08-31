package com.ecommerce.service;

import com.ecommerce.common.BizException;
import com.ecommerce.common.UserContext;
import com.ecommerce.entity.Aftersale;
import com.ecommerce.entity.Order;
import com.ecommerce.mapper.AftersaleMapper;
import com.ecommerce.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AftersaleService {

    private final AftersaleMapper aftersaleMapper;
    private final OrderMapper orderMapper;

    public void apply(Aftersale form) {
        Long userId = UserContext.get();
        Order order = orderMapper.selectById(form.getOrderId());
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException("订单不存在");
        }
        if (order.getStatus() == OrderService.STATUS_WAIT_PAY) {
            throw new BizException("订单未支付，请先取消订单");
        }
        if (aftersaleMapper.countActiveByOrder(form.getOrderId()) > 0) {
            throw new BizException("该订单已有进行中的售后申请");
        }
        Aftersale as = new Aftersale();
        as.setOrderId(form.getOrderId());
        as.setOrderNo(order.getOrderNo());
        as.setUserId(userId);
        as.setShopId(order.getShopId());
        as.setType(form.getType());
        as.setReason(form.getReason());
        as.setRefundAmount(order.getPayAmount());
        as.setImages(form.getImages());
        aftersaleMapper.insert(as);
        int target;
        if (order.getStatus() == OrderService.STATUS_WAIT_SHIP
                || order.getStatus() == OrderService.STATUS_WAIT_RECEIVE
                || order.getStatus() == OrderService.STATUS_FINISHED) {
            target = OrderService.STATUS_AFTERSALE;
        } else {
            target = order.getStatus();
        }
        if (target == OrderService.STATUS_AFTERSALE) {
            orderMapper.updateStatus(form.getOrderId(), order.getStatus(), target, userId);
        }
    }

    public List<Aftersale> list() {
        return aftersaleMapper.selectByUser(UserContext.get());
    }
}