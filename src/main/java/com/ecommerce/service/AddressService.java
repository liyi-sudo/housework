package com.ecommerce.service;

import com.ecommerce.common.BizException;
import com.ecommerce.common.UserContext;
import com.ecommerce.entity.Address;
import com.ecommerce.mapper.AddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressMapper addressMapper;

    public List<Address> list(Long userId) {
        return addressMapper.selectByUserId(userId);
    }

    @Transactional
    public long add(Long userId, Address address) {
        address.setUserId(userId);
        if (address.getIsDefault() == 1 || addressMapper.countByUser(userId) == 0) {
            addressMapper.clearDefault(userId);
            address.setIsDefault(1);
        } else {
            address.setIsDefault(0);
        }
        addressMapper.insert(address);
        return address.getId();
    }

    @Transactional
    public void update(Long userId, Address address) {
        address.setUserId(userId);
        int rows = addressMapper.update(address);
        if (rows == 0) {
            throw new BizException("地址不存在");
        }
    }

    public void delete(Long userId, Long id) {
        addressMapper.deleteByUserIdAndId(userId, id);
    }

    @Transactional
    public void setDefault(Long userId, Long id) {
        addressMapper.clearDefault(userId);
        addressMapper.setDefault(userId, id);
    }

    public Address getDefaultOrFirst(Long userId) {
        List<Address> list = addressMapper.selectByUserId(userId);
        if (list.isEmpty()) {
            throw new BizException("请先添加收货地址");
        }
        return list.get(0);
    }
}