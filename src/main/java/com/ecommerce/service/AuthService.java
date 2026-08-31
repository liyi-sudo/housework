package com.ecommerce.service;

import com.ecommerce.auth.JwtUtil;
import com.ecommerce.common.BizException;
import com.ecommerce.entity.User;
import com.ecommerce.mapper.LoginLogMapper;
import com.ecommerce.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final LoginLogMapper loginLogMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private final Map<String, SmsCode> smsStore = new ConcurrentHashMap<>();

    private static class SmsCode {
        final String code;
        final LocalDateTime expireAt;
        SmsCode(String code, LocalDateTime expireAt) {
            this.code = code;
            this.expireAt = expireAt;
        }
    }

    public String sendCode(String phone) {
        if (phone == null || !phone.matches("^1\\d{10}$")) {
            throw new BizException("请输入正确的手机号");
        }
        String code = String.format("%06d", (int) (Math.random() * 1000000));
        smsStore.put(phone, new SmsCode(code, LocalDateTime.now().plusMinutes(10)));
        return code;
    }

    public void register(String phone, String password, String nickname) {
        if (userMapper.selectByPhone(phone) != null) {
            throw new BizException("该手机号已注册");
        }
        User user = new User();
        user.setPhone(phone);
        user.setPassword(encoder.encode(password));
        user.setNickname(nickname == null || nickname.isBlank() ? "用户" + phone.substring(Math.max(0, phone.length() - 4)) : nickname);
        user.setGender(0);
        userMapper.insert(user);
    }

    public Map<String, Object> login(String phone, String password, String code, String client, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        User user = userMapper.selectByPhone(phone);
        boolean byCode = code != null && !code.isBlank();
        if (byCode) {
            if (user == null) {
                user = new User();
                user.setPhone(phone);
                user.setPassword(encoder.encode(String.valueOf(System.currentTimeMillis())));
                user.setNickname("用户" + phone.substring(Math.max(0, phone.length() - 4)));
                user.setGender(0);
                userMapper.insert(user);
            }
            SmsCode sc = smsStore.get(phone);
            if (sc == null || !sc.code.equals(code) || sc.expireAt.isBefore(LocalDateTime.now())) {
                loginLogMapper.insert(buildLog(phone, client, ip, "FAIL", "验证码错误或已过期"));
                throw new BizException(400, "验证码错误或已过期");
            }
            smsStore.remove(phone);
            if (user.getStatus() != 1) {
                loginLogMapper.insert(buildLog(phone, client, ip, "DISABLED", "账号已被禁用"));
                throw new BizException(403, "账号已被禁用");
            }
            loginLogMapper.insert(buildLog(phone, client, ip, "SUCCESS", "验证码登录成功"));
            String token = jwtUtil.create(user.getId(), client);
            user.setPassword(null);
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            return data;
        }

        if (user == null || !encoder.matches(password, user.getPassword())) {
            loginLogMapper.insert(buildLog(phone, client, ip, "FAIL", "账号或密码错误"));
            throw new BizException(400, "账号或密码错误");
        }
        if (user.getStatus() != 1) {
            loginLogMapper.insert(buildLog(phone, client, ip, "DISABLED", "账号已被禁用"));
            throw new BizException(403, "账号已被禁用");
        }
        loginLogMapper.insert(buildLog(phone, client, ip, "SUCCESS", "登录成功"));
        String token = jwtUtil.create(user.getId(), client);
        user.setPassword(null);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        return data;
    }

    private com.ecommerce.entity.LoginLog buildLog(String username, String client, String ip, String result, String message) {
        com.ecommerce.entity.LoginLog log = new com.ecommerce.entity.LoginLog();
        log.setUsername(username);
        log.setUserType("USER");
        log.setClient(client);
        log.setIp(ip);
        log.setResult(result);
        log.setMessage(message);
        return log;
    }
}
