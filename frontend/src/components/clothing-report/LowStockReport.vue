<template>
  <div class="low-stock-report">
    <el-card class="report-card">
      <div slot="header" class="clearfix">
        <span>低库存预警报表</span>
        <el-button style="float: right;" type="primary" icon="el-icon-refresh" @click="loadData">刷新</el-button>
      </div>

      <el-alert
        v-if="lowStockItems.length === 0"
        title="暂无低库存商品"
        type="success"
        show-icon
        :closable="false">
      </el-alert>

      <el-table
        v-else
        :data="lowStockItems"
        style="width: 100%"
        border
        stripe>
        <el-table-column prop="name" label="商品名称" min-width="150"></el-table-column>
        <el-table-column prop="brand" label="品牌" min-width="120"></el-table-column>
        <el-table-column label="尺码-S" min-width="80">
          <template slot-scope="scope">
            {{ getCurrentStockBySize(scope.row, 'S') }}
          </template>
        </el-table-column>
        <el-table-column label="尺码-M" min-width="80">
          <template slot-scope="scope">
            {{ getCurrentStockBySize(scope.row, 'M') }}
          </template>
        </el-table-column>
        <el-table-column label="尺码-L" min-width="80">
          <template slot-scope="scope">
            {{ getCurrentStockBySize(scope.row, 'L') }}
          </template>
        </el-table-column>
        <el-table-column label="安全库存" min-width="100">
          <template slot-scope="scope">
            {{ scope.row.safetyStock || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="当前总库存" min-width="100">
          <template slot-scope="scope">
            {{ getCurrentStockTotal(scope.row) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="120">
          <template slot-scope="scope">
            <el-tag :type="getStockStatusType(scope.row)">
              {{ getStockStatusText(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import clothingReportService from '@/api/clothingReportService'

export default {
  name: 'LowStockReport',
  data() {
    return {
      lowStockItems: []
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      try {
        this.$emit('loading', true)
        const response = await clothingReportService.getLowStockReport()
        // 确保返回的数据是数组格式
        if (response && response.data) {
          // 处理实际返回的数据格式
          this.lowStockItems = Array.isArray(response.data) ? response.data : []
        } else if (Array.isArray(response)) {
          // 如果响应数据直接是数组
          this.lowStockItems = response
        } else {
          this.lowStockItems = []
        }
      } catch (error) {
        console.error('加载低库存报表失败:', error)
        this.$message.error('加载低库存报表失败: ' + (error.message || '未知错误'))
        this.lowStockItems = []
      } finally {
        this.$emit('loading', false)
      }
    },

    getCurrentStockBySize(item, size) {
      if (!item || !item.currentStockBySize) return 0
      // 处理后端返回的Map格式数据
      if (item.currentStockBySize[size] !== undefined) {
        return item.currentStockBySize[size]
      }
      return 0
    },

    getCurrentStockTotal(item) {
      if (!item || !item.currentStockBySize) return 0
      // 计算所有尺寸的库存总和
      return Object.values(item.currentStockBySize).reduce((sum, stock) => sum + (stock || 0), 0)
    },

    getStockStatusType(item) {
      const totalStock = this.getCurrentStockTotal(item)
      const safetyStock = item.safetyStock || 0

      if (totalStock <= 0) {
        return 'danger' // 严重不足
      } else if (totalStock <= safetyStock * 0.5) {
        return 'warning' // 严重偏低
      } else if (totalStock <= safetyStock) {
        return 'info' // 低于安全库存
      } else {
        return 'success' // 正常
      }
    },

    getStockStatusText(item) {
      const totalStock = this.getCurrentStockTotal(item)
      const safetyStock = item.safetyStock || 0

      if (totalStock <= 0) {
        return '缺货'
      } else if (totalStock <= safetyStock * 0.5) {
        return '严重偏低'
      } else if (totalStock <= safetyStock) {
        return '低于安全库存'
      } else {
        return '正常'
      }
    }
  }
}
</script>

<style scoped>
.low-stock-report {
  padding: 20px 0;
}

.report-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.el-alert {
  margin-bottom: 20px;
}
</style>