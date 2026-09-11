<script setup>
defineProps({
  kpis: {
    type: Object,
    default: () => ({ offeneVorschlaege: 0, kritischeVorschlaege: 0, letzterReorderLauf: null })
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const formatDate = (isoString) => {
  if (!isoString) return '—'
  return new Date(isoString).toLocaleString('de-DE', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}
</script>

<template>
  <div class="kpi-summary">
    <div class="kpi-card">
      <span class="pi pi-list-check kpi-card__icon kpi-card__icon--blue" />
      <div class="kpi-card__body">
        <span class="kpi-card__label">Offene Vorschläge</span>
        <Skeleton v-if="loading" width="3rem" height="2rem" />
        <span v-else class="kpi-card__value" data-testid="kpi-offen">
          {{ kpis.offeneVorschlaege }}
        </span>
      </div>
    </div>

    <div class="kpi-card">
      <span class="pi pi-exclamation-triangle kpi-card__icon kpi-card__icon--red" />
      <div class="kpi-card__body">
        <span class="kpi-card__label">Kritisch</span>
        <Skeleton v-if="loading" width="3rem" height="2rem" />
        <span v-else class="kpi-card__value kpi-card__value--red" data-testid="kpi-kritisch">
          {{ kpis.kritischeVorschlaege }}
        </span>
      </div>
    </div>

    <div class="kpi-card">
      <span class="pi pi-clock kpi-card__icon kpi-card__icon--gray" />
      <div class="kpi-card__body">
        <span class="kpi-card__label">Letzter Reorder-Lauf</span>
        <Skeleton v-if="loading" width="8rem" height="1.25rem" />
        <span v-else class="kpi-card__value kpi-card__value--small" data-testid="kpi-letzter-lauf">
          {{ formatDate(kpis.letzterReorderLauf) }}
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.kpi-summary {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.kpi-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex: 1;
  min-width: 180px;
  padding: 1.25rem 1.5rem;
  background: var(--p-surface-0, #ffffff);
  border: 1px solid var(--p-surface-200, #e2e8f0);
  border-radius: 10px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.kpi-card__icon {
  font-size: 1.75rem;
}

.kpi-card__icon--blue  { color: var(--p-primary-500, #2563eb); }
.kpi-card__icon--red   { color: #ef4444; }
.kpi-card__icon--gray  { color: var(--p-text-muted-color, #94a3b8); }

.kpi-card__body {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.kpi-card__label {
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--p-text-muted-color, #64748b);
}

.kpi-card__value {
  font-size: 1.75rem;
  font-weight: 700;
  line-height: 1;
  color: var(--p-text-color, #1e293b);
}

.kpi-card__value--red   { color: #ef4444; }
.kpi-card__value--small { font-size: 0.95rem; font-weight: 500; }
</style>
