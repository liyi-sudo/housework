package com.ecommerce.service;

import com.ecommerce.auth.JwtUtil;
import com.ecommerce.common.BizException;
import com.ecommerce.common.PageResult;
import com.ecommerce.entity.Admin;
import com.ecommerce.entity.Coupon;
import com.ecommerce.entity.Goods;
import com.ecommerce.entity.LoginLog;
import com.ecommerce.entity.Merchant;
import com.ecommerce.entity.MerchantApply;
import com.ecommerce.entity.Notice;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.Payment;
import com.ecommerce.entity.Shop;
import com.ecommerce.entity.User;
import com.ecommerce.mapper.AdminMapper;
import com.ecommerce.mapper.CouponMapper;
import com.ecommerce.mapper.GoodsMapper;
import com.ecommerce.mapper.LoginLogMapper;
import com.ecommerce.mapper.MerchantApplyMapper;
import com.ecommerce.mapper.MerchantMapper;
import com.ecommerce.mapper.NoticeMapper;
import com.ecommerce.mapper.OrderItemMapper;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.mapper.PaymentMapper;
import com.ecommerce.mapper.ShopMapper;
import com.ecommerce.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminMapper adminMapper;
    private final MerchantMapper merchantMapper;
    private final ShopMapper shopMapper;
    private final MerchantApplyMapper merchantApplyMapper;
    private final GoodsMapper goodsMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final PaymentMapper paymentMapper;
    private final LoginLogMapper loginLogMapper;
    private final UserMapper userMapper;
    private final CouponMapper couponMapper;
    private final NoticeMapper noticeMapper;
    private final JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public Map<String, Object> login(String username, String password, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        Admin admin = adminMapper.selectByUsername(username);
        if (admin == null) {
            loginLogMapper.insert(buildLog(username, ip, "FAIL", "账号不存在"));
            throw new BizException(400, "账号或密码错误");
        }
        if (!encoder.matches(password, admin.getPassword())) {
            loginLogMapper.insert(buildLog(username, ip, "FAIL", "密码错误"));
            throw new BizException(400, "账号或密码错误");
        }
        if (admin.getStatus() != 1) {
            loginLogMapper.insert(buildLog(username, ip, "DISABLED", "账号已被停用"));
            throw new BizException(403, "账号已被停用");
        }
        loginLogMapper.insert(buildLog(username, ip, "SUCCESS", "登录成功"));
        String token = jwtUtil.create(admin.getId(), "ADMIN");
        admin.setPassword(null);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("admin", admin);
        return data;
    }

    private LoginLog buildLog(String username, String ip, String result, String message) {
        LoginLog log = new LoginLog();
        log.setUsername(username);
        log.setUserType("ADMIN");
        log.setClient("PC");
        log.setIp(ip);
        log.setResult(result);
        log.setMessage(message);
        return log;
    }

    public Map<String, Object> overview() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userMapper.countAll());
        data.put("merchantCount", merchantMapper.countPage(null, null));
        data.put("goodsCount", goodsMapper.countAllPage(null, null));
        data.put("pendingGoods", goodsMapper.countAllPage(null, 3));
        data.put("pendingApply", merchantApplyMapper.countPage(0));
        data.put("orderCount", orderMapper.countAllPage(null, null));
        data.put("orderToday", orderMapper.countToday());
        data.put("saleAmount", orderMapper.sumPaid());
        data.put("trend", buildTrend());
        data.put("orderStatusDist", buildOrderStatusDist());
        return data;
    }

    private Map<String, Object> buildTrend() {
        List<Map<String, Object>> rows = orderMapper.select7DayTrend(7);
        Map<String, Map<String, Object>> byDay = new HashMap<>();
        for (Map<String, Object> r : rows) {
            byDay.put(String.valueOf(r.get("day")), r);
        }
        List<String> days = new ArrayList<>();
        List<Long> orderCounts = new ArrayList<>();
        List<Double> saleAmounts = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            String day = today.minusDays(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            days.add(day);
            Map<String, Object> row = byDay.get(day);
            long oc = row == null ? 0 : ((Number) row.get("orderCount")).longValue();
            double sa = row == null ? 0 : ((Number) row.get("saleAmount")).doubleValue();
            orderCounts.add(oc);
            saleAmounts.add(sa);
        }
        Map<String, Object> trend = new HashMap<>();
        trend.put("days", days);
        trend.put("orderCounts", orderCounts);
        trend.put("saleAmounts", saleAmounts);
        return trend;
    }

    private List<Map<String, Object>> buildOrderStatusDist() {
        List<Map<String, Object>> rows = orderMapper.selectStatusDist();
        Map<Integer, String> statusText = new HashMap<>();
        statusText.put(0, "待付款");
        statusText.put(1, "待发货");
        statusText.put(2, "待收货");
        statusText.put(3, "已完成");
        statusText.put(4, "已取消");
        statusText.put(5, "退款");
        List<Map<String, Object>> dist = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Integer status = ((Number) r.get("status")).intValue();
            long cnt = ((Number) r.get("cnt")).longValue();
            Map<String, Object> item = new HashMap<>();
            item.put("name", statusText.getOrDefault(status, "状态" + status));
            item.put("value", cnt);
            dist.add(item);
        }
        return dist;
    }

    public PageResult<Coupon> couponsPage(String keyword, Integer status, int page, int size) {
        int offset = (page - 1) * size;
        List<Coupon> list = couponMapper.selectPage(keyword, status, offset, size);
        long total = couponMapper.countPage(keyword, status);
        list.forEach(c -> c.setClaimed(false));
        return PageResult.of(list, total, page, size);
    }

    public void saveCoupon(Coupon coupon) {
        if (coupon.getName() == null || coupon.getName().isBlank()) {
            throw new BizException("请填写优惠券名称");
        }
        if (coupon.getAmount() == null) {
            throw new BizException("请填写优惠金额");
        }
        if (coupon.getStatus() == null) {
            coupon.setStatus(1);
        }
        if (coupon.getId() == null) {
            if (coupon.getTotal() == null) {
                coupon.setTotal(0);
            }
            coupon.setReceived(0);
            couponMapper.insert(coupon);
        } else {
            couponMapper.update(coupon);
        }
    }

    public void updateCouponStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BizException("非法的优惠券状态");
        }
        couponMapper.updateStatus(id, status);
    }

    public void deleteCoupon(Long id) {
        couponMapper.deleteById(id);
    }

    public PageResult<Notice> noticesPage(String keyword, Integer status, int page, int size) {
        int offset = (page - 1) * size;
        List<Notice> list = noticeMapper.selectPage(keyword, status, offset, size);
        long total = noticeMapper.countPage(keyword, status);
        return PageResult.of(list, total, page, size);
    }

    public void saveNotice(Notice notice) {
        if (notice.getTitle() == null || notice.getTitle().isBlank()) {
            throw new BizException("请填写公告标题");
        }
        if (notice.getStatus() == null) {
            notice.setStatus(1);
        }
        if (notice.getSort() == null) {
            notice.setSort(0);
        }
        if (notice.getId() == null) {
            noticeMapper.insert(notice);
        } else {
            noticeMapper.update(notice);
        }
    }

    public void updateNoticeStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BizException("非法的公告状态");
        }
        noticeMapper.updateStatus(id, status);
    }

    public void deleteNotice(Long id) {
        noticeMapper.deleteById(id);
    }

    public PageResult<Merchant> merchantsPage(String keyword, Integer status, int page, int size) {
        int offset = (page - 1) * size;
        List<Merchant> list = merchantMapper.selectPage(keyword, status, offset, size);
        list.forEach(m -> m.setPassword(null));
        long total = merchantMapper.countPage(keyword, status);
        return PageResult.of(list, total, page, size);
    }

    public void updateMerchantStatus(Long id, Integer status) {
        if (status == null || (status != 1 && status != 2)) {
            throw new BizException("非法的账号状态");
        }
        merchantMapper.updateStatus(id, status);
    }

    public PageResult<MerchantApply> appliesPage(Integer status, int page, int size) {
        int offset = (page - 1) * size;
        List<MerchantApply> list = merchantApplyMapper.selectPage(status, offset, size);
        list.forEach(a -> a.setStatusText(applyText(a.getStatus())));
        long total = merchantApplyMapper.countPage(status);
        return PageResult.of(list, total, page, size);
    }

    private String applyText(Integer status) {
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "已通过";
            case 2 -> "已驳回";
            default -> "未知";
        };
    }

    @Transactional
    public void reviewApply(Long id, Boolean approve, String reason) {
        MerchantApply apply = merchantApplyMapper.selectById(id);
        if (apply == null) {
            throw new BizException("申请单不存在");
        }
        if (apply.getStatus() != 0) {
            throw new BizException("该申请已审核");
        }
        if (approve != null && approve) {
            merchantApplyMapper.review(id, 1, reason);
            merchantMapper.updateStatus(apply.getMerchantId(), 1);
            if (shopMapper.selectByMerchantId(apply.getMerchantId()) == null) {
                Shop shop = new Shop();
                shop.setMerchantId(apply.getMerchantId());
                shop.setName(apply.getShopName());
                shopMapper.insert(shop);
            }
        } else {
            if (reason == null || reason.isBlank()) {
                throw new BizException("请填写驳回原因");
            }
            merchantApplyMapper.review(id, 2, reason);
        }
    }

    public PageResult<Goods> goodsPage(String keyword, Integer status, int page, int size) {
        int offset = (page - 1) * size;
        List<Goods> list = goodsMapper.selectAllPage(keyword, status, offset, size);
        long total = goodsMapper.countAllPage(keyword, status);
        return PageResult.of(list, total, page, size);
    }

    public void updateGoodsStatus(Long id, Integer status) {
        if (status == null || (status != 1 && status != 2 && status != 3 && status != 4)) {
            throw new BizException("非法的商品状态");
        }
        int rows = goodsMapper.updateAdminStatus(id, status);
        if (rows == 0) {
            throw new BizException("商品不存在");
        }
    }

    public void reviewGoods(Long id, Boolean approve) {
        Goods goods = goodsMapper.selectDetail(id);
        if (goods == null) {
            throw new BizException("商品不存在");
        }
        if (goods.getStatus() != 3) {
            throw new BizException("仅「待审核」商品可进行审核");
        }
        int target = approve != null && approve ? 1 : 4;
        goodsMapper.updateAdminStatus(id, target);
    }

    public PageResult<Order> ordersPage(String orderNo, Integer status, int page, int size) {
        int offset = (page - 1) * size;
        List<Order> list = orderMapper.selectAllPage(orderNo, status, offset, size);
        list.forEach(o -> {
            o.setItems(orderItemMapper.selectByOrderId(o.getId()));
            o.setStatusText(OrderService.statusText(o.getStatus()));
        });
        long total = orderMapper.countAllPage(orderNo, status);
        return PageResult.of(list, total, page, size);
    }

    public PageResult<Payment> paymentsPage(int page, int size) {
        int offset = (page - 1) * size;
        List<Payment> list = paymentMapper.selectPage(offset, size);
        list.forEach(p -> {
            if (p.getStatus() == 1) p.setStatusText("支付成功");
            else p.setStatusText("待支付");
        });
        long total = paymentMapper.countPage();
        return PageResult.of(list, total, page, size);
    }

    public PageResult<LoginLog> loginLogsPage(String username, String userType, String result, int page, int size) {
        int offset = (page - 1) * size;
        List<LoginLog> list = loginLogMapper.selectPage(username, userType, null, result, offset, size);
        long total = loginLogMapper.countPage(username, userType, null, result);
        return PageResult.of(list, total, page, size);
    }

    public PageResult<Admin> adminsPage(String keyword, Integer status, int page, int size) {
        int offset = (page - 1) * size;
        List<Admin> list = adminMapper.selectPage(keyword, status, offset, size);
        list.forEach(a -> a.setPassword(null));
        long total = adminMapper.countPage(keyword, status);
        return PageResult.of(list, total, page, size);
    }

    public void updateAdminStatus(Long id, Integer status) {
        if (status == null || (status != 1 && status != 2)) {
            throw new BizException("非法的账号状态");
        }
        adminMapper.updateStatus(id, status);
    }

    public PageResult<User> usersPage(String keyword, Integer status, int page, int size) {
        int offset = (page - 1) * size;
        List<User> list = userMapper.selectPage(keyword, status, offset, size);
        list.forEach(u -> u.setPassword(null));
        long total = userMapper.countPage(keyword, status);
        return PageResult.of(list, total, page, size);
    }

    public void updateUserStatus(Long id, Integer status) {
        if (status == null || (status != 1 && status != 2)) {
            throw new BizException("非法的账号状态");
        }
        userMapper.updateStatus(id, status);
    }
}