<!-- src/components/material/sections/SourceAndSharing.vue -->
<template>
  <div class="source-sharing-section">
    <!-- 是否共享和材料来源放在同一行 -->
    <div class="form-row">
      <div class="form-group">
        <label>是否共享:</label>
        <select
          :value="form.shared"
          @change="$emit('update-field', 'shared', $event.target.value === 'true')">
          <option :value="true">是</option>
          <option :value="false">否</option>
        </select>
      </div>

      <div class="form-group">
        <label>材料来源:</label>
        <select
          :value="form.materialSource"
          @change="$emit('update-field', 'materialSource', $event.target.value)">
          <option value="PERSONAL_SUBMISSION">申请人自备</option>
          <option value="SYSTEM_AUTO_SHARED">系统自动获取</option>
          <option value="WANG_SHAN_PROCESSING">网上办理</option>
        </select>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SourceAndSharing',
  props: {
    form: {
      type: Object,
      required: true
    }
  }
}
</script>

<style scoped>
.form-row {
  display: flex;
  gap: 15px;
}

.form-row .form-group {
  flex: 1;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #303133;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

@media (max-width: 768px) {
  .form-row {
    flex-direction: column;
    gap: 0;
  }
}
</style>
