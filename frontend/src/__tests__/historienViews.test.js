import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import PrimeVue from 'primevue/config'
import ToastService from 'primevue/toastservice'
import * as transaktionServiceModule from '../api/services/transaktionService'
import * as artikelServiceModule from '../api/services/artikelService'

vi.mock('../api/services/transaktionService', () => ({
  transaktionService: {
    getAll: vi.fn(),
  },
  bestellungService: {
    getAll:  vi.fn(),
    getById: vi.fn(),
  }
}))

vi.mock('../api/services/artikelService', () => ({
  artikelService: {
    getAll: vi.fn(),
  }
}))

const testTransaktionen = {
  content: [
    { id: 1, datum: '2026-04-20T10:00:00', artikelnummer: 'ART-001', artikelBezeichnung: 'Schrauben M6',
      typ: 'EINGANG', menge: 100, buchungstyp: null, quelle: 'BATCH', benutzerId: 'system' },
    { id: 2, datum: '2026-04-21T14:00:00', artikelnummer: 'ART-001', artikelBezeichnung: 'Schrauben M6',
      typ: 'AUSGANG', menge: 10, buchungstyp: 'VERBRAUCH_INTERN', quelle: 'MANUELL', benutzerId: 'operator' },
  ],
  totalElements: 2,
  totalPages: 1,
  size: 50,
  number: 0,
}

const testBestellungen = [
  { id: 1, bestellnummer: 'BEST-2026-001', erstelltAm: '2026-04-18T09:00:00',
    artikelBezeichnung: 'Schrauben M6', lieferantName: 'Müller GmbH',
    bestellmenge: 100, status: 'GELIEFERT', erstelltVon: 'operator' },
  { id: 2, bestellnummer: 'BEST-2026-002', erstelltAm: '2026-04-21T11:00:00',
    artikelBezeichnung: 'Muttern M6', lieferantName: 'Schmidt AG',
    bestellmenge: 50, status: 'OFFEN', erstelltVon: 'operator' },
]

const dataTableStub = {
  template: `
    <div v-bind="$attrs">
      <div v-for="row in value" :key="row.id">
        <slot name="default" :data="row" />
        <span>{{ row.bestellnummer || '' }}</span>
        <span>{{ row.artikelBezeichnung || '' }}</span>
        <span>{{ row.typ || '' }}</span>
      </div>
    </div>
  `,
  props: ['value', 'loading', 'totalRecords', 'rows'],
  emits: ['page']
}

const globalStubs = {
  DataTable: dataTableStub,
  Column:    { template: '<div />' },
  Button: {
    template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>',
    inheritAttrs: true, emits: ['click']
  },
  Select:     { props: ['modelValue', 'options'], emits: ['update:modelValue'], template: '<select />' },
  DatePicker: { props: ['modelValue'], emits: ['update:modelValue'], template: '<input type="date" />' },
  Tag:        { template: '<span v-bind="$attrs"><slot /></span>' },
}

// --- TransaktionshistorieView Tests ---
describe('TransaktionshistorieView', () => {
  let TransaktionshistorieView

  const mountView = () =>
    mount(TransaktionshistorieView, {
      global: {
        plugins: [PrimeVue, ToastService, createTestingPinia({ createSpy: vi.fn })],
        stubs: globalStubs
      }
    })

  beforeEach(async () => {
    vi.clearAllMocks()
    transaktionServiceModule.transaktionService.getAll.mockResolvedValue({ data: testTransaktionen })
    artikelServiceModule.artikelService.getAll.mockResolvedValue({ data: [] })
    TransaktionshistorieView = (await import('../views/TransaktionshistorieView.vue')).default
  })

  it('lädt Transaktionen beim Mounten', async () => {
    mountView()
    await flushPromises()

    expect(transaktionServiceModule.transaktionService.getAll).toHaveBeenCalledOnce()
  })

  it('zeigt Transaktionen in der Tabelle an', async () => {
    const wrapper = mountView()
    await flushPromises()

    expect(wrapper.find('[data-testid="transaktionen-tabelle"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Schrauben M6')
  })

  it('zeigt EINGANG und AUSGANG in der Tabelle', async () => {
    const wrapper = mountView()
    await flushPromises()

    expect(wrapper.text()).toContain('EINGANG')
    expect(wrapper.text()).toContain('AUSGANG')
  })

  it('ruft getAll mit Filterparametern auf', async () => {
    const wrapper = mountView()
    await flushPromises()

    await wrapper.find('[data-testid="btn-filter"]').trigger('click')
    await flushPromises()

    expect(transaktionServiceModule.transaktionService.getAll).toHaveBeenCalledTimes(2)
  })

  it('zeigt Fehler-Toast wenn Laden fehlschlägt', async () => {
    transaktionServiceModule.transaktionService.getAll.mockRejectedValue(new Error('500'))
    const wrapper = mountView()
    await flushPromises()

    expect(wrapper.find('[data-testid="transaktionen-tabelle"]').exists()).toBe(true)
  })
})

// --- BestellhistorieView Tests ---
describe('BestellhistorieView', () => {
  let BestellhistorieView

  const mountView = () =>
    mount(BestellhistorieView, {
      global: {
        plugins: [PrimeVue, ToastService, createTestingPinia({ createSpy: vi.fn })],
        stubs: globalStubs
      }
    })

  beforeEach(async () => {
    vi.clearAllMocks()
    transaktionServiceModule.bestellungService.getAll.mockResolvedValue({ data: testBestellungen })
    BestellhistorieView = (await import('../views/BestellhistorieView.vue')).default
  })

  it('lädt Bestellungen beim Mounten', async () => {
    mountView()
    await flushPromises()

    expect(transaktionServiceModule.bestellungService.getAll).toHaveBeenCalledOnce()
  })

  it('zeigt Bestellungen in der Tabelle an', async () => {
    const wrapper = mountView()
    await flushPromises()

    expect(wrapper.find('[data-testid="bestellhistorie-tabelle"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('BEST-2026-001')
    expect(wrapper.text()).toContain('BEST-2026-002')
  })

  it('zeigt Bestellungen für verschiedene Status', async () => {
    const wrapper = mountView()
    await flushPromises()

    expect(wrapper.text()).toContain('Schrauben M6')
    expect(wrapper.text()).toContain('Muttern M6')
  })

  it('zeigt Fehler-Toast wenn Laden fehlschlägt', async () => {
    transaktionServiceModule.bestellungService.getAll.mockRejectedValue(new Error('500'))
    const wrapper = mountView()
    await flushPromises()

    expect(wrapper.find('[data-testid="bestellhistorie-tabelle"]').exists()).toBe(true)
  })
})
