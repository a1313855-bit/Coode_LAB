import { createRouter, createWebHistory } from 'vue-router'

// 商城（會員端）
import StoreLayout from '../layouts/StoreLayout.vue'
import StoreHome from '../views/store/StoreHome.vue'
import ProductDetail from '../views/store/ProductDetail.vue'
import CartView from '../views/store/CartView.vue'
import OrdersView from '../views/store/OrdersView.vue'
import ReturnsView from '../views/store/ReturnsView.vue'
import OutfitsView from '../views/store/OutfitsView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'

// 廠商後台
import VendorLayout from '../layouts/VendorLayout.vue'
import VendorDashboard from '../views/vendor/VendorDashboard.vue'
import VendorProducts from '../views/vendor/VendorProducts.vue'
import VendorLowStock from '../views/vendor/VendorLowStock.vue'
import VendorOrders from '../views/vendor/VendorOrders.vue'
import VendorReturns from '../views/vendor/VendorReturns.vue'

// 管理員後台
import AdminLayout from '../layouts/AdminLayout.vue'
import AdminUsers from '../views/admin/AdminUsers.vue'
import AdminVendors from '../views/admin/AdminVendors.vue'
import AdminAdmins from '../views/admin/AdminAdmins.vue'
import AdminProducts from '../views/admin/AdminProducts.vue'
import AdminOrders from '../views/admin/AdminOrders.vue'

// 登入狀態（reload 後仍保持）
import { useAuth, homeFor } from '../composables/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // ═══ 商城（會員端）═══ 同時是「未登入時的入口頁」
    {
      path: '/',
      component: StoreLayout,
      redirect: '/store',
      children: [
        { path: 'store', name: 'store', component: StoreHome },
        { path: 'store/product/:id', name: 'product-detail', component: ProductDetail },
        { path: 'cart', name: 'cart', component: CartView, meta: { auth: 'user' } },
        { path: 'orders', name: 'orders', component: OrdersView, meta: { auth: 'user' } },
        { path: 'returns', name: 'returns', component: ReturnsView, meta: { auth: 'user' } },
        { path: 'outfits', name: 'outfits', component: OutfitsView, meta: { auth: 'user' } },
        { path: 'login', name: 'login', component: LoginView },
        { path: 'register', name: 'register', component: RegisterView },
      ],
    },
    // ═══ 廠商後台（需 vendor 登入）═══
    {
      path: '/vendor',
      component: VendorLayout,
      redirect: '/vendor/dashboard',
      meta: { auth: 'vendor' },
      children: [
        { path: 'dashboard', name: 'vendor-dashboard', component: VendorDashboard },
        { path: 'products', name: 'vendor-products', component: VendorProducts },
        { path: 'low-stock', name: 'vendor-low-stock', component: VendorLowStock },
        { path: 'orders', name: 'vendor-orders', component: VendorOrders },
        { path: 'returns', name: 'vendor-returns', component: VendorReturns },
      ],
    },
    // ═══ 管理員後台（需 admin 登入）═══
    {
      path: '/admin',
      component: AdminLayout,
      redirect: '/admin/users',
      meta: { auth: 'admin' },
      children: [
        { path: 'users', name: 'admin-users', component: AdminUsers },
        { path: 'vendors', name: 'admin-vendors', component: AdminVendors },
        { path: 'admins', name: 'admin-admins', component: AdminAdmins },
        { path: 'products', name: 'admin-products', component: AdminProducts },
        { path: 'orders', name: 'admin-orders', component: AdminOrders },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/store' },
  ],
})

// 路由守衛：依登入身份擋下，並在登入後自動導向對應區域
router.beforeEach((to) => {
  const auth = useAuth().value
  const need = to.meta.auth

  // 已登入者再進登入/註冊頁 → 直接前往該身份的首頁
  if (auth && (to.name === 'login' || to.name === 'register')) {
    return homeFor(auth.role)
  }

  // 需要特定身份的頁面 → 未登入或身份不符，導去登入頁
  if (need && (!auth || auth.role !== need)) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  return true
})

export default router