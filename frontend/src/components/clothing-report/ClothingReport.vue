<template>
  <div class="clothing-report">
    <div class="report-header">
      <h2>服装库存报表</h2>
      <div class="report-actions">
        <el-button type="primary" @click="refreshReports">刷新</el-button>
        <el-button @click="exportReport">导出</el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" type="card" @tab-click="handleTabClick">
      <el-tab-pane label="库存汇总" name="summary">
        <InventorySummaryReport ref="inventorySummary" />
      </el-tab-pane>
      <el-tab-pane label="低库存预警" name="lowStock">
        <LowStockReport ref="lowStock" />
      </el-tab-pane>
      <el-tab-pane label="出入库统计" name="inboundOutbound">
        <InboundOutboundReport ref="inboundOutbound" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import InventorySummaryReport from './InventorySummaryReport.vue'
import LowStockReport from './LowStockReport.vue'
import InboundOutboundReport from './InboundOutboundReport.vue'

export default {
  name: 'ClothingReport',
  components: {
    InventorySummaryReport,
    LowStockReport,
    InboundOutboundReport
  },
  data() {
    return {
      activeTab: 'summary'
    }
  },
  methods: {
    handleTabClick(tab) {
      console.log('切换到标签页:', tab.name)
    },
    refreshReports() {
      // 根据当前激活的标签页刷新对应报表
      switch (this.activeTab) {
        case 'summary':
          if (this.$refs.inventorySummary && typeof this.$refs.inventorySummary.loadData === 'function') {
            this.$refs.inventorySummary.loadData()
          }
          break
        case 'lowStock':
          if (this.$refs.lowStock && typeof this.$refs.lowStock.loadData === 'function') {
            this.$refs.lowStock.loadData()
          }
          break
        case 'inboundOutbound':
          if (this.$refs.inboundOutbound && typeof this.$refs.inboundOutbound.loadData === 'function') {
            this.$refs.inboundOutbound.loadData()
          }
          break
        default:
          // 刷新所有报表
          if (this.$refs.inventorySummary && typeof this.$refs.inventorySummary.loadData === 'function') {
            this.$refs.inventorySummary.loadData()
          }
          if (this.$refs.lowStock && typeof this.$refs.lowStock.loadData === 'function') {
            this.$refs.lowStock.loadData()
          }
          if (this.$refs.inboundOutbound && typeof this.$refs.inboundOutbound.loadData === 'function') {
            this.$refs.inboundOutbound.loadData()
          }
      }
    },
    exportReport() {
      this.$message.info('导出功能正在开发中')
    }
  }
}
</script>

<style scoped>
.clothing-report {
  padding: 20px;
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.report-header h2 {
  margin: 0;
}

.report-actions {
  display: flex;
  gap: 10px;
}
</style>