import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import TraceView from '../views/TraceView.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/trace/:id?',
    name: 'trace',
    component: TraceView,
    props: true
  },
  {
    path: '/products',
    name: 'products',
    component: () => import(/* webpackChunkName: "products" */ '../views/ProductsView.vue')
  },
  {
    path: '/product/:id',
    name: 'product-detail',
    component: () => import(/* webpackChunkName: "product-detail" */ '../views/ProductDetailView.vue'),
    props: true
  },
  {
    path: '/ai-identification',
    name: 'ai-identification',
    component: () => import(/* webpackChunkName: "ai-identification" */ '../views/AiIdentificationView.vue')
  },
  {
    path: '/about',
    name: 'about',
    component: () => import(/* webpackChunkName: "about" */ '../views/AboutView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router 