package com.ecommerce.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderJob {

    private final OrderService orderService;

    @Scheduled(fixedDelay = 60000)
    public void cancelTimeoutPay() {
        try {
            int n = orderService.cancelTimeoutOrders(30);
            if (n > 0) {
                log.info("自动取消超时未支付订单 {} 单", n);
            }
        } catch (Exception e) {
            log.error("定时取消订单失败", e);
        }
    }
}