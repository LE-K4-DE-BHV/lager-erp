<script setup>
import { ref, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import { lieferantenService } from '@/api/services/lieferantenService'

const toast = useToast()

const lieferanten = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)

const leerForm = () => ({
  id: null,
  lieferantId: '',
  name: '',
  kontaktEmail: '',
  kontaktTelefon: '',
  leadTimeTage: 0,
})

const form = ref(leerForm())

const loadLieferanten = async () => {
  loading.value = true
  try {
    const { data } = await lieferantenService.getAll()
    lieferanten.value = data
  } catch {
    toast.add({ severity: 'error', summary: 'Fehler', detail: 'Lieferanten konnten nicht geladen werden.', life: 4000 })
  } finally {
    loading.value = false
  }
}

const handleBearbeiten = (lieferant) => {
  form.value = { ...lieferant }
  dialogVisible.value = true
}

const handleClose = () => {
  dialogVisible.value = false
}

const handleSpeichern = async () => {
  saving.value = true
  try {
    await lieferantenService.update(form.value.id, {
      name:           form.value.name,
      kontaktEmail:   form.value.kontaktEmail,
      kontaktTelefon: form.value.kontaktTelefon,
      leadTimeTage:   form.value.leadTimeTage,
    })
    toast.add({ severity: 'success', summary: 'Gespeichert', life: 3000 })
    dialogVisible.value = false
    await loadLieferanten()
  } catch {
    toast.add({ severity: 'error', summary: 'Fehler', detail: 'Lieferant konnte nicht gespeichert werden.', life: 4000 })
  } finally {
    saving.value = false
  }
}

onMounted(loadLieferanten)

defineExpose({ handleBearbeiten, dialogVisible, form })
</script>

<template>
  <div class="lieferanten-seite">
    <div class="lieferanten-seite__header">
      <h1 class="lieferanten-seite__title">Lieferanten</h1>
    </div>

    <Message severity="info" :closable="false" class="lieferanten-seite__info">
      Neue Lieferanten können nur per Import-Datei (Batch) angelegt werden.
    </Message>

    <DataTable
      :value="lieferanten"
      :loading="loading"
      striped-rows
      data-testid="lieferanten-tabelle"
      empty-message="Keine Lieferanten vorhanden."
    >
      <Column field="lieferantId" header="Lieferant-ID" style="width: 140px" />
      <Column field="name" header="Name" />
      <Column field="kontaktEmail" header="E-Mail" />
      <Column field="kontaktTelefon" header="Telefon" style="width: 150px" />
      <Column field="leadTimeTage" header="Lead Time (Tage)" style="width: 150px; text-align: right" />
      <Column header="Aktionen" style="width: 100px">
        <template #body="{ data }">
          <Button
            icon="pi pi-pencil"
            size="small"
            severity="info"
            aria-label="Bearbeiten"
            data-testid="btn-bearbeiten"
            @click="handleBearbeiten(data)"
          />
        </template>
      </Column>
    </DataTable>

    <Dialog
      v-model:visible="dialogVisible"
      modal
      header="Lieferant bearbeiten"
      :style="{ width: '480px' }"
      @update:visible="handleClose"
    >
      <form class="lieferant-form" @submit.prevent="handleSpeichern">
        <div class="p-field">
          <label>Lieferant-ID</label>
          <InputText
            :model-value="form.lieferantId"
            readonly
            class="p-disabled"
            data-testid="input-lieferantId"
          />
        </div>

        <div class="p-field">
          <label for="name">Name *</label>
          <InputText
            id="name"
            v-model="form.name"
            placeholder="Firma GmbH"
            data-testid="input-name"
          />
        </div>

        <div class="p-field">
          <label for="email">E-Mail</label>
          <InputText
            id="email"
            v-model="form.kontaktEmail"
            type="email"
            placeholder="info@lieferant.de"
            data-testid="input-email"
          />
        </div>

        <div class="p-field">
          <label for="telefon">Telefon</label>
          <InputText
            id="telefon"
            v-model="form.kontaktTelefon"
            placeholder="+49 123 456789"
            data-testid="input-telefon"
          />
        </div>

        <div class="p-field">
          <label for="leadtime">Lead Time (Tage)</label>
          <InputNumber
            id="leadtime"
            v-model="form.leadTimeTage"
            :min="0"
            data-testid="input-leadtime"
          />
        </div>
      </form>

      <template #footer>
        <Button label="Abbrechen" severity="secondary" @click="handleClose" />
        <Button
          label="Speichern"
          icon="pi pi-check"
          :loading="saving"
          :disabled="!form.name"
          data-testid="btn-speichern"
          @click="handleSpeichern"
        />
      </template>
    </Dialog>
  </div>
</template>

<style scoped>
.lieferanten-seite {
  padding: 1.5rem;
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.lieferanten-seite__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.lieferanten-seite__title {
  font-size: 1.4rem;
  font-weight: 700;
  margin: 0;
  color: var(--p-text-color, #1e293b);
}

.lieferanten-seite__info {
  margin: 0;
}

.lieferant-form {
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
.p-field :deep(.p-inputnumber) {
  width: 100%;
}
</style>
