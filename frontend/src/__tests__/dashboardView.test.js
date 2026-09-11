import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import PrimeVue from 'primevue/config'
import ToastService from 'primevue/toastservice'
import ConfirmationService from 'primevue/confirmationservice'
import DashboardView from '../views/DashboardView.vue'
import * as dashboardServiceModule from '../api/services/dashboardService'

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
  useRoute: () => ({}),
  RouterLink: { template: '<a><slot /></a>' },
}))

vi.mock('../api/services/dashboardService', () => ({
  dashboardService: {
    getKpis: vi.fn(),
    getVorschlaege: vi.fn(),
    triggerReorder: vi.fn(),
    schnellbestellen: vi.fn(),
    bestellungAufgeben: vi.fn(),
    ignorieren: vi.fn(),
  }
}))

vi.mock('../api/services/lieferantenService', () => ({
  lieferantenService: {
    getAll: vi.fn().mockResolvedValue({ data: [] }),
  }
}))

const testKpis = { offeneVorschlaege: 3, kritischeVorschlaege: 1, letzterReorderLauf: '2026-04-20T02:00:00' }
const testVorschlaege = [
  {
    id: 1, artikelId: 10, artikelnummer: 'ART-001', artikelBezeichnung: 'Schrauben M6',
    lieferantName: 'Müller GmbH', aktuellerBestand: 5, bestellpunkt: 20,
    sicherheitsbestand: 10, vorgeschlageneMenge: 100, status: 'VORSCHLAG', istKritisch: true
  },
  {
    id: 2, artikelId: 11, artikelnummer: 'ART-002', artikelBezeichnung: 'Muttern M6',
    lieferantName: 'Müller GmbH', aktuellerBestand: 15, bestellpunkt: 20,
    sicherheitsbestand: 10, vorgeschlageneMenge: 50, status: 'VORSCHLAG', istKritisch: false
  },
]

// Kind-Komponenten stubben um PrimeVue-Renderprobleme im Testkontext zu umgehen
const globalStubs = {
  KpiSummary: {
    template: `
      <div>
        <span data-testid="kpi-offen">{{ kpis.offeneVorschlaege }}</span>
        <span data-testid="kpi-kritisch">{{ kpis.kritischeVorschlaege }}</span>
        <span data-testid="kpi-letzter-lauf">{{ kpis.letzterReorderLauf }}</span>
      </div>
    `,
    props: ['kpis', 'loading']
  },
  BestellvorschlagTabelle: {
    template: `
      <div data-testid="vorschlag-tabelle">
        <div v-for="v in vorschlaege" :key="v.id">{{ v.artikelBezeichnung }}</div>
      </div>
    `,
    props: ['vorschlaege', 'loading'],
    emits: ['refresh']
  },
  Button: {
    template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>',
    inheritAttrs: true,
    emits: ['click']
  },
  Card: { template: '<div class="p-card" @click="$emit(\'click\')"><slot name="content" /></div>', emits: ['click'] },
  Skeleton: { template: '<span />' },
}

const mountDashboard = () =>
  mount(DashboardView, {
    global: {
      plugins: [
        PrimeVue,
        ToastService,
        ConfirmationService,
        createTestingPinia({ createSpy: vi.fn })
      ],
      stubs: globalStubs
    }
  })

describe('DashboardView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    dashboardServiceModule.dashboardService.getKpis.mockResolvedValue({ data: testKpis })
    dashboardServiceModule.dashboardService.getVorschlaege.mockResolvedValue({ data: testVorschlaege })
  })

  it('lädt KPIs und Vorschläge beim Mounten', async () => {
    mountDashboard()
    await flushPromises()

    expect(dashboardServiceModule.dashboardService.getKpis).toHaveBeenCalledOnce()
    expect(dashboardServiceModule.dashboardService.getVorschlaege).toHaveBeenCalledOnce()
  })

  it('zeigt korrekte KPI-Werte an', async () => {
    const wrapper = mountDashboard()
    await flushPromises()

    expect(wrapper.find('[data-testid="kpi-offen"]').text()).toBe('3')
    expect(wrapper.find('[data-testid="kpi-kritisch"]').text()).toBe('1')
  })

  it('zeigt die Vorschlagstabelle mit Daten', async () => {
    const wrapper = mountDashboard()
    await flushPromises()

    expect(wrapper.find('[data-testid="vorschlag-tabelle"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Schrauben M6')
    expect(wrapper.text()).toContain('Muttern M6')
  })

  it('zeigt Reorder-Trigger-Button an', async () => {
    const wrapper = mountDashboard()
    await flushPromises()

    expect(wrapper.find('[data-testid="btn-reorder-trigger"]').exists()).toBe(true)
  })

  it('ruft triggerReorder auf und lädt danach KPIs neu', async () => {
    dashboardServiceModule.dashboardService.triggerReorder.mockResolvedValue({
      data: { message: 'Analyse abgeschlossen', neueVorschlaege: 2 }
    })
    const wrapper = mountDashboard()
    await flushPromises()

    await wrapper.find('[data-testid="btn-reorder-trigger"]').trigger('click')
    await flushPromises()

    expect(dashboardServiceModule.dashboardService.triggerReorder).toHaveBeenCalledOnce()
    expect(dashboardServiceModule.dashboardService.getKpis).toHaveBeenCalledTimes(2)
  })

  it('behält KPI-Initialwerte bei Server-Fehler', async () => {
    dashboardServiceModule.dashboardService.getKpis.mockRejectedValue(new Error('500'))
    const wrapper = mountDashboard()
    await flushPromises()

    expect(wrapper.find('[data-testid="kpi-offen"]').text()).toBe('0')
  })
})
