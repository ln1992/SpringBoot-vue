<!-- src/components/update-record/UpdateRecordDetail.vue -->
<template>
  <div class="update-record-detail-container">
    <div class="header">
      <h2>更新记录详情</h2>
      <button class="api-btn" @click="openApiUrl" title="查看API数据">API</button>
    </div>

    <div class="update-record-detail-content">
      <form @submit.prevent="handleSubmit">
        <BasicInfo
          :record="record" />

        <OperationInfo
          :record="record" />

        <DataInfo
          :record="record" />

        <TimeInfo
          :record="record" />

        <FormActions
          @back="goBack" />
      </form>
    </div>
  </div>
</template>

<script>
import BasicInfo from './sections/BasicInfo.vue'
import OperationInfo from './sections/OperationInfo.vue'
import TimeInfo from './sections/TimeInfo.vue'
import DataInfo from './sections/DataInfo.vue'
import FormActions from './sections/FormActions.vue'

export default {
  name: 'UpdateRecordDetail',
  components: {
    BasicInfo,
    OperationInfo,
    TimeInfo,
    DataInfo,
    FormActions
  },
  props: {
    record: {
      type: Object,
      required: true
    }
  },
  methods: {
    goBack() {
      this.$emit('back')
    },

    handleSubmit() {
      // 空提交处理函数，保持与MaterialDetail.vue一致的结构
    },

    // 打开API网址查看数据
    openApiUrl() {
      if (this.record.id) {
        // 构造API URL，假设API端点为 /api/update-records/{id}
        const apiUrl = `${window.location.origin}/api/update-records/${this.record.id}`;
        window.open(apiUrl, '_blank');
      } else {
        alert('记录ID不存在，无法打开API链接');
      }
    }
  }
}
</script>

<style scoped>
.update-record-detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1001;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  color: #303133;
  margin: 0;
}

.api-btn {
  background-color: #409eff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.api-btn:hover {
  background-color: #337ecc;
}

.update-record-detail-content {
  background-color: white;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1001;
}

@media (max-width: 768px) {
  .update-record-detail-container {
    padding: 10px;
  }

  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
