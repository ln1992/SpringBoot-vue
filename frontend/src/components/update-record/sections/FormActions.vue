<template>
  <div class="form-actions">
    <button @click="goBack" class="back-btn">返回</button>
  </div>
</template>

<script>
export default {
  name: 'FormActions',
  methods: {
    goBack() {
      this.$emit('back')
    }
  }
}
</script>

<style scoped>
.form-actions {
  display: flex;
  justify-content: flex-end; /* 将按钮放置在右侧 */
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.back-btn {
  padding: 10px 20px;
  background-color: #f0f0f0;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  color: #333;
}

.back-btn:hover {
  background-color: #e0e0e0;
}
</style>