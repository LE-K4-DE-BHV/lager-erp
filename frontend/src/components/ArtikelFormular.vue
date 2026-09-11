<script setup>
import { ref, watch, computed } from 'vue'
import { useToast } from 'primevue/usetoast'
import { artikelService } from '@/api/services/artikelService'
import { lieferantenService } from '@/api/services/lieferantenService'

const props = defineProps({
  visible:  { type: Boolean, required: true },
  artikel:  { type: Object, default: null }
})

const emit = defineEmits(['update:visible', 'gespeichert'])

const toast = useToast()
const loading = ref(false)
const lieferanten = ref([])
const duplikatFehler = ref(false)

const istBearbeitung = computed(() => props.artikel !== null)

const leerFormular = () => ({
  artikelnummer:       '',
  bezeichnung:         '',
  mengeneinheit:       '',
  warengruppe:         '',
  lieferantId:         null,
  sicherheitsbestand:  0,
  bestellpunkt:        0,
  standardBestellmenge: 0,
  einkaufspreis:       0,
})

const form = ref(leerFormular())

const isValid = computed(() =>
  form.value.bezeichnung.trim() &&
  form.value.mengeneinheit.trim() &&
  form.value.warengruppe.trim() &&
  form.value.lieferantId !== null &&
  (istBearbeitung.value || form.value.artikelnummer.trim())
)

watch(() => props.visible, async (open) => {
  if (!open) return
  duplikatFehler.value = false
  form.value = props.artikel
    ? {
        artikelnummer:        props.artikel.artikelnummer,
        bezeichnung:          props.artikel.bezeichnung,
        mengeneinheit:        props.artikel.mengeneinheit,
        warengruppe:          props.artikel.warengruppe,
        lieferantId:          props.artikel.lieferantId,
        sicherheitsbestand:   props.artikel.sicherheitsbestand,
        bestellpunkt:         props.artikel.bestellpunkt,
        standardBestellmenge: props.artikel.standardBestellmenge,
        einkaufspreis:        props.artikel.einkaufspreis,
      }
    : leerFormular()

  try {
    const { data } = await lieferantenService.getAll()
    lieferanten.value = data
  } catch {
    lieferanten.value = []
  }
})

const handleSubmit = async () => {
  if (!isValid.value) return
  duplikatFehler.value = false
  loading.value = true
  try {
    if (istBearbeitung.value) {
      await artikelService.update(props.artikel.id, form.value)
    } else {
      await artikelService.create(form.value)
    }
    toast.add({ severity: 'success', summary: 'Gespeichert', life: 3000 })
    emit('update:visible', false)
    emit('gespeichert')
  } catch (err) {
    if (err.response?.status === 409) {
      duplikatFehler.value = true
    } else {
      toast.add({ severity: 'error', summary: 'Fehler', detail: 'Artikel konnte nicht gespeichert werden.', life: 4000 })
    }
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  emit('update:visible', false)
}
</script>

<template>
  <Dialog
    :visible="visible"
    modal
    :header="istBearbeitung ? 'Artikel bearbeiten' : 'Neuen Artikel anlegen'"
    :style="{ width: '560px' }"
    @update:visible="handleClose"
  >
    <form class="artikel-form" @submit.prevent="handleSubmit">
      <Message
        v-if="duplikatFehler"
        severity="error"
        :closable="false"
        data-testid="duplikat-fehler"
      >
        Artikelnummer bereits vorhanden. Bitte eine andere Artikelnummer wählen.
      </Message>

      <div class="form-grid">
        <div class="p-field">
          <label for="artikelnummer">Artikelnummer *</label>
          <InputText
            id="artikelnummer"
            v-model="form.artikelnummer"
            :readonly="istBearbeitung"
            :class="{ 'p-disabled': istBearbeitung }"
            placeholder="ART-001"
            data-testid="input-artikelnummer"
          />
        </div>

        <div class="p-field">
          <label for="bezeichnung">Bezeichnung *</label>
          <InputText
            id="bezeichnung"
            v-model="form.bezeichnung"
            placeholder="Schrauben M6"
            data-testid="input-bezeichnung"
          />
        </div>

        <div class="p-field">
          <label for="mengeneinheit">Mengeneinheit *</label>
          <InputText
            id="mengeneinheit"
            v-model="form.mengeneinheit"
            placeholder="Stück"
            data-testid="input-mengeneinheit"
          />
        </div>

        <div class="p-field">
          <label for="warengruppe">Warengruppe *</label>
          <InputText
            id="warengruppe"
            v-model="form.warengruppe"
            placeholder="Befestigungsmaterial"
            data-testid="input-warengruppe"
          />
        </div>

        <div class="p-field p-field--full">
          <label for="lieferant">Lieferant *</label>
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
          <label for="sicherheitsbestand">Sicherheitsbestand</label>
          <InputNumber
            id="sicherheitsbestand"
            v-model="form.sicherheitsbestand"
            :min="0"
            data-testid="input-sicherheitsbestand"
          />
        </div>

        <div class="p-field">
          <label for="bestellpunkt">Bestellpunkt</label>
          <InputNumber
            id="bestellpunkt"
            v-model="form.bestellpunkt"
            :min="0"
            data-testid="input-bestellpunkt"
          />
        </div>

        <div class="p-field">
          <label for="standardBestellmenge">Standardbestellmenge</label>
          <InputNumber
            id="standardBestellmenge"
            v-model="form.standardBestellmenge"
            :min="0"
            data-testid="input-standardBestellmenge"
          />
        </div>

        <div class="p-field">
          <label for="einkaufspreis">Einkaufspreis (€)</label>
          <InputNumber
            id="einkaufspreis"
            v-model="form.einkaufspreis"
            :min="0"
            :max-fraction-digits="2"
            mode="decimal"
            data-testid="input-einkaufspreis"
          />
        </div>
      </div>
    </form>

    <template #footer>
      <Button label="Abbrechen" severity="secondary" @click="handleClose" />
      <Button
        label="Speichern"
        icon="pi pi-check"
        :loading="loading"
        :disabled="!isValid"
        data-testid="btn-speichern"
        @click="handleSubmit"
      />
    </template>
  </Dialog>
</template>

<style scoped>
.artikel-form {
  padding-top: 0.5rem;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.25rem;
  margin-top: 1rem;
}

.p-field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.p-field--full {
  grid-column: 1 / -1;
}

.p-field label {
  font-size: 0.875rem;
  font-weight: 500;
}

.p-field :deep(input),
.p-field :deep(.p-select),
.p-field :deep(.p-inputnumber) {
  width: 100%;
}
</style>
