<!-- src/components/update-record/UpdateRecordDetail.vue -->
<template>
  <div class="update-record-detail-container">
    <div class="header">
      <h2>更新记录详情</h2>
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
}
</style>
