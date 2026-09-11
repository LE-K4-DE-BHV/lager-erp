<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import { transaktionService } from '@/api/services/transaktionService'
import { artikelService } from '@/api/services/artikelService'

const toast = useToast()

const transaktionen = ref([])
const artikel = ref([])
const loading = ref(false)
const totalRecords = ref(0)

const filter = reactive({
  artikelId: null,
  von:       null,
  bis:       null,
})

const pagination = reactive({
  seite:   0,
  groesse: 50,
})

const formatDate = (isoString) => {
  if (!isoString) return '—'
  return new Date(isoString).toLocaleDateString('de-DE', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

const formatIsoDate = (date) => {
  if (!date) return undefined
  const d = new Date(date)
  return d.toISOString().split('T')[0]
}

const loadTransaktionen = async () => {
  loading.value = true
  try {
    const { data } = await transaktionService.getAll({
      artikelId: filter.artikelId || undefined,
      von:       formatIsoDate(filter.von),
      bis:       formatIsoDate(filter.bis),
      seite:     pagination.seite,
      groesse:   pagination.groesse,
    })
    transaktionen.value = data.content ?? data
    totalRecords.value  = data.totalElements ?? (data.length ?? 0)
  } catch {
    toast.add({ severity: 'error', summary: 'Fehler', detail: 'Transaktionen konnten nicht geladen werden.', life: 4000 })
  } finally {
    loading.value = false
  }
}

const loadArtikel = async () => {
  try {
    const { data } = await artikelService.getAll()
    artikel.value = data
  } catch {
    artikel.value = []
  }
}

const handleFilterAnwenden = () => {
  pagination.seite = 0
  loadTransaktionen()
}

const handlePageChange = (event) => {
  pagination.seite = event.page
  loadTransaktionen()
}

const typSeverity = (typ) => typ === 'EINGANG' ? 'success' : 'danger'
const typIcon    = (typ) => typ === 'EINGANG' ? 'pi pi-arrow-down' : 'pi pi-arrow-up'

onMounted(() => {
  loadArtikel()
  loadTransaktionen()
})
</script>

<template>
  <div class="historien-seite">
    <h1 class="historien-seite__title">Transaktionshistorie</h1>

    <div class="filter-bereich">
      <div class="p-field">
        <label>Artikel</label>
        <Select
          v-model="filter.artikelId"
          :options="artikel"
          option-label="bezeichnung"
          option-value="id"
          placeholder="Alle Artikel"
          show-clear
          data-testid="filter-artikel"
        />
      </div>
      <div class="p-field">
        <label>Von</label>
        <DatePicker
          v-model="filter.von"
          date-format="dd.mm.yy"
          placeholder="TT.MM.JJJJ"
          show-clear
          data-testid="filter-von"
        />
      </div>
      <div class="p-field">
        <label>Bis</label>
        <DatePicker
          v-model="filter.bis"
          date-format="dd.mm.yy"
          placeholder="TT.MM.JJJJ"
          show-clear
          data-testid="filter-bis"
        />
      </div>
      <Button
        label="Filter anwenden"
        icon="pi pi-filter"
        :loading="loading"
        data-testid="btn-filter"
        @click="handleFilterAnwenden"
      />
    </div>

    <DataTable
      :value="transaktionen"
      :loading="loading"
      lazy
      :total-records="totalRecords"
      :rows="pagination.groesse"
      paginator
      striped-rows
      data-testid="transaktionen-tabelle"
      empty-message="Keine Transaktionen gefunden."
      @page="handlePageChange"
    >
      <Column field="datum" header="Datum" style="width: 150px">
        <template #body="{ data }">{{ formatDate(data.datum) }}</template>
      </Column>
      <Column field="artikelnummer" header="Art.-Nr." style="width: 110px" />
      <Column field="artikelBezeichnung" header="Bezeichnung" />
      <Column header="Typ" style="width: 110px">
        <template #body="{ data }">
          <Tag
            :value="data.typ"
            :severity="typSeverity(data.typ)"
          >
            <template #default>
              <span class="typ-tag">
                <i :class="typIcon(data.typ)" />
                {{ data.typ }}
              </span>
            </template>
          </Tag>
        </template>
      </Column>
      <Column field="menge" header="Menge" style="width: 90px; text-align: right" />
      <Column field="buchungstyp" header="Buchungstyp" style="width: 140px" />
      <Column field="quelle" header="Quelle" style="width: 100px">
        <template #body="{ data }">
          <Tag
            :value="data.quelle"
            :severity="data.quelle === 'MANUELL' ? 'info' : 'secondary'"
          />
        </template>
      </Column>
      <Column field="benutzerId" header="Benutzer" style="width: 100px" />
    </DataTable>
  </div>
</template>

<style scoped>
.historien-seite {
  padding: 1.5rem;
  max-width: 1400px;
  margin: 0 auto;
}

.historien-seite__title {
  font-size: 1.4rem;
  font-weight: 700;
  margin: 0 0 1.5rem;
  color: var(--p-text-color, #1e293b);
}

.filter-bereich {
  display: flex;
  align-items: flex-end;
  gap: 1rem;
  flex-wrap: wrap;
  margin-bottom: 1.5rem;
  padding: 1rem 1.25rem;
  background: var(--p-surface-0, #fff);
  border: 1px solid var(--p-surface-200, #e2e8f0);
  border-radius: 10px;
}

.p-field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  min-width: 200px;
}

.p-field label {
  font-size: 0.8rem;
  font-weight: 500;
  color: var(--p-text-muted-color, #64748b);
}

.p-field :deep(input),
.p-field :deep(.p-select),
.p-field :deep(.p-datepicker) {
  width: 100%;
}

.typ-tag {
  display: flex;
  align-items: center;
  gap: 0.3rem;
}
</style>
