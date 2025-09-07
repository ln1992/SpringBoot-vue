<!-- src/components/material/sections/TimeInfo.vue -->
<template>
  <div class="time-info-section" v-if="createdTime || updateTime">
    <div class="form-group">
      <label>时间信息:</label>
      <div class="time-details">
        <p v-if="createdTime">创建时间: {{ formatDateTime(createdTime) }}</p>
        <p v-if="updateTime">更新时间: {{ formatDateTime(updateTime) }}</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TimeInfo',
  props: {
    createdTime: {
      type: String,
      default: null
    },
    updateTime: {
      type: String,
      default: null
    }
  },
  methods: {
    formatDateTime(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.time-info-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
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

.time-details p {
  margin: 5px 0;
  color: #909399;
  font-size: 14px;
}
</style>
