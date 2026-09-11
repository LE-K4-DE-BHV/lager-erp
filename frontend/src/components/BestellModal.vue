<script setup>
import { ref, watch } from 'vue'
import { useToast } from 'primevue/usetoast'
import { dashboardService } from '@/api/services/dashboardService'
import { lieferantenService } from '@/api/services/lieferantenService'

const props = defineProps({
  visible: { type: Boolean, required: true },
  vorschlag: { type: Object, default: null }
})

const emit = defineEmits(['update:visible', 'bestellt'])

const toast = useToast()
const loading = ref(false)
const lieferanten = ref([])

const form = ref({
  bestellmenge: 0,
  lieferantId: null,
  gewuenschtesDatum: null,
  notiz: ''
})

watch(() => props.vorschlag, (v) => {
  if (!v) return
  form.value.bestellmenge = v.vorgeschlageneMenge
  form.value.lieferantId  = null
  form.value.gewuenschtesDatum = null
  form.value.notiz = ''
})

watch(() => props.visible, async (open) => {
  if (!open) return
  try {
    const { data } = await lieferantenService.getAll()
    lieferanten.value = data
  } catch {
    lieferanten.value = []
  }
})

const handleSubmit = async () => {
  loading.value = true
  try {
    await dashboardService.bestellungAufgeben(props.vorschlag.id, {
      bestellmenge: form.value.bestellmenge,
      gewuenschtesLieferdatum: form.value.gewuenschtesDatum,
      notiz: form.value.notiz
    })
    toast.add({ severity: 'success', summary: 'Bestellung aufgegeben', life: 3000 })
    emit('update:visible', false)
    emit('bestellt')
  } catch {
    toast.add({ severity: 'error', summary: 'Fehler', detail: 'Bestellung konnte nicht aufgegeben werden.', life: 4000 })
  } finally {
    loading.value = false
  }
}

const handleClose = () => emit('update:visible', false)
</script>

<template>
  <Dialog
    :visible="visible"
    modal
    header="Bestellung aufgeben"
    :style="{ width: '480px' }"
    @update:visible="handleClose"
  >
    <form v-if="vorschlag" class="bestell-form" @submit.prevent="handleSubmit">
      <div class="p-field">
        <label>Artikel</label>
        <p class="bestell-form__info">
          {{ vorschlag.artikelBezeichnung }} ({{ vorschlag.artikelnummer }})
        </p>
      </div>

      <div class="p-field">
        <label for="bestellmenge">Bestellmenge</label>
        <InputNumber
          id="bestellmenge"
          v-model="form.bestellmenge"
          :min="1"
          show-buttons
          data-testid="input-bestellmenge"
        />
      </div>

      <div class="p-field">
        <label for="lieferant">Lieferant</label>
        <Select
          id="lieferant"
          v-model="form.lieferantId"
          :options="lieferanten"
          option-label="name"
          option-value="id"
          placeholder="Lieferant wählen"
          data-testid="select-lieferant"
        />
      </div>

      <div class="p-field">
        <label for="lieferdatum">Gewünschtes Lieferdatum</label>
        <DatePicker
          id="lieferdatum"
          v-model="form.gewuenschtesDatum"
          date-format="dd.mm.yy"
          :min-date="new Date()"
          placeholder="TT.MM.JJJJ"
          data-testid="input-lieferdatum"
        />
      </div>

      <div class="p-field">
        <label for="notiz">Notiz</label>
        <Textarea
          id="notiz"
          v-model="form.notiz"
          rows="3"
          placeholder="Optionale Bemerkung..."
          auto-resize
          data-testid="input-notiz"
        />
      </div>
    </form>

    <template #footer>
      <Button label="Abbrechen" severity="secondary" @click="handleClose" />
      <Button
        label="Bestellung freigeben"
        icon="pi pi-check"
        :loading="loading"
        :disabled="!form.bestellmenge"
        data-testid="btn-bestellen"
        @click="handleSubmit"
      />
    </template>
  </Dialog>
</template>

<style scoped>
.bestell-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  padding-top: 0.5rem;
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
.p-field :deep(.p-textarea) {
  width: 100%;
}

.bestell-form__info {
  margin: 0;
  font-weight: 500;
  color: var(--p-text-color, #1e293b);
}
</style>
