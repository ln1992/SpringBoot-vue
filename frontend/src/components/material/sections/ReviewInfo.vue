<!-- src/components/material/sections/ReviewInfo.vue -->
<template>
  <div class="review-info-section">
    <!-- 审核要点 -->
    <div class="form-group">
      <label>审核要点:</label>
      <textarea
        :value="form.reviewPoint"
        @input="$emit('update-field', 'reviewPoint', $event.target.value)"></textarea>
    </div>

    <!-- "智能秒批"判断标准 -->
    <div class="form-group">
      <label>"智能秒批"判断标准:</label>
      <textarea
        :value="form.autoApprovalCriteria"
        @input="$emit('update-field', 'autoApprovalCriteria', $event.target.value)"></textarea>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ReviewInfo',
  props: {
    form: {
      type: Object,
      required: true
    }
  }
}
</script>

<style scoped>
.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #303133;
}

.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
  min-height: 80px;
  resize: vertical;
}
</style>
