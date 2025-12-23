<template>
  <div class="clothing-chart">
    <div class="chart-header">
      <h3>出入库统计图表</h3>
      <div class="date-range-selector">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          @change="loadChartData"
          style="width: 300px;">
        </el-date-picker>
        <el-button type="primary" icon="el-icon-refresh" @click="loadChartData">刷新</el-button>
      </div>
    </div>
    
    <div v-loading="loading" class="chart-container">
      <div ref="chartContainer" class="chart-wrapper"></div>
      
      <div v-if="!loading && chartData && chartData.length === 0" class="no-data">
        <el-alert title="在选定的时间范围内没有找到出入库记录" type="warning" show-icon :closable="false"></el-alert>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import clothingReportService from '@/api/clothingReportService';
import moment from 'moment';

export default {
  name: 'ClothingChart',
  props: {
    clothingId: {
      type: Number,
      required: true
    },
    clothingName: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      dateRange: null,
      chart: null,
      chartData: [],
      loading: false
    };
  },
  mounted() {
    this.initChart();
    this.loadChartData();
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    if (this.chart) {
      this.chart.dispose();
    }
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    initChart() {
      this.chart = echarts.init(this.$refs.chartContainer);
      this.updateChart();
    },
    
    handleResize() {
      if (this.chart) {
        this.chart.resize();
      }
    },
    
    async loadChartData() {
      this.loading = true;
      
      try {
        let startDate = null;
        let endDate = null;
        
        if (this.dateRange && this.dateRange.length === 2) {
          startDate = moment(this.dateRange[0], 'YYYY-MM-DD').startOf('day').toISOString();
          endDate = moment(this.dateRange[1], 'YYYY-MM-DD').endOf('day').toISOString();
        }
        
        const response = await clothingReportService.getInboundOutboundReport(startDate, endDate);
        
        if (response && response.data && response.data.details) {
          // 筛选出当前服装的数据
          const clothingData = response.data.details.find(
            item => item.clothingName === this.clothingName
          );
          
          if (clothingData) {
            this.chartData = [
              {
                name: '入库数量',
                value: clothingData.inboundQuantity || 0
              },
              {
                name: '出库数量',
                value: clothingData.outboundQuantity || 0
              }
            ];
          } else {
            this.chartData = [
              { name: '入库数量', value: 0 },
              { name: '出库数量', value: 0 }
            ];
          }
        } else {
          this.chartData = [
            { name: '入库数量', value: 0 },
            { name: '出库数量', value: 0 }
          ];
        }
        
        this.updateChart();
      } catch (error) {
        console.error('加载图表数据失败:', error);
        this.$message.error('加载图表数据失败: ' + (error.message || '未知错误'));
        this.chartData = [
          { name: '入库数量', value: 0 },
          { name: '出库数量', value: 0 }
        ];
        this.updateChart();
      } finally {
        this.loading = false;
      }
    },
    
    updateChart() {
      if (!this.chart) return;
      
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        legend: {
          data: ['入库数量', '出库数量']
        },
        xAxis: {
          type: 'category',
          data: ['']
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '入库数量',
            type: 'bar',
            stack: '总量',
            color: '#67c23a',
            data: [this.chartData.find(item => item.name === '入库数量')?.value || 0]
          },
          {
            name: '出库数量',
            type: 'bar',
            stack: '总量',
            color: '#f56c6c',
            data: [this.chartData.find(item => item.name === '出库数量')?.value || 0]
          }
        ]
      };
      
      this.chart.setOption(option);
    }
  }
};
</script>

<style scoped>
.clothing-chart {
  margin-top: 20px;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #fff;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}

.chart-header h3 {
  margin: 0;
}

.date-range-selector {
  display: flex;
  gap: 10px;
  align-items: center;
}

.chart-container {
  min-height: 300px;
  position: relative;
}

.chart-wrapper {
  width: 100%;
  height: 300px;
}

.no-data {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  text-align: center;
}
</style>