import request from './request'

// 认证
export const register = data => request.post('/auth/register', data)
export const login = data => request.post('/auth/login', data)
export const sendSmsCode = phone => request.post('/auth/sms-code', { phone })
export const logout = () => request.post('/auth/logout')

// 用户
export const getUserInfo = () => request.get('/user/info')
export const updateUserInfo = data => request.put('/user/info', data)
export const changePassword = data => request.put('/user/password', data)

// 地址
export const getAddresses = () => request.get('/address')
export const addAddress = data => request.post('/address', data)
export const updateAddress = data => request.put('/address', data)
export const deleteAddress = id => request.delete(`/address/${id}`)
export const setDefaultAddress = id => request.put(`/address/${id}/default`)

// 首页
export const getBanners = () => request.get('/banner')
export const getNotices = () => request.get('/notice')
export const getCategories = () => request.get('/category/tree')

// 商品
export const getGoodsList = params => request.get('/goods', { params })
export const getGoodsDetail = id => request.get(`/goods/${id}`)
export const getGoodsReviews = (id, params) => request.get(`/goods/${id}/reviews`, { params })
export const recordBrowse = id => request.post(`/goods/${id}/browse`)
export const getShop = id => request.get(`/shop/${id}`)

// 收藏
export const getFavorites = params => request.get('/favorites', { params })
export const addFavorite = id => request.post(`/favorites/${id}`)
export const removeFavorite = id => request.delete(`/favorites/${id}`)
export const favoriteStatus = id => request.get(`/favorites/${id}/status`)

// 购物车
export const getCart = () => request.get('/cart')
export const addToCart = data => request.post('/cart', data)
export const updateCartQty = (id, quantity) => request.put(`/cart/${id}/quantity`, { quantity })
export const checkCart = (checked, ids) => request.put('/cart/check', { checked, ids })
export const deleteCartItem = id => request.delete(`/cart/${id}`)

// 优惠券
export const getClaimableCoupons = () => request.get('/coupon/claimable')
export const claimCoupon = id => request.post(`/coupon/${id}/claim`)
export const getMyCoupons = params => request.get('/coupon/mine', { params })

// 订单
export const submitOrder = data => request.post('/order/submit', data)
export const getOrders = params => request.get('/order', { params })
export const getOrderDetail = id => request.get(`/order/${id}`)
export const payOrder = (id, method = 'BALANCE') => request.post(`/order/${id}/pay`, { method })
export const cancelOrder = id => request.post(`/order/${id}/cancel`)
export const confirmOrder = id => request.post(`/order/${id}/confirm`)
export const getLogistics = id => request.get(`/order/${id}/logistics`)

// 评价
export const publishReview = data => request.post('/review', data)
export const myReviews = () => request.get('/review/mine')
export const getReviewTarget = goodsId => request.get(`/review/goods/${goodsId}/target`)

// 售后
export const applyAfterSale = data => request.post('/aftersale', data)
export const getAfterSales = () => request.get('/aftersale')

// 浏览记录
export const getBrowseHistory = () => request.get('/user/browse-history')
export const clearBrowseHistory = () => request.delete('/user/browse-history')

// 消息通知
export const getUserMessages = () => request.get('/message/user')