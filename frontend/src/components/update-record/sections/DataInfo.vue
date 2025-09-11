<template>
  <div class="data-info-section">
    <h3>数据变更信息</h3>
    <div class="data-comparison">
      <div class="data-column">
        <h4>变更前数据</h4>
        <div class="form-value">
          <pre v-if="formattedBeforeData">{{ formattedBeforeData }}</pre>
          <span v-else>-</span>
        </div>
      </div>
      
      <div class="data-column">
        <h4>变更后数据</h4>
        <div class="form-value">
          <pre v-if="formattedAfterData">{{ formattedAfterData }}</pre>
          <span v-else>-</span>
        </div>
      </div>
    </div>
    
  </div>
</template>

<script>
export default {
  name: 'DataInfo',
  props: {
    record: {
      type: Object,
      required: true
    }
  },
  computed: {
    
    formattedBeforeData() {
      if (!this.record.beforeData) return null;
      
      try {
        // 如果是字符串，尝试解析为JSON
        if (typeof this.record.beforeData === 'string') {
          const parsed = JSON.parse(this.record.beforeData);
          return JSON.stringify(parsed, null, 2);
        }
        // 如果已经是对象，直接格式化
        return JSON.stringify(this.record.beforeData, null, 2);
      } catch (e) {
        // 如果解析失败，返回原始数据
        return this.record.beforeData;
      }
    },
    
    formattedAfterData() {
      if (!this.record.afterData) return null;
      
      try {
        // 如果是字符串，尝试解析为JSON
        if (typeof this.record.afterData === 'string') {
          const parsed = JSON.parse(this.record.afterData);
          return JSON.stringify(parsed, null, 2);
        }
        // 如果已经是对象，直接格式化
        return JSON.stringify(this.record.afterData, null, 2);
      } catch (e) {
        // 如果解析失败，返回原始数据
        return this.record.afterData;
      }
    }
  }
}
</script>

<style scoped>
.data-info-section {
  margin-bottom: 20px;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #fafafa;
}

.data-info-section h3 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #303133;
  border-left: 4px solid #007bff;
  padding-left: 10px;
}

.data-comparison {
  display: flex;
  gap: 20px;
}

.data-column {
  flex: 1;
}

.data-column h4 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #606266;
  font-size: 14px;
}

.form-value {
  flex: 1;
  color: #303133;
}

.form-value pre {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
  margin: 0;
  color: #606266;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.test-data-section {
  margin-top: 20px;
  padding: 10px;
  background-color: #e8f4f8;
  border-left: 4px solid #007bff;
  border-radius: 4px;
}

@media (max-width: 768px) {
  .data-comparison {
    flex-direction: column;
  }
}
</style>