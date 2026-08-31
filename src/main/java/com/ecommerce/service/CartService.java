package com.ecommerce.service;

import com.ecommerce.common.BizException;
import com.ecommerce.common.UserContext;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.Goods;
import com.ecommerce.entity.Sku;
import com.ecommerce.mapper.CartMapper;
import com.ecommerce.mapper.GoodsMapper;
import com.ecommerce.mapper.SkuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartMapper cartMapper;
    private final GoodsMapper goodsMapper;
    private final SkuMapper skuMapper;

    public List<Cart> list() {
        return cartMapper.selectByUserId(UserContext.get());
    }

    public void add(Long skuId, int quantity) {
        Long userId = UserContext.get();
        Sku sku = skuMapper.selectById(skuId);
        if (sku == null) {
            throw new BizException("商品规格不存在");
        }
        Goods goods = goodsMapper.selectDetail(sku.getGoodsId());
        if (goods == null || goods.getStatus() != 1) {
            throw new BizException("商品已下架");
        }
        Cart exist = cartMapper.selectByUserAndSku(userId, skuId);
        if (exist != null) {
            cartMapper.updateQuantity(exist.getId(), userId, exist.getQuantity() + quantity);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setGoodsId(sku.getGoodsId());
            cart.setSkuId(skuId);
            cart.setQuantity(quantity);
            cart.setChecked(1);
            cartMapper.insert(cart);
        }
    }

    public void updateQuantity(Long id, int quantity) {
        if (quantity <= 0) {
            throw new BizException("数量不合法");
        }
        cartMapper.updateQuantity(id, UserContext.get(), quantity);
    }

    public void check(Long userId, Integer checked, List<Long> ids) {
        cartMapper.updateChecked(userId, checked, ids);
    }

    public void delete(Long id) {
        cartMapper.deleteByUserAndId(UserContext.get(), id);
    }
}