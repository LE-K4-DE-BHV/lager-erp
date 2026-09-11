<script setup>
import { ref, computed, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import { bewegungService } from '@/api/services/bewegungService'
import { artikelService } from '@/api/services/artikelService'

const toast = useToast()

const BUCHUNGSTYPEN = [
  { label: 'Verbrauch intern', value: 'VERBRAUCH_INTERN' },
  { label: 'Verkauf',          value: 'VERKAUF' },
  { label: 'Verlust/Schwund',  value: 'VERLUST_SCHWUND' },
  { label: 'Retoure',          value: 'RETOURE' },
]

const aktiveArtikel = ref([])
const loading = ref(false)
const submitting = ref(false)
const fehler = ref('')

const leerForm = () => ({
  artikelId:   null,
  menge:       null,
  datum:       new Date(),
  buchungstyp: null,
  grund:       '',
})

const form = ref(leerForm())

const isValid = computed(() =>
  form.value.artikelId !== null &&
  form.value.menge > 0 &&
  form.value.datum !== null &&
  form.value.buchungstyp !== null
)

const loadArtikel = async () => {
  loading.value = true
  try {
    const { data } = await artikelService.getAll()
    aktiveArtikel.value = data.filter(a => a.status === 'AKTIV')
  } catch {
    toast.add({ severity: 'error', summary: 'Fehler', detail: 'Artikelliste konnte nicht geladen werden.', life: 4000 })
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.value = leerForm()
  fehler.value = ''
}

const handleSubmit = async () => {
  if (!isValid.value) return
  fehler.value = ''
  submitting.value = true
  try {
    await bewegungService.bucheAusgang({
      artikelId:   form.value.artikelId,
      menge:       form.value.menge,
      datum:       form.value.datum,
      buchungstyp: form.value.buchungstyp,
      grund:       form.value.grund || undefined,
    })
    toast.add({ severity: 'success', summary: 'Warenausgang gebucht', life: 3000 })
    resetForm()
  } catch (err) {
    if (err.response?.status === 422) {
      fehler.value = 'Nicht genügend Bestand vorhanden.'
    } else {
      toast.add({ severity: 'error', summary: 'Fehler', detail: 'Buchung fehlgeschlagen.', life: 4000 })
    }
  } finally {
    submitting.value = false
  }
}

onMounted(loadArtikel)
</script>

<template>
  <div class="bewegung-form">
    <h2 class="bewegung-form__title">Warenausgang buchen</h2>

    <form @submit.prevent="handleSubmit">
      <Message
        v-if="fehler"
        severity="error"
        :closable="false"
        data-testid="ausgang-fehler"
      >
        {{ fehler }}
      </Message>

      <div class="p-field">
        <label for="ausgang-artikel">Artikel *</label>
        <Select
          id="ausgang-artikel"
          v-model="form.artikelId"
          :options="aktiveArtikel"
          option-label="bezeichnung"
          option-value="id"
          placeholder="Artikel wählen"
          :loading="loading"
          data-testid="select-artikel-ausgang"
        />
      </div>

      <div class="p-field">
        <label for="ausgang-menge">Menge *</label>
        <InputNumber
          id="ausgang-menge"
          v-model="form.menge"
          :min="1"
          placeholder="0"
          data-testid="input-menge-ausgang"
        />
      </div>

      <div class="p-field">
        <label for="ausgang-datum">Datum *</label>
        <DatePicker
          id="ausgang-datum"
          v-model="form.datum"
          date-format="dd.mm.yy"
          :max-date="new Date()"
          placeholder="TT.MM.JJJJ"
          data-testid="input-datum-ausgang"
        />
      </div>

      <div class="p-field">
        <label for="buchungstyp">Buchungstyp *</label>
        <Select
          id="buchungstyp"
          v-model="form.buchungstyp"
          :options="BUCHUNGSTYPEN"
          option-label="label"
          option-value="value"
          placeholder="Buchungstyp wählen"
          data-testid="select-buchungstyp"
        />
      </div>

      <div class="p-field">
        <label for="grund">Grund (optional)</label>
        <Textarea
          id="grund"
          v-model="form.grund"
          rows="2"
          placeholder="Optionale Bemerkung..."
          auto-resize
          data-testid="input-grund"
        />
      </div>

      <Button
        type="submit"
        label="Ausgang buchen"
        icon="pi pi-arrow-up"
        severity="warning"
        :loading="submitting"
        :disabled="!isValid"
        data-testid="btn-ausgang-buchen"
      />
    </form>
  </div>
</template>

<style scoped>
.bewegung-form {
  max-width: 480px;
}

.bewegung-form__title {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 1.5rem;
  color: var(--p-text-color, #1e293b);
}

form {
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
}

.p-field :deep(input),
.p-field :deep(.p-select),
.p-field :deep(.p-datepicker),
.p-field :deep(.p-inputnumber),
.p-field :deep(.p-textarea) {
  width: 100%;
}
</style>
