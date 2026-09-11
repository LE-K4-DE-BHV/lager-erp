<script setup>
import { ref, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import { artikelService } from '@/api/services/artikelService'
import ArtikelFormular from '@/components/ArtikelFormular.vue'

const toast = useToast()
const confirm = useConfirm()

const artikel = ref([])
const loading = ref(false)
const formVisible = ref(false)
const selectedArtikel = ref(null)
const actionLoading = ref(null)

const loadArtikel = async () => {
  loading.value = true
  try {
    const { data } = await artikelService.getAll()
    artikel.value = data
  } catch {
    toast.add({ severity: 'error', summary: 'Fehler', detail: 'Artikelliste konnte nicht geladen werden.', life: 4000 })
  } finally {
    loading.value = false
  }
}

const handleNeuAnlegen = () => {
  selectedArtikel.value = null
  formVisible.value = true
}

const handleBearbeiten = (a) => {
  selectedArtikel.value = a
  formVisible.value = true
}

const handleDeaktivieren = (a) => {
  confirm.require({
    message: `Artikel "${a.bezeichnung}" wirklich deaktivieren?`,
    header: 'Artikel deaktivieren',
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: 'Deaktivieren',
    rejectLabel: 'Abbrechen',
    acceptClass: 'p-button-danger',
    accept: async () => {
      actionLoading.value = a.id
      try {
        await artikelService.deaktivieren(a.id)
        toast.add({ severity: 'success', summary: 'Deaktiviert', life: 3000 })
        await loadArtikel()
      } catch {
        toast.add({ severity: 'error', summary: 'Fehler', detail: 'Deaktivierung fehlgeschlagen.', life: 4000 })
      } finally {
        actionLoading.value = null
      }
    }
  })
}

const handleGespeichert = () => loadArtikel()

const statusSeverity = (status) => status === 'AKTIV' ? 'success' : 'secondary'

onMounted(loadArtikel)
</script>

<template>
  <div class="artikel-seite">
    <ConfirmDialog />

    <div class="artikel-seite__header">
      <h1 class="artikel-seite__title">Artikel</h1>
      <Button
        label="Neuen Artikel anlegen"
        icon="pi pi-plus"
        data-testid="btn-neu-anlegen"
        @click="handleNeuAnlegen"
      />
    </div>

    <DataTable
      :value="artikel"
      :loading="loading"
      striped-rows
      data-testid="artikel-tabelle"
      empty-message="Keine Artikel vorhanden."
    >
      <Column field="artikelnummer" header="Art.-Nr." style="width: 120px" />
      <Column field="bezeichnung" header="Bezeichnung" />
      <Column field="mengeneinheit" header="Einheit" style="width: 90px" />
      <Column field="warengruppe" header="Warengruppe" />
      <Column field="lieferantName" header="Lieferant" />
      <Column field="aktuellerBestand" header="Bestand" style="width: 90px; text-align: right" />
      <Column field="bestellpunkt" header="Bestellpunkt" style="width: 110px; text-align: right" />
      <Column header="Status" style="width: 100px">
        <template #body="{ data }">
          <Tag :value="data.status" :severity="statusSeverity(data.status)" />
        </template>
      </Column>
      <Column header="Aktionen" style="width: 180px">
        <template #body="{ data }">
          <div class="aktionen">
            <Button
              icon="pi pi-pencil"
              size="small"
              severity="info"
              aria-label="Bearbeiten"
              data-testid="btn-bearbeiten"
              @click="handleBearbeiten(data)"
            />
            <Button
              v-if="data.status === 'AKTIV'"
              icon="pi pi-ban"
              size="small"
              severity="danger"
              :loading="actionLoading === data.id"
              aria-label="Deaktivieren"
              data-testid="btn-deaktivieren"
              @click="handleDeaktivieren(data)"
            />
          </div>
        </template>
      </Column>
    </DataTable>

    <ArtikelFormular
      v-model:visible="formVisible"
      :artikel="selectedArtikel"
      @gespeichert="handleGespeichert"
    />
  </div>
</template>

<style scoped>
.artikel-seite {
  padding: 1.5rem;
  max-width: 1400px;
  margin: 0 auto;
}

.artikel-seite__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}

.artikel-seite__title {
  font-size: 1.4rem;
  font-weight: 700;
  margin: 0;
  color: var(--p-text-color, #1e293b);
}

.aktionen {
  display: flex;
  gap: 0.35rem;
}
</style>
