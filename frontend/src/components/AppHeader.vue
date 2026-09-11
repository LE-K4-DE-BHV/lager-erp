<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

const handleLogout = async () => {
  await authStore.logout()
}
</script>

<template>
  <header class="app-header">
    <div class="app-header__brand">
      <span class="pi pi-box" style="font-size: 1.4rem;" />
      <span class="app-header__title">Lager-Management</span>
    </div>
    <nav class="app-header__nav">
      <RouterLink to="/dashboard">Dashboard</RouterLink>
      <RouterLink to="/artikel">Artikel</RouterLink>
      <RouterLink to="/lieferanten">Lieferanten</RouterLink>
      <RouterLink to="/lagerbewegung">Lagerbewegung</RouterLink>
      <RouterLink to="/transaktionen">Transaktionen</RouterLink>
      <RouterLink to="/bestellungen">Bestellungen</RouterLink>
    </nav>
    <Button
      label="Abmelden"
      icon="pi pi-sign-out"
      severity="secondary"
      size="small"
      aria-label="Abmelden"
      @click="handleLogout"
    />
  </header>
</template>

<style scoped>
.app-header {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 0.75rem 1.5rem;
  background-color: var(--p-surface-0, #ffffff);
  border-bottom: 1px solid var(--p-surface-200, #e2e8f0);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.app-header__brand {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--p-primary-500, #2563eb);
}

.app-header__title {
  font-size: 1.1rem;
  font-weight: 600;
  white-space: nowrap;
}

.app-header__nav {
  display: flex;
  gap: 0.25rem;
  flex: 1;
}

.app-header__nav a {
  padding: 0.4rem 0.75rem;
  border-radius: 6px;
  text-decoration: none;
  font-size: 0.875rem;
  color: var(--p-text-muted-color, #64748b);
  transition: background 0.15s, color 0.15s;
}

.app-header__nav a:hover,
.app-header__nav a.router-link-active {
  background-color: var(--p-primary-50, #eff6ff);
  color: var(--p-primary-600, #1d4ed8);
  font-weight: 500;
}
</style>
