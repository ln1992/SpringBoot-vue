<template>
  <div class="clothing-monthly-chart">
    <div class="chart-header">
      <h3>月度出入库统计图表</h3>
      <div class="date-range-selector">
        <el-date-picker
          v-model="startDate"
          type="date"
          placeholder="开始日期"
          value-format="yyyy-MM-dd"
          style="width: 150px;">
        </el-date-picker>
        <el-date-picker
          v-model="endDate"
          type="date"
          placeholder="结束日期"
          value-format="yyyy-MM-dd"
          style="width: 150px;">
        </el-date-picker>
        <el-button type="primary" icon="el-icon-refresh" @click="loadChartData">刷新</el-button>
      </div>
    </div>
    
    <div v-loading="loading" class="chart-container">
      <div ref="chartContainer" class="chart-wrapper"></div>
      
      <div v-if="!loading && (!chartData || chartData.length === 0)" class="no-data">
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
  name: 'ClothingMonthlyChart',
  props: {
    clothingName: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      startDate: moment().startOf('year').format('YYYY-MM-DD'), // 默认今年1月1日
      endDate: moment().format('YYYY-MM-DD'), // 默认今天
      chart: null,
      chartData: [],
      loading: false
    };
  },
  mounted() {
    // 使用ResizeObserver来检测容器尺寸变化
    this.$nextTick(() => {
      if (this.$refs.chartContainer) {
        // 如果浏览器支持ResizeObserver
        if (window.ResizeObserver) {
          this.resizeObserver = new ResizeObserver(entries => {
            for (let entry of entries) {
              // 当容器尺寸变化时调整图表
              if (this.chart && entry.contentRect.width > 0 && entry.contentRect.height > 0) {
                this.chart.resize();
              }
            }
          });
          this.resizeObserver.observe(this.$refs.chartContainer);
        }
        
        // 初始化图表
        setTimeout(() => {
          this.initChart();
          this.loadChartData();
        }, 100);
      }
    });
    
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    if (this.chart) {
      this.chart.dispose();
    }
    
    // 清理ResizeObserver
    if (this.resizeObserver) {
      this.resizeObserver.disconnect();
    }
    
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    initChart() {
      if (this.$refs.chartContainer) {
        this.chart = echarts.init(this.$refs.chartContainer, null, {
          renderer: 'canvas'
        });
        
        // 等待图表渲染后再更新数据
        this.$nextTick(() => {
          this.updateChart();
          
          // 立即调整图表大小
          this.chart.resize();
          
          // 再次调整以确保正确尺寸
          setTimeout(() => {
            if (this.chart) {
              this.chart.resize();
            }
          }, 100);
          
          // 再次确认尺寸
          setTimeout(() => {
            if (this.chart) {
              this.chart.resize();
            }
          }, 300);
        });
      }
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
        
        if (this.startDate) {
          startDate = moment(this.startDate, 'YYYY-MM-DD').startOf('day').toISOString();
        }
        
        if (this.endDate) {
          endDate = moment(this.endDate, 'YYYY-MM-DD').endOf('day').toISOString();
        }
        
        const response = await clothingReportService.getMonthlyStatsReport(
          this.clothingName, startDate, endDate);
        
        if (response && response.data) {
          this.chartData = response.data;
        } else {
          this.chartData = [];
        }
        
        this.updateChart();
      } catch (error) {
        console.error('加载图表数据失败:', error);
        this.$message.error('加载图表数据失败: ' + (error.message || '未知错误'));
        this.chartData = [];
        this.updateChart();
      } finally {
        this.loading = false;
        // 确保数据加载后图表大小正确
        this.$nextTick(() => {
          if (this.chart) {
            this.chart.resize();
          }
        });
      }
    },
    
    updateChart() {
      if (!this.chart) return;
      
      // 处理图表数据
      const months = this.chartData.map(item => item.month);
      const inboundData = this.chartData.map(item => item.inbound || 0);
      const outboundData = this.chartData.map(item => item.outbound || 0);
      
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
          data: months
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '入库数量',
            type: 'bar',
            color: '#67c23a',
            data: inboundData
          },
          {
            name: '出库数量',
            type: 'bar',
            color: '#f56c6c',
            data: outboundData
          }
        ]
      };
      
      this.chart.setOption(option);
      
      // 确保图表调整到容器大小
      this.$nextTick(() => {
        if (this.chart) {
          this.chart.resize();
        }
      });
      
      // 立即调整大小
      setTimeout(() => {
        if (this.chart) {
          this.chart.resize();
        }
      }, 50);
    }
  }
};
</script>

<style scoped>
.clothing-monthly-chart {
  margin-top: 20px;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #fff;
  width: 100%;
  box-sizing: border-box;
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
  min-height: 500px;
  position: relative;
  width: 100%;
}

.chart-wrapper {
  width: 100%;
  height: 500px;
  min-height: 400px;
  margin: 0;
  padding: 0;
  overflow: hidden;
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