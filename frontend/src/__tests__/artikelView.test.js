import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import PrimeVue from 'primevue/config'
import ToastService from 'primevue/toastservice'
import ConfirmationService from 'primevue/confirmationservice'
import ArtikelView from '../views/ArtikelView.vue'
import * as artikelServiceModule from '../api/services/artikelService'

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
  useRoute: () => ({}),
}))

vi.mock('../api/services/artikelService', () => ({
  artikelService: {
    getAll: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    deaktivieren: vi.fn(),
  }
}))

vi.mock('../api/services/lieferantenService', () => ({
  lieferantenService: {
    getAll: vi.fn().mockResolvedValue({ data: [{ id: 1, name: 'Müller GmbH' }] }),
  }
}))

const testArtikel = [
  {
    id: 1, artikelnummer: 'ART-001', bezeichnung: 'Schrauben M6',
    mengeneinheit: 'Stück', warengruppe: 'Befestigung',
    lieferantId: 1, lieferantName: 'Müller GmbH',
    aktuellerBestand: 50, bestellpunkt: 20, sicherheitsbestand: 10,
    standardBestellmenge: 100, einkaufspreis: 0.05, status: 'AKTIV'
  },
  {
    id: 2, artikelnummer: 'ART-002', bezeichnung: 'Muttern M6',
    mengeneinheit: 'Stück', warengruppe: 'Befestigung',
    lieferantId: 1, lieferantName: 'Müller GmbH',
    aktuellerBestand: 0, bestellpunkt: 10, sicherheitsbestand: 5,
    standardBestellmenge: 50, einkaufspreis: 0.03, status: 'INAKTIV'
  }
]

const globalStubs = {
  ArtikelFormular: {
    template: '<div data-testid="artikel-formular" />',
    props: ['visible', 'artikel'],
    emits: ['update:visible', 'gespeichert']
  },
  ConfirmDialog: { template: '<div />' },
  DataTable: {
    template: `
      <div data-testid="artikel-tabelle">
        <div v-for="row in value" :key="row.id">
          <span>{{ row.bezeichnung }}</span>
          <button data-testid="btn-bearbeiten" @click="$emit('bearbeiten', row)">Bearbeiten</button>
          <button v-if="row.status === 'AKTIV'" data-testid="btn-deaktivieren" @click="$emit('deaktivieren', row)">Deaktivieren</button>
        </div>
      </div>
    `,
    props: ['value', 'loading'],
    emits: ['bearbeiten', 'deaktivieren']
  },
  Column: { template: '<div />' },
  Button: {
    template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>',
    inheritAttrs: true, emits: ['click']
  },
  Tag: { template: '<span v-bind="$attrs"><slot /></span>' },
}

const mountArtikelView = () =>
  mount(ArtikelView, {
    global: {
      plugins: [PrimeVue, ToastService, ConfirmationService, createTestingPinia({ createSpy: vi.fn })],
      stubs: globalStubs
    }
  })

describe('ArtikelView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    artikelServiceModule.artikelService.getAll.mockResolvedValue({ data: testArtikel })
  })

  it('lädt Artikelliste beim Mounten', async () => {
    mountArtikelView()
    await flushPromises()

    expect(artikelServiceModule.artikelService.getAll).toHaveBeenCalledOnce()
  })

  it('zeigt Artikel in der Tabelle an', async () => {
    const wrapper = mountArtikelView()
    await flushPromises()

    expect(wrapper.find('[data-testid="artikel-tabelle"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Schrauben M6')
    expect(wrapper.text()).toContain('Muttern M6')
  })

  it('öffnet das Formular beim Klick auf "Neuen Artikel anlegen"', async () => {
    const wrapper = mountArtikelView()
    await flushPromises()

    await wrapper.find('[data-testid="btn-neu-anlegen"]').trigger('click')

    expect(wrapper.find('[data-testid="artikel-formular"]').exists()).toBe(true)
  })

  it('zeigt Fehler-Toast wenn Laden fehlschlägt', async () => {
    artikelServiceModule.artikelService.getAll.mockRejectedValue(new Error('500'))
    const wrapper = mountArtikelView()
    await flushPromises()

    expect(wrapper.find('[data-testid="artikel-tabelle"]').exists()).toBe(true)
  })
})

describe('ArtikelFormular — 409-Duplikatfehler', () => {
  it('zeigt Inline-Fehlermeldung bei 409-Response', async () => {
    const { mount: mountComp } = await import('@vue/test-utils')
    const ArtikelFormular = (await import('../components/ArtikelFormular.vue')).default

    artikelServiceModule.artikelService.create.mockRejectedValue({ response: { status: 409 } })
    vi.mocked(await import('../api/services/lieferantenService')).lieferantenService.getAll
      .mockResolvedValue({ data: [{ id: 1, name: 'Test GmbH' }] })

    const primeStubs = {
      InputText: {
        props: ['modelValue'], emits: ['update:modelValue'], inheritAttrs: false,
        template: '<input :value="modelValue" v-bind="$attrs" @input="$emit(\'update:modelValue\', $event.target.value)" />'
      },
      InputNumber: {
        props: ['modelValue'], emits: ['update:modelValue'], inheritAttrs: false,
        template: '<input type="number" :value="modelValue" v-bind="$attrs" @input="$emit(\'update:modelValue\', Number($event.target.value))" />'
      },
      Select: {
        props: ['modelValue', 'options', 'optionLabel', 'optionValue'],
        emits: ['update:modelValue'], inheritAttrs: false,
        template: '<select v-bind="$attrs" @change="$emit(\'update:modelValue\', Number($event.target.value))"><option v-for="o in options" :key="o.id" :value="o.id">{{ o.name }}</option></select>'
      },
      Button: {
        template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>',
        inheritAttrs: true, emits: ['click']
      },
      Message: { template: '<div data-testid="duplikat-fehler"><slot /></div>' },
      Dialog: { template: '<div><slot /><slot name="footer" /></div>' },
    }

    const wrapper = mountComp(ArtikelFormular, {
      props: { visible: true, artikel: null },
      global: {
        plugins: [PrimeVue, ToastService, createTestingPinia({ createSpy: vi.fn })],
        stubs: primeStubs
      }
    })

    await flushPromises()

    await wrapper.find('[data-testid="input-artikelnummer"]').setValue('ART-001')
    await wrapper.find('[data-testid="input-bezeichnung"]').setValue('Schrauben')
    await wrapper.find('[data-testid="input-mengeneinheit"]').setValue('Stück')
    await wrapper.find('[data-testid="input-warengruppe"]').setValue('Gruppe')
    await wrapper.find('select').setValue('1')
    await wrapper.find('[data-testid="btn-speichern"]').trigger('click')
    await flushPromises()

    expect(wrapper.find('[data-testid="duplikat-fehler"]').exists()).toBe(true)
  })
})
