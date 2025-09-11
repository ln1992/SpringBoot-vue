<template>
  <div class="operation-info-section">
    <h3>操作信息</h3>
    <div class="info-row">
      <div class="form-group">
        <label class="form-label">操作类型:</label>
        <div class="form-value">{{ getOperationTypeLabel(record.operationType) }}</div>
      </div>
      
      <div class="form-group">
        <label class="form-label">操作用户:</label>
        <div class="form-value">{{ record.operator }}</div>
      </div>
      
      <div class="form-group">
        <label class="form-label">操作描述:</label>
        <div class="form-value">{{ record.description || '-' }}</div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'OperationInfo',
  props: {
    record: {
      type: Object,
      required: true
    }
  },
  methods: {
    getOperationTypeLabel(operationType) {
      const labels = {
        'CREATE': '创建',
        'UPDATE': '更新',
        'DELETE': '删除'
      }
      return labels[operationType] || operationType
    }
  }
}
</script>

<style scoped>
.operation-info-section {
  margin-bottom: 20px;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #fafafa;
}

.operation-info-section h3 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #303133;
  border-left: 4px solid #007bff;
  padding-left: 10px;
}

.info-row {
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
  .info-row {
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