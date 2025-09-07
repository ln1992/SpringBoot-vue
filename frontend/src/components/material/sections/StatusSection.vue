<!-- src/components/material/sections/StatusSection.vue -->
<template>
  <div class="status-section">
    <!-- 是否有效 -->
    <div class="form-group">
      <label>是否有效:</label>
      <select
        :value="form.valid"
        @change="$emit('update-field', 'valid', $event.target.value === 'true')">
        <option :value="true">已上线</option>
        <option :value="false">已下线</option>
      </select>
    </div>
  </div>
</template>

<script>
export default {
  name: 'StatusSection',
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
</style>
