<template>
  <div class="time-info-section">
    <h3>时间信息</h3>
    <div class="time-info-row">
      <div class="form-group">
        <label class="form-label">创建时间:</label>
        <div class="form-value">{{ formatDate(record.createdTime) }}</div>
      </div>
      
      <div class="form-group">
        <label class="form-label">更新时间:</label>
        <div class="form-value">{{ formatDate(record.updateTime) }}</div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TimeInfo',
  props: {
    record: {
      type: Object,
      required: true
    }
  },
  methods: {
    formatDate(dateString) {
      if (!dateString) return '-'
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.time-info-section {
  margin-bottom: 20px;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #fafafa;
}

.time-info-section h3 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #303133;
  border-left: 4px solid #007bff;
  padding-left: 10px;
}

.time-info-row {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.form-group {
  display: flex;
  min-width: 200px;
  flex: 1;
}

.form-label {
  width: 80px;
  font-weight: bold;
  margin-right: 10px;
  flex-shrink: 0;
  color: #606266;
}

.form-value {
  flex: 1;
  color: #303133;
}

@media (max-width: 768px) {
  .time-info-row {
    flex-direction: column;
  }
  
  .form-group {
    flex-direction: column;
  }
  
  .form-label {
    width: auto;
    margin-bottom: 5px;
  }
}
</style>