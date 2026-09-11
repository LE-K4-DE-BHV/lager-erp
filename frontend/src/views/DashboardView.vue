<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { dashboardService } from '@/api/services/dashboardService'
import KpiSummary from '@/components/KpiSummary.vue'
import BestellvorschlagTabelle from '@/components/BestellvorschlagTabelle.vue'

const router = useRouter()
const toast = useToast()

const kpis = ref({ offeneVorschlaege: 0, kritischeVorschlaege: 0, letzterReorderLauf: null })
const vorschlaege = ref([])
const kpisLoading = ref(false)
const vorschlaegeLoading = ref(false)
const reorderLoading = ref(false)

const quickLinks = [
  { label: 'Lagerbewegung', icon: 'pi pi-arrow-right-arrow-left', route: '/lagerbewegung' },
  { label: 'Artikel',        icon: 'pi pi-box',                   route: '/artikel' },
  { label: 'Lieferanten',   icon: 'pi pi-truck',                  route: '/lieferanten' },
  { label: 'Transaktionen', icon: 'pi pi-history',                route: '/transaktionen' },
  { label: 'Bestellhistorie', icon: 'pi pi-file-check',           route: '/bestellungen' },
]

const loadKpis = async () => {
  kpisLoading.value = true
  try {
    const { data } = await dashboardService.getKpis()
    kpis.value = data
  } catch {
    toast.add({ severity: 'error', summary: 'Fehler', detail: 'KPIs konnten nicht geladen werden.', life: 4000 })
  } finally {
    kpisLoading.value = false
  }
}

const loadVorschlaege = async () => {
  vorschlaegeLoading.value = true
  try {
    const { data } = await dashboardService.getVorschlaege()
    vorschlaege.value = data
  } catch {
    toast.add({ severity: 'error', summary: 'Fehler', detail: 'Bestellvorschläge konnten nicht geladen werden.', life: 4000 })
  } finally {
    vorschlaegeLoading.value = false
  }
}

const handleReorderTrigger = async () => {
  reorderLoading.value = true
  try {
    const { data } = await dashboardService.triggerReorder()
    toast.add({
      severity: 'success',
      summary: 'Analyse abgeschlossen',
      detail: `${data.neueVorschlaege} neue Vorschläge erstellt.`,
      life: 5000
    })
    await Promise.all([loadKpis(), loadVorschlaege()])
  } catch {
    toast.add({ severity: 'error', summary: 'Fehler', detail: 'Reorder-Analyse fehlgeschlagen.', life: 4000 })
  } finally {
    reorderLoading.value = false
  }
}

const handleRefresh = () => {
  loadKpis()
  loadVorschlaege()
}

onMounted(() => {
  loadKpis()
  loadVorschlaege()
})
</script>

<template>
  <div class="dashboard">
    <div class="dashboard__section">
      <div class="dashboard__section-header">
        <h2 class="dashboard__section-title">Übersicht</h2>
        <Button
          label="Analyse jetzt ausführen"
          icon="pi pi-refresh"
          severity="secondary"
          :loading="reorderLoading"
          data-testid="btn-reorder-trigger"
          @click="handleReorderTrigger"
        />
      </div>
      <KpiSummary :kpis="kpis" :loading="kpisLoading" />
    </div>

    <div class="dashboard__section">
      <h2 class="dashboard__section-title">Schnellzugriff</h2>
      <div class="quick-links">
        <Card
          v-for="link in quickLinks"
          :key="link.route"
          class="quick-link-card"
          @click="router.push(link.route)"
        >
          <template #content>
            <span :class="['pi', link.icon, 'quick-link-card__icon']" />
            <span class="quick-link-card__label">{{ link.label }}</span>
          </template>
        </Card>
      </div>
    </div>

    <div class="dashboard__section">
      <h2 class="dashboard__section-title">Bestellvorschläge</h2>
      <div class="dashboard__legend">
        <span class="legend-dot legend-dot--rot" /> Kritisch (Bestand ≤ Sicherheitsbestand)
        <span class="legend-dot legend-dot--gelb" /> Offen (Bestand > Sicherheitsbestand)
        <span class="legend-dot legend-dot--grau" /> In Lieferung (bestellt)
      </div>
      <BestellvorschlagTabelle
        :vorschlaege="vorschlaege"
        :loading="vorschlaegeLoading"
        @refresh="handleRefresh"
      />
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 2rem;
  max-width: 1400px;
  margin: 0 auto;
}

.dashboard__section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.dashboard__section-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--p-text-color, #1e293b);
  margin: 0 0 1rem;
}

.dashboard__section-header .dashboard__section-title {
  margin: 0;
}

.quick-links {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.quick-link-card {
  flex: 1;
  min-width: 140px;
  cursor: pointer;
  transition: box-shadow 0.15s, transform 0.1s;
}

.quick-link-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

:deep(.quick-link-card .p-card-body) {
  padding: 1.25rem 1rem;
}

:deep(.quick-link-card .p-card-content) {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 0;
}

.quick-link-card__icon {
  font-size: 1.75rem;
  color: var(--p-primary-500, #2563eb);
}

.quick-link-card__label {
  font-size: 0.875rem;
  font-weight: 500;
  text-align: center;
  color: var(--p-text-color, #1e293b);
}

.dashboard__legend {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  color: var(--p-text-muted-color, #64748b);
  margin-bottom: 0.75rem;
  flex-wrap: wrap;
}

.legend-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 3px;
  margin-left: 0.75rem;
}

.legend-dot:first-child { margin-left: 0; }
.legend-dot--rot  { background-color: #fef2f2; border: 1px solid #fca5a5; }
.legend-dot--gelb { background-color: #fefce8; border: 1px solid #fde047; }
.legend-dot--grau { background-color: #f1f5f9; border: 1px solid #cbd5e1; }
</style>
