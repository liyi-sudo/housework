package com.ecommerce.service;

import com.ecommerce.entity.LoginLog;
import com.ecommerce.mapper.LoginLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginLogService {

    private final LoginLogMapper loginLogMapper;

    public void record(String username, String userType, String client, String ip, String result, String message) {
        LoginLog log = new LoginLog();
        log.setUsername(username);
        log.setUserType(userType);
        log.setClient(client);
        log.setIp(ip);
        log.setResult(result);
        log.setMessage(message);
        loginLogMapper.insert(log);
    }
}