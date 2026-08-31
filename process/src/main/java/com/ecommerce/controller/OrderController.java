package com.ecommerce.controller;

import com.ecommerce.common.PageResult;
import com.ecommerce.common.Result;
import com.ecommerce.common.UserContext;
import com.ecommerce.entity.Logistics;
import com.ecommerce.entity.Order;
import com.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/submit")
    public Result<List<Order>> submit(@RequestBody SubmitForm form) {
        List<Order> orders = orderService.submit(UserContext.get(), form.getAddressId(),
                form.getCartIds(), form.getUserCouponId(), form.getRemark());
        return Result.ok(orders);
    }

    @GetMapping
    public Result<PageResult<Order>> page(@RequestParam(required = false) String status,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "5") int size) {
        Integer statusInt = null;
        if (status != null && !status.isBlank()) {
            try {
                statusInt = Integer.valueOf(status.trim());
            } catch (NumberFormatException ignored) {
                statusInt = null;
            }
        }
        return Result.ok(orderService.page(statusInt, page, size));
    }

    @GetMapping("/{id}")
    public Result<Order> detail(@PathVariable Long id) {
        return Result.ok(orderService.detail(id));
    }

    @PostMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        orderService.pay(id);
        return Result.ok();
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return Result.ok();
    }

    @PostMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        orderService.confirmReceive(id);
        return Result.ok();
    }

    @GetMapping("/{id}/logistics")
    public Result<Logistics> logistics(@PathVariable Long id) {
        return Result.ok(orderService.logistics(id));
    }

    public static class SubmitForm {
        private Long addressId;
        private List<Long> cartIds;
        private Long userCouponId;
        private String remark;
        public Long getAddressId() { return addressId; }
        public void setAddressId(Long addressId) { this.addressId = addressId; }
        public List<Long> getCartIds() { return cartIds; }
        public void setCartIds(List<Long> cartIds) { this.cartIds = cartIds; }
        public Long getUserCouponId() { return userCouponId; }
        public void setUserCouponId(Long userCouponId) { this.userCouponId = userCouponId; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }
}