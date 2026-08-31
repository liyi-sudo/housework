package com.ecommerce.service;

import com.ecommerce.common.BizException;
import com.ecommerce.common.PageResult;
import com.ecommerce.common.UserContext;
import com.ecommerce.entity.*;
import com.ecommerce.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    public static final int STATUS_WAIT_PAY = 0;
    public static final int STATUS_WAIT_SHIP = 1;
    public static final int STATUS_WAIT_RECEIVE = 2;
    public static final int STATUS_FINISHED = 3;
    public static final int STATUS_CANCELED = 4;
    public static final int STATUS_AFTERSALE = 5;

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartMapper cartMapper;
    private final SkuMapper skuMapper;
    private final GoodsMapper goodsMapper;
    private final PaymentMapper paymentMapper;
    private final LogisticsMapper logisticsMapper;
    private final AddressMapper addressMapper;
    private final AddressService addressService;
    private final UserCouponMapper userCouponMapper;

    @Transactional
    public List<Order> submit(Long userId, Long addressId, List<Long> cartIds, Long userCouponId, String remark) {
        if (cartIds == null || cartIds.isEmpty()) {
            throw new BizException("请选择要结算的商品");
        }
        Address addr = addressId == null ? addressService.getDefaultOrFirst(userId)
                : addressMapper.selectById(addressId);
        if (addr == null || !addr.getUserId().equals(userId)) {
            throw new BizException("收货地址无效");
        }
        List<Cart> items = cartMapper.selectCheckoutItems(userId, cartIds);
        if (items.isEmpty()) {
            throw new BizException("没有可结算的商品");
        }
        items.forEach(it -> {
            if (it.getGoodsStatus() == null || it.getGoodsStatus() != 1) {
                throw new BizException("商品「" + it.getGoodsName() + "」已下架");
            }
            if (it.getQuantity() > it.getStock()) {
                throw new BizException("商品「" + it.getGoodsName() + "」库存不足");
            }
        });

        String snapshot = addr.getReceiverName() + " " + addr.getReceiverPhone() + " "
                + addr.getProvince() + addr.getCity() + addr.getDistrict() + addr.getDetail();

        Map<Long, List<Cart>> byShop = items.stream().collect(Collectors.groupingBy(Cart::getShopId));

        UserCoupon userCoupon = null;
        if (userCouponId != null) {
            userCoupon = userCouponMapper.selectById(userCouponId, userId);
            if (userCoupon == null || userCoupon.getStatus() != 0) {
                throw new BizException("优惠券不可用");
            }
        }

        List<Order> created = new ArrayList<>();
        boolean discountUsed = false;
        for (Map.Entry<Long, List<Cart>> entry : byShop.entrySet()) {
            List<Cart> shopItems = entry.getValue();
            BigDecimal total = shopItems.stream()
                    .map(it -> it.getPrice().multiply(BigDecimal.valueOf(it.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal discount = BigDecimal.ZERO;
            Long appliedUserCouponId = null;
            if (userCoupon != null && !discountUsed
                    && total.compareTo(userCoupon.getThreshold()) >= 0) {
                discount = userCoupon.getAmount().min(total);
                discountUsed = true;
                appliedUserCouponId = userCoupon.getId();
            }

            BigDecimal freight = total.compareTo(new BigDecimal("99")) >= 0 ? BigDecimal.ZERO : new BigDecimal("8");
            BigDecimal pay = total.subtract(discount).add(freight);

            Order order = new Order();
            order.setOrderNo(genOrderNo());
            order.setUserId(userId);
            order.setShopId(entry.getKey());
            order.setTotalAmount(total);
            order.setFreight(freight);
            order.setDiscount(discount);
            order.setPayAmount(pay);
            order.setAddrSnapshot(snapshot);
            order.setCouponId(appliedUserCouponId);
            order.setRemark(remark);
            order.setStatus(STATUS_WAIT_PAY);
            orderMapper.insert(order);

            List<OrderItem> orderItems = new ArrayList<>();
            for (Cart it : shopItems) {
                OrderItem oi = new OrderItem();
                oi.setOrderId(order.getId());
                oi.setGoodsId(it.getGoodsId());
                oi.setGoodsName(it.getGoodsName());
                oi.setSkuId(it.getSkuId());
                oi.setSkuSpec(it.getSkuSpec());
                oi.setCoverImage(it.getMainImage());
                oi.setPrice(it.getPrice());
                oi.setQuantity(it.getQuantity());
                orderItems.add(oi);
            }
            orderItemMapper.insertBatch(orderItems);
            order.setItems(orderItems);

            if (appliedUserCouponId != null) {
                userCouponMapper.markUsed(appliedUserCouponId, order.getId());
            }
            created.add(order);
            for (Cart it : shopItems) {
                cartMapper.deleteByUserAndId(userId, it.getId());
            }
        }
        return created;
    }

    @Transactional
    public void pay(Long orderId) {
        Long userId = UserContext.get();
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException("订单不存在");
        }
        if (order.getStatus() != STATUS_WAIT_PAY) {
            throw new BizException("订单状态不可支付");
        }
        orderMapper.updateStatus(orderId, STATUS_WAIT_PAY, STATUS_WAIT_SHIP, userId);

        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem it : items) {
            int rows = skuMapper.reduceStock(it.getSkuId(), it.getQuantity());
            if (rows == 0) {
                orderMapper.updateStatus(orderId, STATUS_WAIT_SHIP, STATUS_CANCELED, userId);
                throw new BizException("商品库存不足，订单已取消");
            }
            goodsMapper.addSales(it.getGoodsId(), it.getQuantity());
        }

        Payment payment = new Payment();
        payment.setTradeNo("PAY" + genOrderNo());
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setAmount(order.getPayAmount());
        payment.setMethod("BALANCE");
        payment.setStatus(1);
        payment.setPayTime(LocalDateTime.now());
        paymentMapper.insert(payment);

        Logistics logistics = new Logistics();
        logistics.setOrderId(orderId);
        logistics.setLogisticsNo("SF" + System.currentTimeMillis());
        logistics.setCompany("模拟快递");
        logistics.setStatus(0);
        logistics.setTrace("包裹已揽收;商品从仓库发出");
        logisticsMapper.insert(logistics);
    }

    public PageResult<Order> page(Integer status, int page, int size) {
        Long userId = UserContext.get();
        int offset = (page - 1) * size;
        List<Order> list = orderMapper.selectPageByUser(userId, status, offset, size);
        list.forEach(o -> o.setStatusText(statusText(o.getStatus())));
        list.forEach(o -> o.setItems(orderItemMapper.selectByOrderId(o.getId())));
        long total = orderMapper.countByUser(userId, status);
        return PageResult.of(list, total, page, size);
    }

    public Order detail(Long id) {
        Long userId = UserContext.get();
        Order order = orderMapper.selectFullById(id, userId);
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        order.setItems(orderItemMapper.selectByOrderId(id));
        order.setStatusText(statusText(order.getStatus()));
        return order;
    }

    public void cancel(Long orderId) {
        Long userId = UserContext.get();
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException("订单不存在");
        }
        if (order.getStatus() != STATUS_WAIT_PAY) {
            throw new BizException("当前状态不可取消");
        }
        orderMapper.updateStatus(orderId, STATUS_WAIT_PAY, STATUS_CANCELED, userId);
    }

    public void confirmReceive(Long orderId) {
        Long userId = UserContext.get();
        int rows = orderMapper.updateStatus(orderId, STATUS_WAIT_RECEIVE, STATUS_FINISHED, userId);
        if (rows == 0) {
            throw new BizException("订单状态不可确认收货");
        }
    }

    public Logistics logistics(Long orderId) {
        Long userId = UserContext.get();
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException("订单不存在");
        }
        Logistics lg = logisticsMapper.selectByOrderId(orderId);
        if (lg == null) {
            throw new BizException("暂无物流信息");
        }
        if (order.getStatus() == STATUS_FINISHED) {
            lg.setStatus(2);
            lg.setTrace(lg.getTrace() + ";包裹已签收");
        } else if (order.getStatus() >= STATUS_WAIT_SHIP) {
            lg.setStatus(1);
            if (!lg.getTrace().contains("运输中")) {
                lg.setTrace(lg.getTrace() + ";运输中，正在派送");
            }
        }
        return lg;
    }

    public int cancelTimeoutOrders(int timeoutMin) {
        List<Order> list = orderMapper.selectTimeoutOrders(timeoutMin);
        int n = 0;
        for (Order o : list) {
            n += orderMapper.cancelTimeoutOrder(o.getId());
        }
        return n;
    }

    public static String statusText(Integer status) {
        if (status == null) return "-";
        return switch (status) {
            case STATUS_WAIT_PAY -> "待付款";
            case STATUS_WAIT_SHIP -> "待发货";
            case STATUS_WAIT_RECEIVE -> "待收货";
            case STATUS_FINISHED -> "已完成";
            case STATUS_CANCELED -> "已取消";
            case STATUS_AFTERSALE -> "售后中";
            default -> "-";
        };
    }

    private static String genOrderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000));
    }
}