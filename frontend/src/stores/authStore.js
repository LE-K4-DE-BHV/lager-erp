import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authService } from '@/api/services/authService'
import router from '@/router'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const isAuthenticated = computed(() => user.value !== null)

  async function fetchUser() {
    const response = await authService.me()
    user.value = response.data
  }

  async function login(username, password) {
    await authService.login(username, password)
    await fetchUser()
  }

  async function logout() {
    await authService.logout()
    user.value = null
    router.push('/login')
  }

  return { user, isAuthenticated, fetchUser, login, logout }
})
