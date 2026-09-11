import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import PrimeVue from 'primevue/config'
import ToastService from 'primevue/toastservice'
import LieferantenView from '../views/LieferantenView.vue'
import * as lieferantenServiceModule from '../api/services/lieferantenService'

vi.mock('../api/services/lieferantenService', () => ({
  lieferantenService: {
    getAll:  vi.fn(),
    update:  vi.fn(),
  }
}))

const testLieferanten = [
  { id: 1, lieferantId: 'LIF-001', name: 'Müller GmbH', kontaktEmail: 'info@mueller.de', kontaktTelefon: '0800 123', leadTimeTage: 5 },
  { id: 2, lieferantId: 'LIF-002', name: 'Schmidt AG',  kontaktEmail: 'info@schmidt.de', kontaktTelefon: '0800 456', leadTimeTage: 7 },
]

const inputStub = {
  props: ['modelValue'], emits: ['update:modelValue'], inheritAttrs: false,
  template: '<input :value="modelValue" v-bind="$attrs" @input="$emit(\'update:modelValue\', $event.target.value)" />'
}

const globalStubs = {
  DataTable: {
    template: `
      <div data-testid="lieferanten-tabelle">
        <div v-for="row in value" :key="row.id">
          <span>{{ row.name }}</span>
          <span>{{ row.lieferantId }}</span>
          <button data-testid="btn-bearbeiten" @click="$emit('edit', row)">Bearbeiten</button>
        </div>
      </div>
    `,
    props: ['value', 'loading'],
    emits: ['edit']
  },
  Column:     { template: '<div />' },
  Button: {
    template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>',
    inheritAttrs: true, emits: ['click']
  },
  Message:    { template: '<div data-testid="info-banner"><slot /></div>' },
  Dialog:     { template: '<div v-if="visible"><slot /><slot name="footer" /></div>', props: ['visible'], emits: ['update:visible'] },
  InputText:  inputStub,
  InputNumber: {
    props: ['modelValue'], emits: ['update:modelValue'], inheritAttrs: false,
    template: '<input type="number" :value="modelValue" v-bind="$attrs" @input="$emit(\'update:modelValue\', Number($event.target.value))" />'
  },
}

const mountView = () =>
  mount(LieferantenView, {
    global: {
      plugins: [PrimeVue, ToastService, createTestingPinia({ createSpy: vi.fn })],
      stubs: globalStubs
    }
  })

describe('LieferantenView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    lieferantenServiceModule.lieferantenService.getAll.mockResolvedValue({ data: testLieferanten })
  })

  it('lädt Lieferantenliste beim Mounten', async () => {
    mountView()
    await flushPromises()

    expect(lieferantenServiceModule.lieferantenService.getAll).toHaveBeenCalledOnce()
  })

  it('zeigt alle Lieferanten in der Tabelle', async () => {
    const wrapper = mountView()
    await flushPromises()

    expect(wrapper.text()).toContain('Müller GmbH')
    expect(wrapper.text()).toContain('Schmidt AG')
  })

  it('enthält keinen "Neuen Lieferanten anlegen"-Button', async () => {
    const wrapper = mountView()
    await flushPromises()

    expect(wrapper.text()).not.toContain('Neuen Lieferanten anlegen')
  })

  it('zeigt Info-Banner zur Batch-only-Anlage', async () => {
    const wrapper = mountView()
    await flushPromises()

    expect(wrapper.find('[data-testid="info-banner"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="info-banner"]').text()).toContain('Import-Datei')
  })

  it('öffnet den Bearbeitungs-Dialog mit korrekten Daten', async () => {
    const wrapper = mountView()
    await flushPromises()

    // handleBearbeiten über defineExpose direkt aufrufen (DataTable-Stub rendert keinen Column-Slot)
    wrapper.vm.handleBearbeiten(testLieferanten[0])
    await wrapper.vm.$nextTick()

    const nameInput = wrapper.find('[data-testid="input-name"]')
    expect(nameInput.exists()).toBe(true)
    expect(nameInput.element.value).toBe('Müller GmbH')
  })

  it('ruft update() auf und schließt den Dialog nach Speichern', async () => {
    lieferantenServiceModule.lieferantenService.update.mockResolvedValue({ data: testLieferanten[0] })
    const wrapper = mountView()
    await flushPromises()

    wrapper.vm.handleBearbeiten(testLieferanten[0])
    await wrapper.vm.$nextTick()

    await wrapper.find('[data-testid="btn-speichern"]').trigger('click')
    await flushPromises()

    expect(lieferantenServiceModule.lieferantenService.update).toHaveBeenCalledOnce()
    expect(lieferantenServiceModule.lieferantenService.getAll).toHaveBeenCalledTimes(2)
  })
})
