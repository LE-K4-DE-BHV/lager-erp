import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import PrimeVue from 'primevue/config'
import ToastService from 'primevue/toastservice'
import LoginView from '../views/LoginView.vue'

const mockRouterPush = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockRouterPush }),
  useRoute: () => ({ path: '/login' }),
  RouterLink: { template: '<a><slot /></a>' },
  RouterView: { template: '<div />' },
}))

vi.mock('../stores/authStore.js', () => ({
  useAuthStore: vi.fn()
}))

// PrimeVue-Komponenten als einfache native HTML-Elemente stubben
// v-model über modelValue-Prop + update:modelValue-Event korrekt weitergeben
const primeVueStubs = {
  InputText: {
    props: ['modelValue'],
    emits: ['update:modelValue'],
    inheritAttrs: false,
    template: '<input :value="modelValue" v-bind="$attrs" @input="$emit(\'update:modelValue\', $event.target.value)" />'
  },
  Password: {
    props: ['modelValue'],
    emits: ['update:modelValue'],
    inheritAttrs: false,
    template: '<input type="password" :value="modelValue" v-bind="$attrs" @input="$emit(\'update:modelValue\', $event.target.value)" />'
  },
  Button: {
    template: '<button type="submit" v-bind="$attrs" :disabled="$attrs.disabled !== undefined"><slot /></button>',
    inheritAttrs: true
  },
  Message: { template: '<div data-testid="error-msg"><slot /></div>' },
}

describe('LoginView', () => {
  let mockLogin

  const mountLoginView = () =>
    mount(LoginView, {
      global: {
        plugins: [
          PrimeVue,
          ToastService,
          createTestingPinia({ createSpy: vi.fn })
        ],
        stubs: primeVueStubs
      }
    })

  beforeEach(async () => {
    vi.clearAllMocks()
    mockLogin = vi.fn()
    const { useAuthStore } = await import('../stores/authStore.js')
    useAuthStore.mockReturnValue({ login: mockLogin, isAuthenticated: false })
  })

  it('rendert das Login-Formular mit Benutzername, Passwort und Submit-Button', () => {
    const wrapper = mountLoginView()
    expect(wrapper.find('[data-testid="input-username"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="input-password"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="btn-submit"]').exists()).toBe(true)
  })

  it('leitet nach erfolgreichem Login auf /dashboard weiter', async () => {
    mockLogin.mockResolvedValue(undefined)
    const wrapper = mountLoginView()

    await wrapper.find('[data-testid="input-username"]').setValue('operator')
    await wrapper.find('[data-testid="input-password"]').setValue('geheim')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(mockLogin).toHaveBeenCalledWith('operator', 'geheim')
    expect(mockRouterPush).toHaveBeenCalledWith('/dashboard')
  })

  it('zeigt Fehlermeldung bei falschem Login, leitet nicht weiter', async () => {
    mockLogin.mockRejectedValue({ response: { status: 401 } })
    const wrapper = mountLoginView()

    await wrapper.find('[data-testid="input-username"]').setValue('operator')
    await wrapper.find('[data-testid="input-password"]').setValue('falsch')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.find('[data-testid="error-msg"]').exists()).toBe(true)
    expect(mockRouterPush).not.toHaveBeenCalled()
  })

  it('deaktiviert den Submit-Button bei leerem Formular', () => {
    const wrapper = mountLoginView()
    const btn = wrapper.find('[data-testid="btn-submit"]')
    expect(btn.attributes('disabled')).toBeDefined()
  })
})
