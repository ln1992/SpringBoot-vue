<!-- src/components/material/sections/BasicInfo.vue -->
<template>
  <div class="basic-info-section">
    <!-- ID和材料明细放在同一行 -->
    <div class="form-row" v-if="isEditMode">
      <div class="form-group">
        <label>ID:</label>
        <input type="text" :value="form.id" disabled>
      </div>

      <div class="form-group">
        <label>材料明细 *</label>
        <input
          type="text"
          :value="form.materialDetail"
          @input="$emit('update-field', 'materialDetail', $event.target.value)"
          required
          :class="{ 'error': errors.materialDetail }"
        >
        <div class="error-message" v-if="errors.materialDetail">
          {{ errors.materialDetail }}
        </div>
      </div>
    </div>

    <!-- 新增模式下材料明细单独一行 -->
    <div class="form-group" v-else>
      <label>材料明细 *</label>
      <input
        type="text"
        :value="form.materialDetail"
        @input="$emit('update-field', 'materialDetail', $event.target.value)"
        required
        :class="{ 'error': errors.materialDetail }"
      >
      <div class="error-message" v-if="errors.materialDetail">
        {{ errors.materialDetail }}
      </div>
    </div>

    <!-- 版本和显示名称放在同一行 -->
    <div class="form-row">
      <div class="form-group">
        <label>版本:</label>
        <input
          type="number"
          :value="form.version"
          @input="$emit('update-field', 'version', $event.target.value)"
          min="1"
        >
        <div class="error-message" v-if="errors.version">
          {{ errors.version }}
        </div>
      </div>

      <div class="form-group">
        <label>显示名称:</label>
        <input type="text" :value="form.__name__" disabled>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BasicInfo',
  props: {
    form: {
      type: Object,
      required: true
    },
    errors: {
      type: Object,
      default: () => ({})
    },
    isEditMode: {
      type: Boolean,
      default: false
    }
  }
}
</script>

<style scoped>
.form-group {
  margin-bottom: 20px;
}

.form-row {
  display: flex;
  gap: 15px;
}

.form-row .form-group {
  flex: 1;
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

.form-group input.error {
  border-color: #f56c6c;
}

.error-message {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 5px;
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
}
</style>
