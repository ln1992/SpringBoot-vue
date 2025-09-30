<!-- src/components/update-record/UpdateRecordDetail.vue -->
<template>
  <div class="update-record-detail-container">
    <div class="header">
      <h2>更新记录详情</h2>
      <div class="header-actions">
        <button class="api-btn" @click="openApiUrl" title="查看API数据">API</button>
        <button
          v-if="canNavigateToEntity(record.entityType, record.entityId)"
          class="navigate-btn"
          @click="navigateToEntity(record.entityType, record.entityId)"
          title="跳转到相关实体">
          跳转到实体
        </button>
      </div>
    </div>

    <div class="update-record-detail-content">
      <form @submit.prevent="handleSubmit">
        <BasicInfo
          :record="record"
          @navigate-to-entity="navigateToEntity" />

        <OperationInfo
          :record="record" />

        <DataInfo
          :record="record"
          @navigate-to-material-detail="navigateToMaterialDetail"
          @navigate-to-approval-diagram-detail="navigateToApprovalDiagramDetail"
          @navigate-to-business-diagram-detail="navigateToBusinessDiagramDetail" />

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
        this.$message({
          message: '记录ID不存在，无法打开API链接',
          type: 'warning'
        });
      }
    },

    // 判断是否可以跳转到实体
    canNavigateToEntity(entityType, entityId) {
      return entityType && entityId && !isNaN(entityId);
    },

    // 跳转到实体详情
    navigateToEntity(entityType, entityId) {
      if (!this.canNavigateToEntity(entityType, entityId)) {
        return;
      }

      const entityIdNum = parseInt(entityId);

      switch (entityType) {
        case 'Material':
          // 跳转到材料详情
          this.$router.push({ name: 'MaterialDetail', params: { id: entityIdNum } });
          break;
        case 'Matter':
          // 跳转到事项详情
          this.$router.push({ name: 'MatterDetail', params: { id: entityIdNum } });
          break;
        case 'ApprovalProcessDiagram':
          // 跳转到审批流程图详情
          this.$router.push({ name: 'ApprovalDiagramDetail', params: { id: entityIdNum } });
          break;
        case 'BusinessProcessDiagram':
          // 跳转到业务流程图详情
          this.$router.push({ name: 'BusinessDiagramDetail', params: { id: entityIdNum } });
          break;
        default:
          this.$message({
            message: '不支持的实体类型',
            type: 'warning'
          });
      }
    },

    // 跳转到材料详情
    navigateToMaterialDetail(materialId) {
      this.$router.push({ name: 'MaterialDetail', params: { id: materialId } });
    },

    // 跳转到审批流程图详情
    navigateToApprovalDiagramDetail(diagramId) {
      this.$router.push({ name: 'ApprovalDiagramDetail', params: { id: diagramId } });
    },

    // 跳转到业务流程图详情
    navigateToBusinessDiagramDetail(diagramId) {
      this.$router.push({ name: 'BusinessDiagramDetail', params: { id: diagramId } });
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

.header-actions {
  display: flex;
  gap: 10px;
}

.api-btn, .navigate-btn {
  background-color: #409eff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.api-btn:hover, .navigate-btn:hover {
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

  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
