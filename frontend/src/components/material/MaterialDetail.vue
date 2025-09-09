<!-- src/components/material/MaterialDetail.vue -->
<template>
  <div class="material-detail-container">
    <div class="header">
      <h2>{{ isEditMode ? '编辑材料' : '新增材料' }}</h2>
    </div>

    <div class="material-detail-content">
      <form @submit.prevent="handleSubmit">
        <BasicInfo
          :form="form"
          :errors="errors"
          :is-edit-mode="isEditMode"
          @update-field="updateField" />

        <ReviewInfo
          :form="form"
          @update-field="updateField" />

        <SourceAndSharing
          :form="form"
          @update-field="updateField" />

        <ProcessingInfo
          :form="form"
          @update-field="updateField" />

        <StatusSection
          :form="form"
          @update-field="updateField" />

        <TimeInfo
          :created-time="form.createdTime"
          :update-time="form.updateTime" />

        <FormActions
          @back="goBack"
          @submit="handleSubmit"
          :is-submitting="submitting"
          :is-edit-mode="isEditMode" />
      </form>
    </div>
  </div>
</template>

<script>
import { materialService } from '../../api';
import BasicInfo from './sections/BasicInfo.vue'
import ReviewInfo from './sections/ReviewInfo.vue'
import SourceAndSharing from './sections/SourceAndSharing.vue'
import ProcessingInfo from './sections/ProcessingInfo.vue'
import StatusSection from './sections/StatusSection.vue'
import TimeInfo from './sections/TimeInfo.vue'
import FormActions from './sections/FormActions.vue'

export default {
  name: 'MaterialDetail',
  components: {
    BasicInfo,
    ReviewInfo,
    SourceAndSharing,
    ProcessingInfo,
    StatusSection,
    TimeInfo,
    FormActions
  },
  props: {
    material: {
      type: Object,
      default: () => ({
        id: null,
        materialDetail: '',
        reviewPoint: '',
        autoApprovalCriteria: '',
        shared: false,
        materialSource: 'PERSONAL_SUBMISSION',
        processingMethodAndInfoAccess: 'ONLINE_PROCESSING',
        eligibleForPromise: false,
        valid: true,
        version: 1,
        __name__: '',
        createdTime: null,
        updateTime: null
      })
    }
  },
  data() {
    return {
      form: {
        id: null,
        materialDetail: '',
        version: 1,
        reviewPoint: '',
        autoApprovalCriteria: '',
        shared: false,
        materialSource: 'PERSONAL_SUBMISSION',
        processingMethodAndInfoAccess: 'ONLINE_PROCESSING',
        eligibleForPromise: false,
        valid: true,
        createdTime: null,
        updateTime: null,
        __name__: ''
      },
      errors: {},
      submitting: false
    };
  },
  computed: {
    isEditMode() {
      return !!this.material.id
    }
  },
  watch: {
    material: {
      handler(newVal) {
        this.form = { ...newVal }
      },
      deep: true,
      immediate: true
    }
  },
  mounted() {
    // 确保在组件挂载时初始化表单
    if (this.material) {
      this.form = { ...this.material };
    }
  },
  methods: {
    updateField(field, value) {
      this.form[field] = value
    },

    goBack() {
      this.$emit('back')
    },

    validateForm() {
      this.errors = {}

      if (!this.form.materialDetail || this.form.materialDetail.trim() === '') {
        this.errors.materialDetail = '材料明细不能为空'
        return false
      }

      if (this.form.version && (isNaN(this.form.version) || this.form.version < 1)) {
        this.errors.version = '版本号必须是大于0的数字'
        return false
      }

      return true
    },

    formatDateTime(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN')
    },

    async handleSubmit() {
      if (this.submitting) return;

      if (!this.validateForm()) return;

      this.submitting = true;

      try {
        if (!this.form.version || this.form.version < 1) {
          this.form.version = 1;
        }

        let response;
        if (this.isEditMode) {
          response = await materialService.updateMaterial(this.form.id, this.form);
        } else {
          response = await materialService.createMaterial(this.form);
        }

        this.$emit('material-updated', response);
        alert(this.isEditMode ? '材料更新成功' : '材料创建成功');
        this.form = { ...response };
      } catch (error) {
        console.error('保存材料出错:', error);
        alert('保存失败: ' + error.message);
      } finally {
        this.submitting = false;
      }
    }
  }
}
</script>

<style scoped>
.material-detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1; /* 降低z-index值，确保侧边栏可以覆盖 */
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

.material-detail-content {
  background-color: white;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1; /* 降低z-index值，确保侧边栏可以覆盖 */
}

/* 防止页面自动滚动到底部 */
.material-detail-content form {
  scroll-behavior: auto;
}

@media (max-width: 768px) {
  .material-detail-container {
    padding: 10px;
  }
}
</style>
