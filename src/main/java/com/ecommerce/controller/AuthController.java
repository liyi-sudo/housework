package com.ecommerce.controller;

import com.ecommerce.common.Result;
import com.ecommerce.common.UserContext;
import com.ecommerce.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterForm form) {
        authService.register(form.getPhone(), form.getPassword(), form.getNickname());
        return Result.ok();
    }

    @PostMapping("/sms-code")
    public Result<String> sendSmsCode(@RequestBody Map<String, String> body) {
        String code = authService.sendCode(body.get("phone"));
        return Result.ok(code);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginForm form, HttpServletRequest request) {
        String client = form.getClient() == null ? "WEB" : form.getClient();
        return Result.ok(authService.login(form.getPhone(), form.getPassword(), form.getCode(), client, request));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        UserContext.clear();
        return Result.ok();
    }

    public static class RegisterForm {
        private String phone;
        private String password;
        private String nickname;
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
    }

    public static class LoginForm {
        private String phone;
        private String password;
        private String code;
        private String client;
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getClient() { return client; }
        public void setClient(String client) { this.client = client; }
    }
}