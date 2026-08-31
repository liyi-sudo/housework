import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Home', component: () => import('../views/Home.vue') },
  { path: '/category', name: 'Category', component: () => import('../views/Category.vue') },
  { path: '/search', name: 'Search', component: () => import('../views/Search.vue') },
  { path: '/goods/:id', name: 'GoodsDetail', component: () => import('../views/GoodsDetail.vue') },
  { path: '/cart', name: 'Cart', component: () => import('../views/Cart.vue'), meta: { auth: true } },
  { path: '/checkout', name: 'Checkout', component: () => import('../views/Checkout.vue'), meta: { auth: true } },
  { path: '/orders', name: 'Orders', component: () => import('../views/Orders.vue'), meta: { auth: true } },
  { path: '/order/:id', name: 'OrderDetail', component: () => import('../views/OrderDetail.vue'), meta: { auth: true } },
  { path: '/pay/:id', name: 'Pay', component: () => import('../views/Pay.vue'), meta: { auth: true } },
  { path: '/shop/:id', name: 'ShopPage', component: () => import('../views/ShopPage.vue') },
  { path: '/notifications', name: 'Notifications', component: () => import('../views/Notifications.vue'), meta: { auth: true } },
  { path: '/aftersales', name: 'AfterSale', component: () => import('../views/AfterSale.vue'), meta: { auth: true } },
  { path: '/reviews', name: 'MyReviews', component: () => import('../views/MyReviews.vue'), meta: { auth: true } },
  { path: '/favorites', name: 'Favorites', component: () => import('../views/Favorites.vue'), meta: { auth: true } },
  { path: '/history', name: 'BrowseHistory', component: () => import('../views/BrowseHistory.vue'), meta: { auth: true } },
  { path: '/coupons', name: 'Coupons', component: () => import('../views/Coupons.vue'), meta: { auth: true } },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },
  { path: '/user', name: 'UserCenter', component: () => import('../views/UserCenter.vue'), meta: { auth: true } },
  { path: '/address', name: 'Address', component: () => import('../views/Address.vue'), meta: { auth: true } },
  { path: '/user/password', name: 'ChangePassword', component: () => import('../views/ChangePassword.vue'), meta: { auth: true } },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('../views/NotFound.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(to => {
  if (to.meta.auth && !localStorage.getItem('token')) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
})

export default router