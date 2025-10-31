<template>
  <div class="inventory-summary-report">
    <el-card class="report-card">
      <div slot="header" class="clearfix">
        <span>库存汇总报表</span>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          @change="loadData"
          style="float: right; width: 300px;">
        </el-date-picker>
      </div>

      <!-- 概要统计 -->
      <el-row :gutter="20" class="summary-stats">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-label">品牌数量</div>
              <div class="stat-value">{{ brandCount }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-label">服装数量</div>
              <div class="stat-value">{{ summary.totalItems }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-label">服装总量（件）</div>
              <div class="stat-value">{{ summary.activeItems }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-label">库存总数</div>
              <div class="stat-value">{{ summary.totalStock }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 按品牌统计 -->
      <el-table :data="brandStats" style="width: 100%; margin-top: 20px;" border>
        <el-table-column prop="brand" label="品牌" min-width="150"></el-table-column>
        <el-table-column prop="itemCount" label="商品数量" min-width="120"></el-table-column>
        <el-table-column prop="totalStock" label="库存总量" min-width="120"></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import clothingReportService from '@/api/clothingReportService'
import moment from 'moment'

export default {
  name: 'InventorySummaryReport',
  data() {
    return {
      dateRange: null, // 默认不设置时间范围
      summary: {
        totalItems: 0,
        activeItems: 0,
        totalStock: 0
      },
      brandStats: []
    }
  },
  computed: {
    brandCount() {
      return this.brandStats.length;
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      try {
        this.$emit('loading', true)

        // 传递参数，如果没有选择时间范围则传递null
        let params = {};
        if (this.dateRange && this.dateRange.length === 2) {
          params.startDate = moment(this.dateRange[0], 'YYYY-MM-DD').startOf('day').toISOString()
          params.endDate = moment(this.dateRange[1], 'YYYY-MM-DD').endOf('day').toISOString()
        } else {
          // 即使没有时间范围，也要传递null值
          params.startDate = null;
          params.endDate = null;
        }

        const response = await clothingReportService.getInventorySummary(params)
        // 确保返回的数据是数组格式
        if (response && response.data) {
          // 处理实际返回的数据格式
          if (response.data.summary) {
            this.summary = response.data.summary
          }
          if (response.data.byBrand) {
            this.brandStats = Array.isArray(response.data.byBrand) ? response.data.byBrand : []
          }
        } else if (response && response.summary) {
          // 如果响应数据直接在顶层
          this.summary = response.summary
          this.brandStats = Array.isArray(response.byBrand) ? response.byBrand : []
        } else {
          // 如果响应格式不正确，设置默认值
          this.summary = {
            totalItems: 0,
            activeItems: 0,
            totalStock: 0
          }
          this.brandStats = []
        }
      } catch (error) {
        console.error('加载库存汇总报表失败:', error)
        this.$message.error('加载库存汇总报表失败: ' + (error.message || '未知错误'))
        // 出错时设置默认值
        this.summary = {
          totalItems: 0,
          activeItems: 0,
          totalStock: 0
        }
        this.brandStats = []
      } finally {
        this.$emit('loading', false)
      }
    }
  }
}
</script>

<style scoped>
.inventory-summary-report {
  padding: 20px 0;
}

.report-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.summary-stats {
  margin-top: 20px;
}

.stat-card {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 16px;
  color: #606266;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}
</style>