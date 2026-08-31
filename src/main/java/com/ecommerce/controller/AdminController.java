package com.ecommerce.controller;

import com.ecommerce.common.PageResult;
import com.ecommerce.common.Result;
import com.ecommerce.entity.Admin;
import com.ecommerce.entity.Coupon;
import com.ecommerce.entity.Goods;
import com.ecommerce.entity.LoginLog;
import com.ecommerce.entity.Merchant;
import com.ecommerce.entity.MerchantApply;
import com.ecommerce.entity.Notice;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.Payment;
import com.ecommerce.entity.User;
import com.ecommerce.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        return Result.ok(adminService.login(body.get("username"), body.get("password"), request));
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.ok(adminService.overview());
    }

    @GetMapping("/merchants")
    public Result<PageResult<Merchant>> merchants(@RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) Integer status,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return Result.ok(adminService.merchantsPage(keyword, status, page, size));
    }

    @PutMapping("/merchant/{id}/status")
    public Result<Void> merchantStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminService.updateMerchantStatus(id, body.get("status"));
        return Result.ok();
    }

    @GetMapping("/applies")
    public Result<PageResult<MerchantApply>> applies(@RequestParam(required = false) Integer status,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        return Result.ok(adminService.appliesPage(status, page, size));
    }

    @PostMapping("/apply/{id}/review")
    public Result<Void> reviewApply(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Boolean approve = body.get("approve") == null ? null : Boolean.valueOf(String.valueOf(body.get("approve")));
        adminService.reviewApply(id, approve, (String) body.get("reason"));
        return Result.ok();
    }

    @GetMapping("/goods")
    public Result<PageResult<Goods>> goods(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Integer status,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return Result.ok(adminService.goodsPage(keyword, status, page, size));
    }

    @PutMapping("/goods/{id}/status")
    public Result<Void> goodsStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminService.updateGoodsStatus(id, body.get("status"));
        return Result.ok();
    }

    @PostMapping("/goods/{id}/review")
    public Result<Void> reviewGoods(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Boolean approve = body.get("approve") == null ? null : Boolean.valueOf(String.valueOf(body.get("approve")));
        adminService.reviewGoods(id, approve);
        return Result.ok();
    }

    @GetMapping("/orders")
    public Result<PageResult<Order>> orders(@RequestParam(required = false) String orderNo,
                                            @RequestParam(required = false) Integer status,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(adminService.ordersPage(orderNo, status, page, size));
    }

    @GetMapping("/payments")
    public Result<PageResult<Payment>> payments(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return Result.ok(adminService.paymentsPage(page, size));
    }

    @GetMapping("/login-logs")
    public Result<PageResult<LoginLog>> loginLogs(@RequestParam(required = false) String username,
                                                  @RequestParam(required = false) String userType,
                                                  @RequestParam(required = false) String result,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return Result.ok(adminService.loginLogsPage(username, userType, result, page, size));
    }

    @GetMapping("/admins")
    public Result<PageResult<Admin>> admins(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Integer status,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(adminService.adminsPage(keyword, status, page, size));
    }

    @PutMapping("/admin/{id}/status")
    public Result<Void> adminStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminService.updateAdminStatus(id, body.get("status"));
        return Result.ok();
    }

    @GetMapping("/users")
    public Result<PageResult<User>> users(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) Integer status,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return Result.ok(adminService.usersPage(keyword, status, page, size));
    }

    @PutMapping("/user/{id}/status")
    public Result<Void> userStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminService.updateUserStatus(id, body.get("status"));
        return Result.ok();
    }

    @GetMapping("/coupons")
    public Result<PageResult<Coupon>> coupons(@RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) Integer status,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return Result.ok(adminService.couponsPage(keyword, status, page, size));
    }

    @PostMapping("/coupon")
    public Result<Void> saveCoupon(@RequestBody Coupon coupon) {
        adminService.saveCoupon(coupon);
        return Result.ok();
    }

    @PutMapping("/coupon/{id}/status")
    public Result<Void> couponStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminService.updateCouponStatus(id, body.get("status"));
        return Result.ok();
    }

    @DeleteMapping("/coupon/{id}")
    public Result<Void> deleteCoupon(@PathVariable Long id) {
        adminService.deleteCoupon(id);
        return Result.ok();
    }

    @GetMapping("/notices")
    public Result<PageResult<Notice>> notices(@RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) Integer status,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return Result.ok(adminService.noticesPage(keyword, status, page, size));
    }

    @PostMapping("/notice")
    public Result<Void> saveNotice(@RequestBody Notice notice) {
        adminService.saveNotice(notice);
        return Result.ok();
    }

    @PutMapping("/notice/{id}/status")
    public Result<Void> noticeStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminService.updateNoticeStatus(id, body.get("status"));
        return Result.ok();
    }

    @DeleteMapping("/notice/{id}")
    public Result<Void> deleteNotice(@PathVariable Long id) {
        adminService.deleteNotice(id);
        return Result.ok();
    }
}