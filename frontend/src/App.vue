<script setup>
import { useRoute } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'

const route = useRoute()
const isLoginPage = () => route.name === 'login'
</script>

<template>
  <AppHeader v-if="!isLoginPage()" />
  <main>
    <RouterView />
  </main>
  <Toast position="top-right" />
</template>

<style>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: var(--p-font-family), system-ui, sans-serif;
  background-color: var(--p-surface-50, #f8fafc);
  color: var(--p-text-color, #1e293b);
}

main {
  padding: 1.5rem;
}
</style>
