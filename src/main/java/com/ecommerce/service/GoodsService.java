package com.ecommerce.service;

import com.ecommerce.common.BizException;
import com.ecommerce.common.PageResult;
import com.ecommerce.entity.*;
import com.ecommerce.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoodsService {

    private final GoodsMapper goodsMapper;
    private final SkuMapper skuMapper;
    private final CategoryMapper categoryMapper;
    private final BannerMapper bannerMapper;
    private final NoticeMapper noticeMapper;
    private final FavoriteMapper favoriteMapper;
    private final BrowseHistoryMapper browseHistoryMapper;

    public List<Category> categoryTree() {
        return categoryMapper.selectAll();
    }

    public List<Category> primaryCategory() {
        return categoryMapper.selectAll().stream()
                .filter(c -> c.getParentId() == 0)
                .collect(Collectors.toList());
    }

    public PageResult<Goods> list(String keyword, Long categoryId, String sort, int page, int size) {
        int offset = (page - 1) * size;
        List<Goods> list = goodsMapper.selectPage(keyword, categoryId, sort, offset, size);
        long total = goodsMapper.countPage(keyword, categoryId);
        return PageResult.of(list, total, page, size);
    }

    public Goods detail(Long id) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BizException(404, "商品不存在");
        }
        goods.setSkuList(skuMapper.selectByGoodsId(id));
        return goods;
    }

    public List<Banner> banners() {
        return bannerMapper.selectShowList();
    }

    public List<Notice> notices() {
        return noticeMapper.selectShowList();
    }

    public boolean isFavorite(Long userId, Long goodsId) {
        return userId != null && favoriteMapper.countByUserGoods(userId, goodsId) > 0;
    }

    public void addFavorite(Long userId, Long goodsId) {
        if (goodsMapper.selectDetail(goodsId) == null) {
            throw new BizException(404, "商品不存在");
        }
        Favorite f = new Favorite();
        f.setUserId(userId);
        f.setGoodsId(goodsId);
        favoriteMapper.insert(f);
    }

    public void removeFavorite(Long userId, Long goodsId) {
        favoriteMapper.delete(userId, goodsId);
    }

    public PageResult<Favorite> favorites(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        List<Favorite> list = favoriteMapper.selectByUser(userId, offset, size);
        long total = favoriteMapper.countByUser(userId);
        return PageResult.of(list, total, page, size);
    }

    public void recordBrowse(Long userId, Long goodsId) {
        if (userId == null || goodsMapper.selectDetail(goodsId) == null) {
            return;
        }
        BrowseHistory h = new BrowseHistory();
        h.setUserId(userId);
        h.setGoodsId(goodsId);
        browseHistoryMapper.insert(h);
    }

    public List<BrowseHistory> browseHistory(Long userId) {
        return browseHistoryMapper.selectByUser(userId, 50);
    }

    public void clearBrowse(Long userId) {
        browseHistoryMapper.deleteByUser(userId);
    }
}