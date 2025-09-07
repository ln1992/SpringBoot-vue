<!-- src/components/material/sections/FormActions.vue -->
<template>
  <div class="form-actions">
    <button type="button" @click="$emit('back')" class="back-btn-form">返回</button>
    <button 
      type="submit" 
      class="save-btn" 
      :disabled="isSubmitting">
      {{ isSubmitting ? '保存中...' : (isEditMode ? '保存' : '创建') }}
    </button>
  </div>
</template>

<script>
export default {
  name: 'FormActions',
  props: {
    isSubmitting: {
      type: Boolean,
      default: false
    },
    isEditMode: {
      type: Boolean,
      default: false
    }
  }
}
</script>

<style scoped>
.form-actions {
  margin-top: 30px;
  text-align: right;
  position: relative;
  z-index: 1002;
}

.form-actions button {
  margin-left: 10px;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  position: relative;
  z-index: 1003;
}

.form-actions button[type="button"] {
  background-color: #909399;
  color: white;
  border: none;
}

.form-actions button[type="button"]:hover {
  background-color: #a6a9ad;
}

.form-actions button:disabled {
  background-color: #a0a0a0;
  cursor: not-allowed;
}

.back-btn-form {
  background-color: #909399 !important;
  color: white !important;
  border: none !important;
}

.back-btn-form:hover {
  background-color: #a6a9ad !important;
}

.save-btn {
  background-color: #67c23a;
  color: white;
  border: none;
}

.save-btn:hover:not(:disabled) {
  background-color: #85ce61;
}

@media (max-width: 768px) {
  .form-actions {
    display: flex;
    justify-content: space-between;
  }

  .form-actions button {
    flex: 1;
    margin: 0 5px;
  }
}
</style>