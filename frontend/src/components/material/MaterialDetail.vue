<!-- src/components/material/MaterialDetail.vue -->
<template>
  <div class="material-detail-container">
    <div class="header">
      <h2>{{ isEditMode ? '编辑材料' : '新增材料' }}</h2>
    </div>

    <div class="material-detail-content">
      <!-- 拷贝功能区 -->
      <div class="copy-section" v-if="!isEditMode && allMaterials.length > 0">
        <div class="form-group">
          <label for="copyMaterial">拷贝材料:</label>
          <div class="material-copy-container">
            <input
              type="text"
              v-model="materialSearchQuery"
              placeholder="输入或选择材料"
              class="material-search-input"
              @input="onMaterialSearchInput"
              @focus="showMaterialDropdown = true"
              @blur="hideMaterialDropdown"
            />
            <div
              v-if="showMaterialDropdown"
              class="material-dropdown-list"
              @mousedown.prevent
            >
              <div
                v-for="availableMaterial in filteredMaterials"
                :key="availableMaterial.id"
                class="material-dropdown-item"
                @mousedown="selectMaterialFromDropdown(availableMaterial)"
              >
                {{ availableMaterial.__name__ }}
              </div>
              <div
                v-if="filteredMaterials && filteredMaterials.length === 0"
                class="no-results"
              >
                无匹配结果
              </div>
            </div>
          </div>
        </div>
      </div>

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
      submitting: false,
      // 拷贝功能相关数据
      allMaterials: [],
      materialSearchQuery: '',
      showMaterialDropdown: false,
      filteredMaterials: []
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
    // 正确初始化表单数据
    if (this.material && Object.keys(this.material).length > 0) {
      this.form = { ...this.material };
    } else {
      // 新增模式下使用默认值初始化表单
      this.form = { ...this.$options.props.material.default() };
    }

    // 只在新增模式下加载所有材料用于拷贝功能
    if (!this.isEditMode) {
      this.loadAllMaterials();
    }
  },
  methods: {
    updateField(field, value) {
      this.form[field] = value
    },

    // 加载所有材料用于拷贝功能
    async loadAllMaterials() {
      try {
        this.allMaterials = await materialService.getAllMaterials();
        // 限制初始显示数量
        this.filteredMaterials = this.allMaterials.slice(0, 100);
      } catch (error) {
        console.error('加载材料列表失败:', error);
        // 不中断用户操作，只是不显示拷贝功能
        this.allMaterials = [];
      }
    },

    // 处理材料搜索输入
    onMaterialSearchInput() {
      if (!this.materialSearchQuery) {
        // 限制显示数量，只显示前100个材料
        this.filteredMaterials = this.allMaterials.slice(0, 100);
      } else {
        const query = this.materialSearchQuery.toLowerCase();
        // 过滤并限制显示数量
        this.filteredMaterials = this.allMaterials
          .filter(material =>
            material.__name__ && material.__name__.toLowerCase().includes(query)
          )
          .slice(0, 100);
      }
      this.showMaterialDropdown = true;
    },

    // 显示材料下拉列表
    showMaterialDropdown() {
      this.showMaterialDropdown = true;
    },

    // 隐藏材料下拉列表
    hideMaterialDropdown() {
      // 延迟隐藏，确保点击选项时能正常触发
      setTimeout(() => {
        this.showMaterialDropdown = false;
      }, 200);
    },

    // 从下拉列表中选择材料
    async selectMaterialFromDropdown(material) {
      this.materialSearchQuery = material.__name__;
      this.showMaterialDropdown = false;

      try {
        // 获取选中的材料详情
        const materialDetail = await materialService.getMaterialById(material.id);
        this.copyMaterialData(materialDetail);
      } catch (error) {
        console.error('拷贝材料失败:', error);
        alert('拷贝材料失败: ' + (error.message || '未知错误'));
      }
    },

    // 拷贝材料数据到当前表单
    copyMaterialData(materialData) {
      // 基本信息
      this.updateField('materialDetail', materialData.materialDetail);
      this.updateField('reviewPoint', materialData.reviewPoint);
      this.updateField('autoApprovalCriteria', materialData.autoApprovalCriteria);
      this.updateField('shared', materialData.shared);
      this.updateField('materialSource', materialData.materialSource);
      this.updateField('processingMethodAndInfoAccess', materialData.processingMethodAndInfoAccess);
      this.updateField('eligibleForPromise', materialData.eligibleForPromise);
      this.updateField('version', materialData.version);

      alert('材料数据拷贝成功');
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

/* 拷贝功能区样式 */
.copy-section {
  margin-bottom: 20px;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #f9f9f9;
}

.form-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.form-group label {
  font-weight: bold;
  white-space: nowrap;
}

/* 材料拷贝搜索框样式 */
.material-copy-container {
  position: relative;
  flex: 1;
}

.material-search-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.material-search-input:focus {
  outline: none;
  border-color: #409eff;
}

.material-dropdown-list {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #dcdfe6;
  border-top: none;
  border-radius: 0 0 4px 4px;
  background-color: white;
  z-index: 1002;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.material-dropdown-item {
  padding: 8px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
}

.material-dropdown-item:hover {
  background-color: #f5f7fa;
}

.material-dropdown-item:last-child {
  border-bottom: none;
}

.no-results {
  padding: 8px 12px;
  color: #909399;
  font-style: italic;
}

/* 防止页面自动滚动到底部 */
.material-detail-content form {
  scroll-behavior: auto;
}

@media (max-width: 768px) {
  .material-detail-container {
    padding: 10px;
  }

  .form-group {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
