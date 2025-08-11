<!-- src/components/material/MaterialItem.vue -->
<template>
  <div class="material-item">
    <div class="material-header">
      <h3>{{ material.materialDetails || '未命名材料' }}</h3>
      <span class="material-id">ID: {{ material.id }}</span>
    </div>
    <div class="material-details">
      <p><strong>审核点:</strong> {{ material.reviewPoints || '无' }}</p>
      <p><strong>自动审批标准:</strong> {{ material.autoApprovalCriteria || '无' }}</p>
      <p><strong>共享:</strong> {{ material.isShared ? '是' : '否' }}</p>
      <p><strong>承诺资格:</strong> {{ material.isEligibleForPromise ? '是' : '否' }}</p>
      <p><strong>材料来源:</strong> {{ material.materialSource || '未指定' }}</p>
      <p><strong>处理方式:</strong> {{ material.processingMethodAndInfoAccess || '未指定' }}</p>
      <p><strong>承诺资格:</strong> {{ material.isEligibleForPromise ? '是' : '否' }}</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MaterialItem',
  props: {
    material: {
      type: Object,
      required: true
    }
  }
}
</script>

<style scoped>
.material-item {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 15px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.material-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.material-header h3 {
  margin: 0;
  color: #303133;
}

.material-id {
  color: #909399;
  font-size: 14px;
}

.material-details p {
  margin: 5px 0;
  color: #606266;
}
</style>
