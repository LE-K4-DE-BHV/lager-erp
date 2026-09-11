import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { createRouter, createWebHistory } from 'vue-router'

vi.mock('../router', () => ({
  default: {
    push: vi.fn()
  }
}))

describe('axios.js – Konfiguration', () => {
  let apiClient

  beforeEach(async () => {
    vi.resetModules()
    const mod = await import('../api/axios.js')
    apiClient = mod.default
  })

  it('nutzt baseURL /api/v1', () => {
    expect(apiClient.defaults.baseURL).toBe('/api/v1')
  })

  it('sendet Credentials (withCredentials: true)', () => {
    expect(apiClient.defaults.withCredentials).toBe(true)
  })

  it('hat einen Response-Interceptor registriert', () => {
    const interceptors = apiClient.interceptors.response.handlers
    expect(interceptors.length).toBeGreaterThan(0)
  })
})

describe('axios.js – 401-Interceptor', () => {
  it('leitet bei 401 auf /login um', async () => {
    const routerMock = await import('../router')
    const mod = await import('../api/axios.js')
    const client = mod.default

    const rejectedHandler = client.interceptors.response.handlers.find(h => h !== null)

    const error = { response: { status: 401 } }
    await rejectedHandler.rejected(error).catch(() => {})

    expect(routerMock.default.push).toHaveBeenCalledWith('/login')
  })

  it('leitet bei 500 NICHT auf /login um', async () => {
    const routerMock = await import('../router')
    routerMock.default.push.mockClear()
    const mod = await import('../api/axios.js')
    const client = mod.default

    const rejectedHandler = client.interceptors.response.handlers.find(h => h !== null)

    const error = { response: { status: 500 } }
    await rejectedHandler.rejected(error).catch(() => {})

    expect(routerMock.default.push).not.toHaveBeenCalled()
  })
})
