import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createRouter, createMemoryHistory } from 'vue-router'

// authService mocken bevor Router importiert wird
vi.mock('../api/services/authService.js', () => ({
  authService: {
    me: vi.fn(),
    login: vi.fn(),
    logout: vi.fn(),
  }
}))

// Alle View-Importe stubben
vi.mock('../views/LoginView.vue', () => ({ default: { template: '<div>Login</div>' } }))
vi.mock('../views/DashboardView.vue', () => ({ default: { template: '<div>Dashboard</div>' } }))
vi.mock('../views/ArtikelView.vue', () => ({ default: { template: '<div>Artikel</div>' } }))
vi.mock('../views/LieferantenView.vue', () => ({ default: { template: '<div>Lieferanten</div>' } }))
vi.mock('../views/LagerbewegungView.vue', () => ({ default: { template: '<div>Lagerbewegung</div>' } }))
vi.mock('../views/TransaktionshistorieView.vue', () => ({ default: { template: '<div>Transaktionen</div>' } }))
vi.mock('../views/BestellhistorieView.vue', () => ({ default: { template: '<div>Bestellhistorie</div>' } }))

describe('Router Navigation Guard', () => {
  let router
  let authService

  beforeEach(async () => {
    vi.resetModules()
    vi.clearAllMocks()

    const authMod = await import('../api/services/authService.js')
    authService = authMod.authService

    const routerMod = await import('../router/index.js')
    router = routerMod.default
  })

  it('leitet bei fehlendem Session-Cookie auf /login um', async () => {
    authService.me.mockRejectedValue({ response: { status: 401 } })

    await router.push('/dashboard')
    await router.isReady()

    expect(router.currentRoute.value.path).toBe('/login')
  })

  it('erlaubt Zugriff auf /dashboard bei gültiger Session', async () => {
    authService.me.mockResolvedValue({ data: { username: 'operator' } })

    await router.push('/dashboard')
    await router.isReady()

    expect(router.currentRoute.value.path).toBe('/dashboard')
  })

  it('erlaubt Zugriff auf /login ohne Authentifizierung', async () => {
    authService.me.mockRejectedValue({ response: { status: 401 } })

    await router.push('/login')
    await router.isReady()

    expect(router.currentRoute.value.path).toBe('/login')
  })

  it('leitet / auf /dashboard um', async () => {
    authService.me.mockResolvedValue({ data: { username: 'operator' } })

    await router.push('/')
    await router.isReady()

    expect(router.currentRoute.value.path).toBe('/dashboard')
  })

  it('alle geschützten Routen haben meta.requiresAuth', async () => {
    const routerMod = await import('../router/index.js')
    const routes = routerMod.default.getRoutes()
    const geschuetzt = routes.filter(r => r.meta?.requiresAuth)
    expect(geschuetzt.length).toBeGreaterThanOrEqual(6)
  })
})
