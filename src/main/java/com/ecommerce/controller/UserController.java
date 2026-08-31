package com.ecommerce.controller;

import com.ecommerce.common.Result;
import com.ecommerce.common.UserContext;
import com.ecommerce.entity.Address;
import com.ecommerce.entity.BrowseHistory;
import com.ecommerce.entity.User;
import com.ecommerce.service.AddressService;
import com.ecommerce.service.GoodsService;
import com.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AddressService addressService;
    private final GoodsService goodsService;

    @GetMapping("/user/info")
    public Result<User> info() {
        return Result.ok(userService.current());
    }

    @PutMapping("/user/info")
    public Result<Void> updateInfo(@RequestBody User user) {
        userService.updateInfo(user);
        return Result.ok();
    }

    @PutMapping("/user/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> body) {
        userService.changePassword(body.get("oldPassword"), body.get("newPassword"));
        return Result.ok();
    }

    @GetMapping("/address")
    public Result<List<Address>> addresses() {
        return Result.ok(addressService.list(UserContext.get()));
    }

    @PostMapping("/address")
    public Result<Long> addAddress(@RequestBody Address address) {
        return Result.ok(addressService.add(UserContext.get(), address));
    }

    @PutMapping("/address")
    public Result<Void> updateAddress(@RequestBody Address address) {
        addressService.update(UserContext.get(), address);
        return Result.ok();
    }

    @DeleteMapping("/address/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id) {
        addressService.delete(UserContext.get(), id);
        return Result.ok();
    }

    @PutMapping("/address/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        addressService.setDefault(UserContext.get(), id);
        return Result.ok();
    }

    @GetMapping("/user/browse-history")
    public Result<List<BrowseHistory>> browseHistory() {
        return Result.ok(goodsService.browseHistory(UserContext.get()));
    }

    @DeleteMapping("/user/browse-history")
    public Result<Void> clearBrowse() {
        goodsService.clearBrowse(UserContext.get());
        return Result.ok();
    }
}