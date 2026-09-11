import { describe, it, expect, vi, beforeEach } from 'vitest'

const mockPost = vi.fn()
const mockGet = vi.fn()

vi.mock('../api/axios.js', () => ({
  default: {
    post: mockPost,
    get: mockGet,
    interceptors: { response: { use: vi.fn(), handlers: [] } },
    defaults: { baseURL: '/api/v1', withCredentials: true }
  }
}))

describe('authService', () => {
  let authService

  beforeEach(async () => {
    vi.resetModules()
    vi.clearAllMocks()
    const mod = await import('../api/services/authService.js')
    authService = mod.authService
  })

  it('login() sendet POST an /auth/login mit form-encoded-Daten', async () => {
    mockPost.mockResolvedValue({ status: 200 })

    await authService.login('operator', 'geheim')

    // Spring Security formLogin erwartet application/x-www-form-urlencoded, kein HTTP-Basic-Auth
    expect(mockPost).toHaveBeenCalledWith(
      '/auth/login',
      expect.any(URLSearchParams),
      { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
    )
    const params = mockPost.mock.calls[0][1]
    expect(params.get('username')).toBe('operator')
    expect(params.get('password')).toBe('geheim')
  })

  it('logout() sendet POST an /auth/logout', async () => {
    mockPost.mockResolvedValue({ status: 200 })

    await authService.logout()

    expect(mockPost).toHaveBeenCalledWith('/auth/logout')
  })

  it('me() sendet GET an /auth/me', async () => {
    mockGet.mockResolvedValue({ status: 200, data: { username: 'operator' } })

    await authService.me()

    expect(mockGet).toHaveBeenCalledWith('/auth/me')
  })

  it('login() wirft bei fehlgeschlagener Auth weiter', async () => {
    mockPost.mockRejectedValue(new Error('401 Unauthorized'))

    await expect(authService.login('operator', 'falsch'))
      .rejects.toThrow('401 Unauthorized')
  })
})
