<script setup>
import { ref, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import { bewegungService } from '@/api/services/bewegungService'
import { artikelService } from '@/api/services/artikelService'

const toast = useToast()
const confirm = useConfirm()

const aktiveArtikel = ref([])
const loading = ref(false)
const submitting = ref(false)
const fehler = ref('')

const leerForm = () => ({
  artikelId: null,
  menge:     null,
  datum:     new Date(),
})

const form = ref(leerForm())

const isValid = computed(() =>
  form.value.artikelId !== null &&
  form.value.menge > 0 &&
  form.value.datum !== null
)

import { computed } from 'vue'

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
    const { data } = await bewegungService.bucheEingang({
      artikelId: form.value.artikelId,
      menge:     form.value.menge,
      datum:     form.value.datum,
    })

    if (data.bestellungKannGeschlossenWerden) {
      confirm.require({
        message: 'Soll die zugehörige Bestellung als "Geliefert" markiert werden?',
        header: 'Bestellung abschließen',
        icon: 'pi pi-question-circle',
        acceptLabel: 'Ja, abschließen',
        rejectLabel: 'Nein',
        accept: async () => {
          try {
            await bewegungService.schliesseBestellung(data.transaktionId)
            toast.add({ severity: 'success', summary: 'Bestellung abgeschlossen', life: 3000 })
          } catch {
            toast.add({ severity: 'error', summary: 'Fehler', detail: 'Bestellung konnte nicht abgeschlossen werden.', life: 4000 })
          }
        },
        reject: () => {}
      })
    }

    toast.add({ severity: 'success', summary: 'Wareneingang gebucht', life: 3000 })
    resetForm()
  } catch (err) {
    if (err.response?.status === 422) {
      fehler.value = 'Keine offene Bestellung für diesen Artikel vorhanden.'
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
    <h2 class="bewegung-form__title">Wareneingang buchen</h2>

    <form @submit.prevent="handleSubmit">
      <Message
        v-if="fehler"
        severity="error"
        :closable="false"
        data-testid="eingang-fehler"
      >
        {{ fehler }}
      </Message>

      <div class="p-field">
        <label for="eingang-artikel">Artikel *</label>
        <Select
          id="eingang-artikel"
          v-model="form.artikelId"
          :options="aktiveArtikel"
          option-label="bezeichnung"
          option-value="id"
          placeholder="Artikel wählen"
          :loading="loading"
          data-testid="select-artikel-eingang"
        />
      </div>

      <div class="p-field">
        <label for="eingang-menge">Menge *</label>
        <InputNumber
          id="eingang-menge"
          v-model="form.menge"
          :min="1"
          placeholder="0"
          data-testid="input-menge-eingang"
        />
      </div>

      <div class="p-field">
        <label for="eingang-datum">Datum *</label>
        <DatePicker
          id="eingang-datum"
          v-model="form.datum"
          date-format="dd.mm.yy"
          :max-date="new Date()"
          placeholder="TT.MM.JJJJ"
          data-testid="input-datum-eingang"
        />
      </div>

      <Button
        type="submit"
        label="Eingang buchen"
        icon="pi pi-arrow-down"
        :loading="submitting"
        :disabled="!isValid"
        data-testid="btn-eingang-buchen"
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
.p-field :deep(.p-inputnumber) {
  width: 100%;
}
</style>
