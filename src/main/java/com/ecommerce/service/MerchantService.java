package com.ecommerce.service;

import com.ecommerce.auth.JwtUtil;
import com.ecommerce.common.BizException;
import com.ecommerce.common.PageResult;
import com.ecommerce.common.UserContext;
import com.ecommerce.entity.Aftersale;
import com.ecommerce.entity.Goods;
import com.ecommerce.entity.Merchant;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.Review;
import com.ecommerce.entity.Shop;
import com.ecommerce.entity.Sku;
import com.ecommerce.mapper.AftersaleMapper;
import com.ecommerce.mapper.GoodsMapper;
import com.ecommerce.mapper.MerchantMapper;
import com.ecommerce.mapper.OrderItemMapper;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.mapper.ReviewMapper;
import com.ecommerce.mapper.ShopMapper;
import com.ecommerce.mapper.SkuMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantMapper merchantMapper;
    private final ShopMapper shopMapper;
    private final GoodsMapper goodsMapper;
    private final SkuMapper skuMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final AftersaleMapper aftersaleMapper;
    private final ReviewMapper reviewMapper;
    private final LoginLogService loginLogService;
    private final JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private Long merchantId() {
        return UserContext.get();
    }

    public Map<String, Object> login(String account, String password, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        Merchant merchant = merchantMapper.selectByAccount(account);
        if (merchant == null) {
            loginLogService.record(account, "MERCHANT", "PC", ip, "FAIL", "账号不存在");
            throw new BizException(400, "账号或密码错误");
        }
        if (!encoder.matches(password, merchant.getPassword())) {
            loginLogService.record(account, "MERCHANT", "PC", ip, "FAIL", "密码错误");
            throw new BizException(400, "账号或密码错误");
        }
        if (merchant.getStatus() != 1) {
            loginLogService.record(account, "MERCHANT", "PC", ip, "DISABLED", "账号已被禁用");
            throw new BizException(403, "账号已被禁用");
        }
        loginLogService.record(account, "MERCHANT", "PC", ip, "SUCCESS", "登录成功");
        String token = jwtUtil.create(merchant.getId(), "MERCHANT");
        Shop shop = shopMapper.selectByMerchantId(merchant.getId());
        merchant.setPassword(null);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("merchant", merchant);
        data.put("shop", shop);
        return data;
    }

    public Shop currentShop() {
        Shop shop = shopMapper.selectByMerchantId(merchantId());
        if (shop == null) {
            throw new BizException("店铺不存在");
        }
        return shop;
    }

    public void updateShop(String name, String logo, String banner, String intro) {
        Shop shop = currentShop();
        Shop form = new Shop();
        form.setName(name);
        form.setLogo(logo);
        form.setBanner(banner);
        form.setIntro(intro);
        shopMapper.updateByMerchantId(shop.getId(), merchantId(), form);
    }

    public PageResult<Goods> goodsPage(String keyword, Integer status, int page, int size) {
        int offset = (page - 1) * size;
        List<Goods> list = goodsMapper.selectByShop(merchantId(), keyword, status, offset, size);
        long total = goodsMapper.countByShop(merchantId(), keyword, status);
        return PageResult.of(list, total, page, size);
    }

    public Goods goodsDetail(Long id) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null || !goods.getShopId().equals(merchantId())) {
            throw new BizException("商品不存在");
        }
        goods.setSkuList(skuMapper.selectByGoodsId(id));
        return goods;
    }

    @Transactional
    public Goods saveGoods(Goods form, List<Sku> skuList) {
        if (form.getName() == null || form.getName().isBlank()) {
            throw new BizException("请填写商品名称");
        }
        if (skuList == null || skuList.isEmpty()) {
            throw new BizException("请至少配置一个规格(SKU)");
        }
        form.setShopId(merchantId());
        form.setStatus(3);
        form.setSales(0);
        goodsMapper.insert(form);
        skuList.forEach(s -> s.setGoodsId(form.getId()));
        skuMapper.insertBatch(skuList);
        return form;
    }

    @Transactional
    public void updateGoods(Long id, Goods form, List<Sku> skuList) {
        Goods exist = goodsDetail(id);
        form.setId(id);
        form.setShopId(exist.getShopId());
        goodsMapper.update(form);
        goodsMapper.updateAdminStatus(id, 3);
        skuMapper.deleteByGoodsId(id);
        if (skuList != null && !skuList.isEmpty()) {
            skuList.forEach(s -> s.setGoodsId(id));
            skuMapper.insertBatch(skuList);
        }
    }

    public void updateGoodsStatus(Long id, Integer status) {
        if (status == null || (status != 2 && status != 3)) {
            throw new BizException("非法的商品状态");
        }
        Goods exist = goodsDetail(id);
        if (status == 2) {
            if (exist.getStatus() != 1) {
                throw new BizException("仅「在售」商品可下架");
            }
        } else {
            if (exist.getStatus() != 2 && exist.getStatus() != 4) {
                throw new BizException("仅「已下架/已驳回」商品可提交审核");
            }
        }
        int rows = goodsMapper.updateStatus(id, merchantId(), status);
        if (rows == 0) {
            throw new BizException("商品不存在或无操作权限");
        }
    }

    public PageResult<Order> ordersPage(Integer status, int page, int size) {
        int offset = (page - 1) * size;
        List<Order> list = orderMapper.selectPageByShop(merchantId(), status, offset, size);
        list.forEach(o -> {
            o.setItems(orderItemMapper.selectByOrderId(o.getId()));
            o.setStatusText(OrderService.statusText(o.getStatus()));
        });
        long total = orderMapper.countByShop(merchantId(), status);
        return PageResult.of(list, total, page, size);
    }

    public void ship(Long orderId) {
        int rows = orderMapper.ship(orderId, merchantId());
        if (rows == 0) {
            throw new BizException("订单不存在或状态不允许发货");
        }
    }

    public PageResult<Aftersale> aftersalesPage(Integer status, int page, int size) {
        int offset = (page - 1) * size;
        List<Aftersale> list = aftersaleMapper.selectByShop(merchantId(), status, offset, size);
        list.forEach(a -> {
            a.setStatusText(aftersaleText(a.getStatus()));
            a.setTypeText(a.getType() != null && a.getType() == 1 ? "仅退款" : "退款退货");
        });
        long total = aftersaleMapper.countByShop(merchantId(), status);
        return PageResult.of(list, total, page, size);
    }

    private String aftersaleText(Integer status) {
        return switch (status) {
            case 0 -> "待处理";
            case 1 -> "已同意退款";
            case 2 -> "已拒绝";
            default -> "未知";
        };
    }

    @Transactional
    public void handleAftersale(Long id, Boolean agree, String reply) {
        Aftersale aftersale = aftersaleMapper.selectById(id);
        if (aftersale == null || !aftersale.getShopId().equals(merchantId())) {
            throw new BizException("售后单不存在");
        }
        if (aftersale.getStatus() != 0) {
            throw new BizException("该售后单已处理");
        }
        int rows = aftersaleMapper.handle(id, merchantId(), agree != null && agree ? 1 : 2, reply);
        if (rows == 0) {
            throw new BizException("售后处理失败");
        }
        if (agree != null && agree) {
            orderMapper.updateStatusByShop(aftersale.getOrderId(), merchantId(),
                    OrderService.STATUS_AFTERSALE, OrderService.STATUS_FINISHED);
        } else {
            orderMapper.updateStatusByShop(aftersale.getOrderId(), merchantId(),
                    OrderService.STATUS_AFTERSALE, OrderService.STATUS_WAIT_RECEIVE);
        }
    }

    public PageResult<Review> reviewsPage(int page, int size) {
        int offset = (page - 1) * size;
        List<Review> list = reviewMapper.selectByShop(merchantId(), offset, size);
        long total = reviewMapper.countByShop(merchantId());
        return PageResult.of(list, total, page, size);
    }

    public void replyReview(Long id, String content) {
        if (content == null || content.isBlank()) {
            throw new BizException("回复内容不能为空");
        }
        int rows = reviewMapper.updateReply(id, merchantId(), content);
        if (rows == 0) {
            throw new BizException("评价不存在或无操作权限");
        }
    }

    public Map<String, Object> dashboard() {
        Long shopId = merchantId();
        Map<String, Object> data = new HashMap<>();
        data.put("shop", currentShop());
        data.put("goodsCount", goodsMapper.countByShop(shopId, null, null));
        data.put("onSaleCount", goodsMapper.countByShop(shopId, null, 1));
        data.put("orderToday", orderMapper.countTodayByShop(shopId));
        data.put("saleAmount", orderMapper.sumPaidByShop(shopId));
        data.put("waitPay", orderMapper.countByShop(shopId, OrderService.STATUS_WAIT_PAY));
        data.put("waitShip", orderMapper.countByShop(shopId, OrderService.STATUS_WAIT_SHIP));
        data.put("waitReceive", orderMapper.countByShop(shopId, OrderService.STATUS_WAIT_RECEIVE));
        data.put("aftersalePending", aftersaleMapper.countPendingByShop(shopId));
        data.put("lowStock", skuMapper.countLowStockByShop(shopId, 20));
        return data;
    }

    public Map<String, Object> analytics() {
        Long shopId = merchantId();
        Map<String, Object> data = new HashMap<>();
        data.put("trend", buildTrend(shopId));
        data.put("statusDist", buildStatusDist(shopId));
        data.put("goodsTop", buildGoodsTop(shopId));
        data.put("categoryShare", buildCategoryShare(shopId));
        data.put("radar", buildRadar(shopId));
        return data;
    }

    private Map<String, Object> buildTrend(Long shopId) {
        List<Map<String, Object>> rows = orderMapper.selectTrendByShop(shopId, 7);
        Map<String, Map<String, Object>> byDay = new HashMap<>();
        for (Map<String, Object> r : rows) {
            byDay.put(String.valueOf(r.get("day")), r);
        }
        List<String> days = new ArrayList<>();
        List<Long> orderCounts = new ArrayList<>();
        List<Double> saleAmounts = new ArrayList<>();
        List<Long> itemQtys = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            String day = today.minusDays(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            days.add(day);
            Map<String, Object> row = byDay.get(day);
            long oc = row == null ? 0 : ((Number) row.get("orderCount")).longValue();
            double sa = row == null ? 0 : ((Number) row.get("saleAmount")).doubleValue();
            long qty = row == null ? 0 : ((Number) row.get("itemQty")).longValue();
            orderCounts.add(oc);
            saleAmounts.add(sa);
            itemQtys.add(qty);
        }
        Map<String, Object> trend = new HashMap<>();
        trend.put("days", days);
        trend.put("orderCounts", orderCounts);
        trend.put("saleAmounts", saleAmounts);
        trend.put("itemQtys", itemQtys);
        return trend;
    }

    private List<Map<String, Object>> buildStatusDist(Long shopId) {
        List<Map<String, Object>> rows = orderMapper.selectStatusDistByShop(shopId);
        Map<Integer, String> text = new HashMap<>();
        text.put(0, "待付款");
        text.put(1, "待发货");
        text.put(2, "待收货");
        text.put(3, "已完成");
        text.put(4, "已取消");
        text.put(5, "退款");
        List<Map<String, Object>> dist = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Integer status = ((Number) r.get("status")).intValue();
            long cnt = ((Number) r.get("cnt")).longValue();
            Map<String, Object> item = new HashMap<>();
            item.put("name", text.getOrDefault(status, "状态" + status));
            item.put("value", cnt);
            dist.add(item);
        }
        return dist;
    }

    private List<Map<String, Object>> buildGoodsTop(Long shopId) {
        List<Map<String, Object>> rows = orderItemMapper.selectGoodsSalesTop(shopId, 5);
        List<Map<String, Object>> top = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", r.get("goodsName"));
            item.put("value", ((Number) r.get("qty")).longValue());
            top.add(item);
        }
        return top;
    }

    private List<Map<String, Object>> buildCategoryShare(Long shopId) {
        List<Map<String, Object>> rows = orderItemMapper.selectCategoryShare(shopId);
        List<Map<String, Object>> share = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", r.get("categoryName"));
            item.put("value", ((Number) r.get("amount")).doubleValue());
            share.add(item);
        }
        return share;
    }

    private List<Map<String, Object>> buildRadar(Long shopId) {
        Map<String, Object> stats = orderMapper.selectStatsByShop(shopId);
        if (stats == null) {
            stats = new HashMap<>();
        }
        long paid = num(stats.get("paid"));
        long shipped = num(stats.get("shipped"));
        long finished = num(stats.get("finished"));
        double avgPay = stats.get("avgPay") == null ? 0 : ((Number) stats.get("avgPay")).doubleValue();

        double goodRate = 100;
        double shipRate = paid == 0 ? 100 : (double) shipped / paid * 100;
        double finishRate = paid == 0 ? 100 : (double) finished / paid * 100;
        double avgPriceScore = Math.min(avgPay / 500.0, 1.0) * 100;

        Map<String, Object> rating = reviewMapper.selectRatingStatsByShop(shopId);
        if (rating != null) {
            double avg = rating.get("avgRating") == null ? 0 : ((Number) rating.get("avgRating")).doubleValue();
            long total = rating.get("total") == null ? 0 : ((Number) rating.get("total")).longValue();
            if (total > 0) {
                goodRate = (avg - 1) / 4.0 * 100;
            }
        }

        long totalGoods = goodsMapper.countByShop(shopId, null, null);
        long onSale = goodsMapper.countByShop(shopId, null, 1);
        double onSaleRate = totalGoods == 0 ? 100 : (double) onSale / totalGoods * 100;

        List<Map<String, Object>> radar = new ArrayList<>();
        radar.add(dim("好评率", clamp(goodRate)));
        radar.add(dim("发货率", clamp(shipRate)));
        radar.add(dim("完成率", clamp(finishRate)));
        radar.add(dim("上架率", clamp(onSaleRate)));
        radar.add(dim("客单价指数", clamp(avgPriceScore)));
        return radar;
    }

    private long num(Object o) {
        return o == null ? 0 : ((Number) o).longValue();
    }

    private Map<String, Object> dim(String name, double value) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("value", Math.round(value * 10) / 10.0);
        return item;
    }

    private double clamp(double v) {
        return Math.max(0, Math.min(100, v));
    }
}