<template>
  <div class="inbound-outbound-report">
    <el-card class="report-card">
      <div slot="header" class="clearfix">
        <span>出入库统计报表</span>
        <div style="float: right; display: flex; gap: 10px;">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
            @change="loadData"
            style="width: 300px;">
          </el-date-picker>
          <el-button type="primary" icon="el-icon-refresh" @click="loadData">刷新</el-button>
        </div>
      </div>

      <!-- 概要统计 -->
      <el-row :gutter="20" class="summary-stats">
        <el-col :span="8">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-label">总入库量</div>
              <div class="stat-value inbound">{{ summary.totalInbound }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-label">总出库量</div>
              <div class="stat-value outbound">{{ summary.totalOutbound }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-label">净变化量</div>
              <div class="stat-value" :class="{ 'positive': summary.netChange > 0, 'negative': summary.netChange < 0 }">
                {{ summary.netChange }}
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 明细表格 -->
      <el-table
        :data="details"
        style="width: 100%; margin-top: 20px;"
        border
        stripe>
        <el-table-column prop="clothingName" label="商品名称" min-width="150"></el-table-column>
        <el-table-column prop="inboundQuantity" label="入库数量" min-width="120">
          <template slot-scope="scope">
            <span class="inbound">{{ scope.row.inboundQuantity }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="outboundQuantity" label="出库数量" min-width="120">
          <template slot-scope="scope">
            <span class="outbound">{{ scope.row.outboundQuantity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="净变化量" min-width="120">
          <template slot-scope="scope">
            <span :class="{
              'positive': scope.row.inboundQuantity - scope.row.outboundQuantity > 0,
              'negative': scope.row.inboundQuantity - scope.row.outboundQuantity < 0
            }">
              {{ scope.row.inboundQuantity - scope.row.outboundQuantity }}
            </span>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 当没有数据时显示提示 -->
      <div v-if="details.length === 0 && hasLoaded" class="no-data-hint" style="margin-top: 20px;">
        <el-alert title="在选定的时间范围内没有找到出入库记录" type="warning" show-icon :closable="false"></el-alert>
      </div>
    </el-card>
  </div>
</template>

<script>
import clothingReportService from '@/api/clothingReportService'
import moment from 'moment'

export default {
  name: 'InboundOutboundReport',
  data() {
    return {
      dateRange: null, // 默认不设置时间范围
      summary: {
        totalInbound: 0,
        totalOutbound: 0,
        netChange: 0
      },
      details: [],
      hasLoaded: false
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.hasLoaded = true;
      
      try {
        this.$emit('loading', true)

        // 如果没有选择时间范围，传递null参数而不是不传递
        let startDate = null;
        let endDate = null;
        
        if (this.dateRange && this.dateRange.length === 2) {
          startDate = moment(this.dateRange[0], 'YYYY-MM-DD').startOf('day').toISOString()
          endDate = moment(this.dateRange[1], 'YYYY-MM-DD').endOf('day').toISOString()
        }
        
        const response = await clothingReportService.getInboundOutboundReport(startDate, endDate)
        // 确保返回的数据是正确的格式
        if (response && response.data) {
          // 处理实际返回的数据格式
          if (response.data.summary) {
            this.summary = response.data.summary
          }
          if (response.data.details) {
            this.details = Array.isArray(response.data.details) ? response.data.details : []
          }
        } else if (response && response.summary) {
          // 如果响应数据直接在顶层
          this.summary = response.summary
          this.details = Array.isArray(response.details) ? response.details : []
        } else {
          // 如果响应格式不正确，设置默认值
          this.summary = {
            totalInbound: 0,
            totalOutbound: 0,
            netChange: 0
          }
          this.details = []
        }
      } catch (error) {
        console.error('加载出入库统计报表失败:', error)
        this.$message.error('加载出入库统计报表失败: ' + (error.message || '未知错误'))
        // 出错时设置默认值
        this.summary = {
          totalInbound: 0,
          totalOutbound: 0,
          netChange: 0
        }
        this.details = []
      } finally {
        this.$emit('loading', false)
      }
    }
  }
}
</script>

<style scoped>
.inbound-outbound-report {
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

.stat-value.inbound {
  color: #67c23a;
}

.stat-value.outbound {
  color: #f56c6c;
}

.stat-value.positive {
  color: #67c23a;
}

.stat-value.negative {
  color: #f56c6c;
}

.inbound {
  color: #67c23a;
}

.outbound {
  color: #f56c6c;
}
</style>