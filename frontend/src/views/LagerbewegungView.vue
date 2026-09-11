<script setup>
import WareineingangFormular from '@/components/WareineingangFormular.vue'
import WarenausgangFormular from '@/components/WarenausgangFormular.vue'

const tabs = [
  { label: 'Wareneingang', value: 'eingang' },
  { label: 'Warenausgang', value: 'ausgang' },
]
</script>

<template>
  <div class="lagerbewegung-seite">
    <h1 class="lagerbewegung-seite__title">Lagerbewegung</h1>

    <Tabs value="eingang">
      <TabList>
        <Tab
          v-for="tab in tabs"
          :key="tab.value"
          :value="tab.value"
        >
          {{ tab.label }}
        </Tab>
      </TabList>
      <TabPanels>
        <TabPanel value="eingang">
          <ConfirmDialog />
          <WareineingangFormular data-testid="eingang-formular" />
        </TabPanel>
        <TabPanel value="ausgang">
          <WarenausgangFormular data-testid="ausgang-formular" />
        </TabPanel>
      </TabPanels>
    </Tabs>
  </div>
</template>

<style scoped>
.lagerbewegung-seite {
  padding: 1.5rem;
  max-width: 900px;
  margin: 0 auto;
}

.lagerbewegung-seite__title {
  font-size: 1.4rem;
  font-weight: 700;
  margin: 0 0 1.5rem;
  color: var(--p-text-color, #1e293b);
}
</style>
