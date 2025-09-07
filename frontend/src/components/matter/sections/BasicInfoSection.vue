<!-- src/components/matter/sections/BasicInfoSection.vue -->
<template>
  <div class="basic-info-section">
    <div class="form-row">
      <div class="form-group form-group-id">
        <label>ID:</label>
        <input type="text" :value="form.id" disabled>
      </div>

      <div class="form-group form-group-version">
        <label>版本:</label>
        <input type="text" :value="form.version" @input="updateForm('version', $event.target.value)">
      </div>

      <div class="form-group form-group-status">
        <label>发布状态:</label>
        <select :value="form.isPublish" @change="updateForm('isPublish', $event.target.value === 'true')">
          <option :value="true">已发布</option>
          <option :value="false">未发布</option>
        </select>
      </div>

      <div class="form-group form-group-status">
        <label>状态:</label>
        <select :value="form.isValid" @change="updateForm('isValid', $event.target.value === 'true')">
          <option :value="true">已上线</option>
          <option :value="false">已下线</option>
        </select>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BasicInfoSection',
  props: {
    form: {
      type: Object,
      required: true
    },
    errors: {
      type: Object,
      default: () => ({})
    }
  },
  methods: {
    updateForm(field, value) {
      this.$emit('update:form', field, value)
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
  margin-bottom: 15px;
}

/* 调整字段的宽度 */
.form-group-id {
  flex: 0 0 250px !important;
}

.form-group-version {
  flex: 0 0 250px !important;
}

.form-group-status {
  flex: 0 0 250px !important;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #303133;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group input:disabled {
  background-color: #f5f7fa;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .form-row {
    flex-direction: column;
    gap: 0;
  }

  /* 在移动端恢复字段的默认宽度 */
  .form-group-id,
  .form-group-version,
  .form-group-status {
    flex: 1 !important;
  }
}
</style>
