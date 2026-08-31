package com.ecommerce.controller;

import com.ecommerce.common.Result;
import com.ecommerce.entity.Cart;
import com.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public Result<List<Cart>> list() {
        return Result.ok(cartService.list());
    }

    @PostMapping
    public Result<Void> add(@RequestBody CartForm form) {
        cartService.add(form.getSkuId(), form.getQuantity() == null ? 1 : form.getQuantity());
        return Result.ok();
    }

    @PutMapping("/{id}/quantity")
    public Result<Void> updateQuantity(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        cartService.updateQuantity(id, body.get("quantity"));
        return Result.ok();
    }

    @PutMapping("/check")
    public Result<Void> check(@RequestBody CheckForm form) {
        cartService.check(com.ecommerce.common.UserContext.get(), form.getChecked(), form.getIds());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cartService.delete(id);
        return Result.ok();
    }

    public static class CartForm {
        private Long skuId;
        private Integer quantity;
        public Long getSkuId() { return skuId; }
        public void setSkuId(Long skuId) { this.skuId = skuId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public static class CheckForm {
        private Integer checked;
        private List<Long> ids;
        public Integer getChecked() { return checked; }
        public void setChecked(Integer checked) { this.checked = checked; }
        public List<Long> getIds() { return ids; }
        public void setIds(List<Long> ids) { this.ids = ids; }
    }
}