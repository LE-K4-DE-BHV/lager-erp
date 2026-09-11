<script setup>
import { ref, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import { bestellungService } from '@/api/services/transaktionService'

const toast = useToast()
const bestellungen = ref([])
const loading = ref(false)

const formatDate = (isoString) => {
  if (!isoString) return '—'
  return new Date(isoString).toLocaleDateString('de-DE', {
    day: '2-digit', month: '2-digit', year: 'numeric'
  })
}

const loadBestellungen = async () => {
  loading.value = true
  try {
    const { data } = await bestellungService.getAll()
    bestellungen.value = data
  } catch {
    toast.add({ severity: 'error', summary: 'Fehler', detail: 'Bestellhistorie konnte nicht geladen werden.', life: 4000 })
  } finally {
    loading.value = false
  }
}

const statusSeverity = (status) => status === 'GELIEFERT' ? 'success' : 'info'

onMounted(loadBestellungen)
</script>

<template>
  <div class="historien-seite">
    <h1 class="historien-seite__title">Bestellhistorie</h1>

    <DataTable
      :value="bestellungen"
      :loading="loading"
      :sort-field="'erstelltAm'"
      :sort-order="-1"
      striped-rows
      data-testid="bestellhistorie-tabelle"
      empty-message="Keine Bestellungen vorhanden."
    >
      <Column field="bestellnummer" header="Bestellnummer" style="width: 180px" />
      <Column field="erstelltAm" header="Datum" style="width: 130px">
        <template #body="{ data }">{{ formatDate(data.erstelltAm) }}</template>
      </Column>
      <Column field="artikelBezeichnung" header="Artikel" />
      <Column field="lieferantName" header="Lieferant" />
      <Column field="bestellmenge" header="Menge" style="width: 90px; text-align: right" />
      <Column header="Status" style="width: 120px">
        <template #body="{ data }">
          <Tag :value="data.status" :severity="statusSeverity(data.status)" />
        </template>
      </Column>
      <Column field="erstelltVon" header="Erstellt von" style="width: 120px" />
    </DataTable>
  </div>
</template>

<style scoped>
.historien-seite {
  padding: 1.5rem;
  max-width: 1200px;
  margin: 0 auto;
}

.historien-seite__title {
  font-size: 1.4rem;
  font-weight: 700;
  margin: 0 0 1.5rem;
  color: var(--p-text-color, #1e293b);
}
</style>
