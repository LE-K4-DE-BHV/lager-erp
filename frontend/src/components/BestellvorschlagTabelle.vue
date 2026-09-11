<script setup>
import { ref } from 'vue'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import { dashboardService } from '@/api/services/dashboardService'
import BestellModal from '@/components/BestellModal.vue'

const props = defineProps({
  vorschlaege: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false }
})

const emit = defineEmits(['refresh'])

const toast = useToast()
const confirm = useConfirm()

const modalVisible = ref(false)
const selectedVorschlag = ref(null)
const actionLoading = ref(null)

const rowClass = (row) => {
  if (row.status === 'BESTELLT') return 'row--bestellt'
  if (row.istKritisch) return 'row--kritisch'
  return 'row--normal'
}

const statusLabel = (status) => {
  const map = { VORSCHLAG: 'Offen', BESTELLT: 'In Lieferung' }
  return map[status] ?? status
}

const handleSchnellbestellen = async (vorschlag) => {
  confirm.require({
    message: `Schnellbestellung für "${vorschlag.artikelBezeichnung}" aufgeben?`,
    header: 'Schnellbestellen',
    icon: 'pi pi-shopping-cart',
    acceptLabel: 'Jetzt bestellen',
    rejectLabel: 'Abbrechen',
    accept: async () => {
      actionLoading.value = vorschlag.id
      try {
        await dashboardService.schnellbestellen(vorschlag.id)
        toast.add({ severity: 'success', summary: 'Bestellung aufgegeben', life: 3000 })
        emit('refresh')
      } catch {
        toast.add({ severity: 'error', summary: 'Fehler', detail: 'Bestellung fehlgeschlagen.', life: 4000 })
      } finally {
        actionLoading.value = null
      }
    }
  })
}

const handleBearbeiten = (vorschlag) => {
  selectedVorschlag.value = vorschlag
  modalVisible.value = true
}

const handleIgnorieren = (vorschlag) => {
  confirm.require({
    message: `Vorschlag für "${vorschlag.artikelBezeichnung}" ignorieren?`,
    header: 'Vorschlag ignorieren',
    icon: 'pi pi-ban',
    acceptLabel: 'Ignorieren',
    rejectLabel: 'Abbrechen',
    acceptClass: 'p-button-danger',
    accept: async () => {
      actionLoading.value = vorschlag.id
      try {
        await dashboardService.ignorieren(vorschlag.id)
        toast.add({ severity: 'info', summary: 'Vorschlag ignoriert', life: 3000 })
        emit('refresh')
      } catch {
        toast.add({ severity: 'error', summary: 'Fehler', detail: 'Aktion fehlgeschlagen.', life: 4000 })
      } finally {
        actionLoading.value = null
      }
    }
  })
}

const handleBestellt = () => {
  emit('refresh')
}
</script>

<template>
  <div>
    <ConfirmDialog />

    <DataTable
      :value="vorschlaege"
      :loading="loading"
      :row-class="rowClass"
      striped-rows
      data-testid="vorschlag-tabelle"
      empty-message="Keine offenen Bestellvorschläge."
    >
      <Column field="artikelnummer" header="Art.-Nr." style="width: 120px" />
      <Column field="artikelBezeichnung" header="Bezeichnung" />
      <Column field="lieferantName" header="Lieferant" />
      <Column header="Bestand" style="width: 100px; text-align: right">
        <template #body="{ data }">
          <span :class="{ 'text-danger': data.istKritisch }">{{ data.aktuellerBestand }}</span>
        </template>
      </Column>
      <Column field="bestellpunkt" header="Bestellpunkt" style="width: 110px; text-align: right" />
      <Column field="vorgeschlageneMenge" header="Vorgesch. Menge" style="width: 130px; text-align: right" />
      <Column header="Status" style="width: 120px">
        <template #body="{ data }">
          <Tag
            :value="statusLabel(data.status)"
            :severity="data.status === 'BESTELLT' ? 'secondary' : data.istKritisch ? 'danger' : 'warn'"
          />
        </template>
      </Column>
      <Column header="Aktionen" style="width: 220px">
        <template #body="{ data }">
          <div class="aktionen" v-if="data.status === 'VORSCHLAG'">
            <Button
              icon="pi pi-shopping-cart"
              label="Schnell"
              size="small"
              severity="success"
              :loading="actionLoading === data.id"
              aria-label="Schnellbestellen"
              data-testid="btn-schnellbestellen"
              @click="handleSchnellbestellen(data)"
            />
            <Button
              icon="pi pi-pencil"
              label="Bearbeiten"
              size="small"
              severity="info"
              :disabled="actionLoading === data.id"
              aria-label="Bearbeiten"
              data-testid="btn-bearbeiten"
              @click="handleBearbeiten(data)"
            />
            <Button
              icon="pi pi-ban"
              size="small"
              severity="danger"
              :disabled="actionLoading === data.id"
              aria-label="Ignorieren"
              data-testid="btn-ignorieren"
              @click="handleIgnorieren(data)"
            />
          </div>
          <span v-else class="text-muted">—</span>
        </template>
      </Column>
    </DataTable>

    <BestellModal
      v-model:visible="modalVisible"
      :vorschlag="selectedVorschlag"
      @bestellt="handleBestellt"
    />
  </div>
</template>

<style scoped>
.aktionen {
  display: flex;
  gap: 0.35rem;
  flex-wrap: wrap;
}

.text-danger { color: #ef4444; font-weight: 600; }
.text-muted  { color: var(--p-text-muted-color, #94a3b8); }

:deep(.row--kritisch td) { background-color: #fef2f2 !important; }
:deep(.row--normal td)   { background-color: #fefce8 !important; }
:deep(.row--bestellt td) { background-color: var(--p-surface-100, #f1f5f9) !important; color: var(--p-text-muted-color, #94a3b8); }
</style>
