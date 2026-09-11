import { createRouter, createWebHistory } from 'vue-router'
import { authService } from '@/api/services/authService'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue')
  },
  {
    path: '/dashboard',
    name: 'dashboard',
    component: () => import('@/views/DashboardView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/artikel',
    name: 'artikel',
    component: () => import('@/views/ArtikelView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/lieferanten',
    name: 'lieferanten',
    component: () => import('@/views/LieferantenView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/lagerbewegung',
    name: 'lagerbewegung',
    component: () => import('@/views/LagerbewegungView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/transaktionen',
    name: 'transaktionen',
    component: () => import('@/views/TransaktionshistorieView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/bestellungen',
    name: 'bestellungen',
    component: () => import('@/views/BestellhistorieView.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  if (to.meta.requiresAuth) {
    try {
      await authService.me()
    } catch {
      return '/login'
    }
  }
})

export default router
