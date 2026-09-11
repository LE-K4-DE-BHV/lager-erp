<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const loading = ref(false)
const errorMessage = ref('')

const handleSubmit = async () => {
  errorMessage.value = ''
  loading.value = true
  try {
    await authStore.login(username.value, password.value)
    router.push('/dashboard')
  } catch {
    errorMessage.value = 'Anmeldung fehlgeschlagen. Bitte Benutzername und Passwort prüfen.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-card__header">
        <span class="pi pi-box login-card__icon" />
        <h1 class="login-card__title">Lager-Management</h1>
        <p class="login-card__subtitle">Melde dich an, um fortzufahren</p>
      </div>

      <form class="login-card__form" @submit.prevent="handleSubmit">
        <Message
          v-if="errorMessage"
          severity="error"
          :closable="false"
          data-testid="error-msg"
        >
          {{ errorMessage }}
        </Message>

        <div class="p-field">
          <label for="username">Benutzername</label>
          <InputText
            id="username"
            v-model="username"
            placeholder="operator"
            autocomplete="username"
            :disabled="loading"
            data-testid="input-username"
          />
        </div>

        <div class="p-field">
          <label for="password">Passwort</label>
          <Password
            id="password"
            v-model="password"
            placeholder="••••••••"
            :feedback="false"
            toggle-mask
            autocomplete="current-password"
            :disabled="loading"
            data-testid="input-password"
          />
        </div>

        <Button
          type="submit"
          label="Anmelden"
          icon="pi pi-sign-in"
          :loading="loading"
          :disabled="!username || !password"
          class="login-card__submit"
          data-testid="btn-submit"
        />
      </form>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--p-surface-50, #f8fafc);
}

.login-card {
  width: 100%;
  max-width: 420px;
  padding: 2.5rem;
  background: var(--p-surface-0, #ffffff);
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}

.login-card__header {
  text-align: center;
  margin-bottom: 2rem;
}

.login-card__icon {
  font-size: 2.5rem;
  color: var(--p-primary-500, #2563eb);
}

.login-card__title {
  margin: 0.75rem 0 0.25rem;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--p-text-color, #1e293b);
}

.login-card__subtitle {
  margin: 0;
  color: var(--p-text-muted-color, #64748b);
  font-size: 0.9rem;
}

.login-card__form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.p-field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.p-field label {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--p-text-color, #1e293b);
}

.p-field :deep(input),
.p-field :deep(.p-password) {
  width: 100%;
}

.login-card__submit {
  width: 100%;
  justify-content: center;
}
</style>
