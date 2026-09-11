import { createApp } from 'vue'
import { createPinia } from 'pinia'
import PrimeVue from 'primevue/config'
import Aura from '@primevue/themes/aura'
import ToastService from 'primevue/toastservice'
import ConfirmationService from 'primevue/confirmationservice'
import 'primeicons/primeicons.css'

import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Message from 'primevue/message'
import Toast from 'primevue/toast'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Tag from 'primevue/tag'
import Card from 'primevue/card'
import Dialog from 'primevue/dialog'
import InputNumber from 'primevue/inputnumber'
import ConfirmDialog from 'primevue/confirmdialog'
import Tabs from 'primevue/tabs'
import TabList from 'primevue/tablist'
import Tab from 'primevue/tab'
import TabPanels from 'primevue/tabpanels'
import TabPanel from 'primevue/tabpanel'
import Select from 'primevue/select'
import DatePicker from 'primevue/datepicker'
import Skeleton from 'primevue/skeleton'
import Textarea from 'primevue/textarea'

import router from './router'
import App from './App.vue'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(PrimeVue, { theme: { preset: Aura } })
app.use(ToastService)
app.use(ConfirmationService)

app.component('Button', Button)
app.component('InputText', InputText)
app.component('Password', Password)
app.component('Message', Message)
app.component('Toast', Toast)
app.component('DataTable', DataTable)
app.component('Column', Column)
app.component('Tag', Tag)
app.component('Card', Card)
app.component('Dialog', Dialog)
app.component('InputNumber', InputNumber)
app.component('ConfirmDialog', ConfirmDialog)
app.component('Tabs', Tabs)
app.component('TabList', TabList)
app.component('Tab', Tab)
app.component('TabPanels', TabPanels)
app.component('TabPanel', TabPanel)
app.component('Select', Select)
app.component('DatePicker', DatePicker)
app.component('Skeleton', Skeleton)
app.component('Textarea', Textarea)

app.mount('#app')
