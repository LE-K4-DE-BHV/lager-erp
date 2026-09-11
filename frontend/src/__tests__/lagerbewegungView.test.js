import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import PrimeVue from 'primevue/config'
import ToastService from 'primevue/toastservice'
import ConfirmationService from 'primevue/confirmationservice'
import * as bewegungServiceModule from '../api/services/bewegungService'
import * as artikelServiceModule from '../api/services/artikelService'

vi.mock('../api/services/bewegungService', () => ({
  bewegungService: {
    bucheEingang:        vi.fn(),
    schliesseBestellung: vi.fn(),
    bucheAusgang:        vi.fn(),
  }
}))

vi.mock('../api/services/artikelService', () => ({
  artikelService: {
    getAll: vi.fn(),
  }
}))

const testArtikel = [
  { id: 1, bezeichnung: 'Schrauben M6', status: 'AKTIV' },
  { id: 2, bezeichnung: 'Inaktiv Artikel', status: 'INAKTIV' },
]

const selectStub = {
  props: ['modelValue', 'options', 'optionLabel', 'optionValue'],
  emits: ['update:modelValue'], inheritAttrs: false,
  template: '<select v-bind="$attrs" @change="$emit(\'update:modelValue\', Number($event.target.value) || $event.target.value)"><option v-for="o in options" :key="o[optionValue || \'value\']" :value="o[optionValue || \'value\']">{{ o[optionLabel || \'label\'] }}</option></select>'
}

const inputNumberStub = {
  props: ['modelValue'], emits: ['update:modelValue'], inheritAttrs: false,
  template: '<input type="number" :value="modelValue" v-bind="$attrs" @input="$emit(\'update:modelValue\', Number($event.target.value))" />'
}

const globalPlugins = [PrimeVue, ToastService, ConfirmationService, createTestingPinia({ createSpy: vi.fn })]

// --- WareineingangFormular Tests ---
describe('WareineingangFormular', () => {
  let WareineingangFormular

  const mountEingang = () =>
    mount(WareineingangFormular, {
      global: {
        plugins: globalPlugins,
        stubs: {
          Select:    selectStub,
          InputNumber: inputNumberStub,
          DatePicker: { props: ['modelValue'], emits: ['update:modelValue'], template: '<input type="date" v-bind="$attrs" />' },
          Message:   { template: '<div data-testid="eingang-fehler"><slot /></div>' },
          Button:    { template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>', inheritAttrs: true, emits: ['click'] },
          ConfirmDialog: { template: '<div />' },
        }
      }
    })

  beforeEach(async () => {
    vi.clearAllMocks()
    artikelServiceModule.artikelService.getAll.mockResolvedValue({ data: testArtikel })
    WareineingangFormular = (await import('../components/WareineingangFormular.vue')).default
  })

  it('lädt nur AKTIVE Artikel in das Dropdown', async () => {
    const wrapper = mountEingang()
    await flushPromises()

    expect(artikelServiceModule.artikelService.getAll).toHaveBeenCalledOnce()
    const options = wrapper.find('[data-testid="select-artikel-eingang"]').findAll('option')
    expect(options).toHaveLength(1)
    expect(options[0].text()).toBe('Schrauben M6')
  })

  it('zeigt 422-Fehler als Inline-Fehlermeldung', async () => {
    bewegungServiceModule.bewegungService.bucheEingang.mockRejectedValue({ response: { status: 422 } })
    const wrapper = mountEingang()
    await flushPromises()

    await wrapper.find('[data-testid="select-artikel-eingang"]').setValue('1')
    await wrapper.find('[data-testid="input-menge-eingang"]').setValue('5')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.find('[data-testid="eingang-fehler"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="eingang-fehler"]').text()).toContain('Keine offene Bestellung')
  })

  it('ruft bucheEingang auf und zeigt Toast bei Erfolg', async () => {
    bewegungServiceModule.bewegungService.bucheEingang.mockResolvedValue({
      data: { transaktionId: 42, bestellungKannGeschlossenWerden: false }
    })
    const wrapper = mountEingang()
    await flushPromises()

    await wrapper.find('[data-testid="select-artikel-eingang"]').setValue('1')
    await wrapper.find('[data-testid="input-menge-eingang"]').setValue('10')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(bewegungServiceModule.bewegungService.bucheEingang).toHaveBeenCalledOnce()
    expect(wrapper.find('[data-testid="eingang-fehler"]').exists()).toBe(false)
  })
})

// --- WarenausgangFormular Tests ---
describe('WarenausgangFormular', () => {
  let WarenausgangFormular

  const mountAusgang = () =>
    mount(WarenausgangFormular, {
      global: {
        plugins: globalPlugins,
        stubs: {
          Select:    selectStub,
          InputNumber: inputNumberStub,
          DatePicker: { props: ['modelValue'], emits: ['update:modelValue'], template: '<input type="date" v-bind="$attrs" />' },
          Textarea:  { props: ['modelValue'], emits: ['update:modelValue'], template: '<textarea v-bind="$attrs" @input="$emit(\'update:modelValue\', $event.target.value)" />' },
          Message:   { template: '<div data-testid="ausgang-fehler"><slot /></div>' },
          Button:    { template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>', inheritAttrs: true, emits: ['click'] },
        }
      }
    })

  beforeEach(async () => {
    vi.clearAllMocks()
    artikelServiceModule.artikelService.getAll.mockResolvedValue({ data: testArtikel })
    WarenausgangFormular = (await import('../components/WarenausgangFormular.vue')).default
  })

  it('bietet die 4 festen Buchungstypen im Dropdown an', async () => {
    const wrapper = mountAusgang()
    await flushPromises()

    const options = wrapper.find('[data-testid="select-buchungstyp"]').findAll('option')
    const labels = options.map(o => o.text())
    expect(labels).toContain('Verbrauch intern')
    expect(labels).toContain('Verkauf')
    expect(labels).toContain('Verlust/Schwund')
    expect(labels).toContain('Retoure')
    expect(labels).toHaveLength(4)
  })

  it('zeigt 422-Fehler "Nicht genügend Bestand" als Inline-Fehlermeldung', async () => {
    bewegungServiceModule.bewegungService.bucheAusgang.mockRejectedValue({ response: { status: 422 } })
    const wrapper = mountAusgang()
    await flushPromises()

    await wrapper.find('[data-testid="select-artikel-ausgang"]').setValue('1')
    await wrapper.find('[data-testid="input-menge-ausgang"]').setValue('999')
    await wrapper.find('[data-testid="select-buchungstyp"]').setValue('VERKAUF')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.find('[data-testid="ausgang-fehler"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="ausgang-fehler"]').text()).toContain('Nicht genügend Bestand')
  })

  it('ruft bucheAusgang mit korrekten Daten auf', async () => {
    bewegungServiceModule.bewegungService.bucheAusgang.mockResolvedValue({ data: {} })
    const wrapper = mountAusgang()
    await flushPromises()

    await wrapper.find('[data-testid="select-artikel-ausgang"]').setValue('1')
    await wrapper.find('[data-testid="input-menge-ausgang"]').setValue('5')
    await wrapper.find('[data-testid="select-buchungstyp"]').setValue('VERBRAUCH_INTERN')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(bewegungServiceModule.bewegungService.bucheAusgang).toHaveBeenCalledOnce()
    const call = bewegungServiceModule.bewegungService.bucheAusgang.mock.calls[0][0]
    expect(call.buchungstyp).toBe('VERBRAUCH_INTERN')
  })
})
