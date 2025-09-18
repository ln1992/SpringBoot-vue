<!-- src/components/matter/MatterDetail.vue -->
<template>
  <div class="matter-detail-container">
    <div class="header">
      <h2>{{ isEditMode ? '编辑事项' : '新增事项' }}</h2>
      <button v-if="isEditMode" class="api-btn" @click="openApiUrl" title="查看API数据">API</button>
    </div>

    <!-- 添加Tab页 -->
    <div class="tabs" v-if="isEditMode">
      <button 
        :class="{ active: activeTab === 'detail' }"
        @click="activeTab = 'detail'"
      >
        事项详情
      </button>
      <button 
        :class="{ active: activeTab === 'updates' }"
        @click="activeTab = 'updates'"
      >
        更新记录
      </button>
    </div>

    <div class="matter-detail-content">
      <!-- 事项详情 Tab -->
      <div v-show="activeTab === 'detail'">
        <!-- 拷贝功能区 -->
        <div class="copy-section" v-if="!isEditMode && allMatters.length > 0">
          <div class="form-group">
            <label for="copyMatter">拷贝事项:</label>
            <div class="matter-copy-container">
              <input
                type="text"
                v-model="matterSearchQuery"
                placeholder="输入或选择事项"
                class="matter-search-input"
                @input="onMatterSearchInput"
                @focus="showMatterDropdown = true"
                @blur="hideMatterDropdown"
              />
              <div
                v-if="showMatterDropdown"
                class="matter-dropdown-list"
                @mousedown.prevent
              >
                <div
                  v-for="availableMatter in filteredMatters"
                  :key="availableMatter.id"
                  class="matter-dropdown-item"
                  @mousedown="selectMatterFromDropdown(availableMatter)"
                >
                  {{ availableMatter.__name__ }}
                </div>
                <div
                  v-if="filteredMatters && filteredMatters.length === 0"
                  class="no-results"
                >
                  无匹配结果
                </div>
              </div>
            </div>
          </div>
        </div>

        <form @submit.prevent="handleSubmit">
          <!-- 基本信息 -->
          <BasicInfoSection
            :form="form"
            :errors="errors"
            @update:form="updateForm" />

          <!-- 事项层级信息 -->
          <ItemHierarchySection
            :form="form"
            :errors="errors"
            @update:form="updateForm" />

          <!-- 经办依据 -->
          <BasisSection
            :bases="form.bases"
            @update:bases="updateBases" />

          <!-- 关联材料 -->
          <MaterialsSection
            :materials="selectedMaterials"
            :materials-list="materialsList"
            @add-material="addMaterial"
            @remove-material="removeMaterial"
            @update-material="updateMaterial" />

          <!-- 时限和审批信息 -->
          <TimeAndApprovalSection
            :form="form"
            :errors="errors"
            :approval-levels="approvalLevels"
            :provincial-offices="provincialDepartmentOffices"
            @update:form="updateForm" />

          <!-- 流程图 -->
          <ProcessDiagramsSection
            :approval-diagram-id="form.approvalProcessDiagramId"
            :business-diagram-id="form.businessProcessDiagramId"
            :approval-diagrams="approvalProcessDiagrams"
            :business-diagrams="businessProcessDiagrams"
            @update:approval-diagram="updateApprovalDiagram"
            @update:business-diagram="updateBusinessDiagram"
            @clear:approval-diagram="clearApprovalDiagram"
            @clear:business-diagram="clearBusinessDiagram" />

          <!-- 时间信息 -->
          <TimeInfoSection
            :created-time="form.createdTime"
            :update-time="form.updateTime" />

          <!-- 操作按钮 -->
          <ActionButtons
            @back="goBack"
            :is-edit-mode="isEditMode"
            @submit="handleSubmit" />
        </form>
      </div>

      <!-- 更新记录 Tab -->
      <div v-show="activeTab === 'updates'" v-if="isEditMode">
        <!-- 只在未选择记录时显示列表 -->
        <UpdateRecordList 
          v-if="!selectedUpdateRecord"
          ref="updateRecordList"
          :filter-entity-type="'Matter'"
          :filter-entity-id="form.id"
          :hide-actions="true"
          :hide-filters="true"
          :hide-pagination="true"
          @view-record="handleViewRecord" />
          
        <!-- 更新记录详情 -->
        <div v-else class="update-record-detail-wrapper">
          <div class="detail-header">
            <button class="back-btn" @click="selectedUpdateRecord = null">← 返回</button>
            <h3>更新记录详情</h3>
          </div>
          <UpdateRecordDetail 
            :record="selectedUpdateRecord" 
            @back="selectedUpdateRecord = null" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import BasicInfoSection from './sections/BasicInfoSection.vue'
import ItemHierarchySection from './sections/ItemHierarchySection.vue'
import BasisSection from './sections/BasisSection.vue'
import MaterialsSection from './sections/MaterialsSection.vue'
import TimeAndApprovalSection from './sections/TimeAndApprovalSection.vue'
import ProcessDiagramsSection from './sections/ProcessDiagramsSection.vue'
import TimeInfoSection from './sections/TimeInfoSection.vue'
import ActionButtons from './sections/ActionButtons.vue'
import UpdateRecordList from '../update-record/UpdateRecordList.vue'
import UpdateRecordDetail from '../update-record/UpdateRecordDetail.vue'
import { matterService } from '../../api';

export default {
  name: 'MatterDetail',
  components: {
    BasicInfoSection,
    ItemHierarchySection,
    BasisSection,
    MaterialsSection,
    TimeAndApprovalSection,
    ProcessDiagramsSection,
    TimeInfoSection,
    ActionButtons,
    UpdateRecordList,
    UpdateRecordDetail
  },
  props: {
    matter: {
      type: Object,
      required: true
    },
    materialsList: {
      type: Array,
      default: () => []
    },
    approvalProcessDiagrams: {
      type: Array,
      default: () => []
    },
    businessProcessDiagrams: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      form: {
        id: null,
        version: null,
        mainItemCode: null,
        subItemCode: null,
        grandchildItemCode: null,
        mainItemName: '',
        subItemName: '',
        grandchildItemName: '',
        bases: [],
        materialIds: [],
        legalTimeLimit: null,
        committedTimeLimit: null,
        approvalLevel: '',
        provincialDepartmentOffice: '',
        isValid: true,
        isPublish: false,
        approvalProcessDiagramId: null,
        businessProcessDiagramId: null,
        createdTime: null,
        updateTime: null
      },
      selectedMaterials: [],
      errors: {},
      // 拷贝功能相关数据
      allMatters: [],
      selectedMatterForCopy: '',
      matterSearchQuery: '',
      showMatterDropdown: false,
      filteredMatters: [],
      approvalLevels: [
        { name: 'PROVINCIAL', description: '省级' },
        { name: 'PROVINCIAL_MUNICIPAL', description: '省市两级' },
        { name: 'PROVINCIAL_MUNICIPAL_COUNTY', description: '省市县三级' },
        { name: 'MUNICIPAL', description: '设区的市' },
        { name: 'MUNICIPAL_COUNTY', description: '市县两级' },
        { name: 'COUNTY', description: '县级' }
      ],
      provincialDepartmentOffices: [
        { name: 'PROVINCIAL_DEPARTMENT_POLICY_REGULATIONS', description: '厅政策法规处' },
        { name: 'PROVINCIAL_DEPARTMENT_ADMINISTRATIVE_APPROVAL', description: '厅行政审批处' },
        { name: 'PROVINCIAL_DEPARTMENT_TRANSPORTATION_MANAGEMENT', description: '厅运输管理处' },
        { name: 'PROVINCIAL_PORT_CENTER_CONSTRUCTION', description: '省港航中心建设处' },
        { name: 'PROVINCIAL_PORT_CENTER_MANAGEMENT', description: '省港航中心管理处' },
        { name: 'PROVINCIAL_HIGHWAY_CENTER_CONSTRUCTION', description: '省公路中心建设处' },
        { name: 'PROVINCIAL_HIGHWAY_CENTER_MAINTENANCE', description: '省公路中心养护处' }
      ],
      // Tab页相关数据
      activeTab: 'detail',
      // 更新记录详情相关数据
      selectedUpdateRecord: null
    }
  },
  computed: {
    isEditMode() {
      return !!this.matter.id
    }
  },
  watch: {
    matter: {
      handler(newVal) {
        this.initializeForm(newVal);
      },
      deep: true,
      immediate: true
    },
    materialsList: {
      handler() {
        this.updateSelectedMaterials();
      },
      deep: true
    }
  },
  mounted() {
    // 只在新增模式下加载所有事项用于拷贝功能
    if (!this.isEditMode) {
      this.loadAllMatters();
    }
  },
  methods: {
    initializeForm(matter) {
      // 初始化表单数据
      this.form = {
        id: matter.id || null,
        version: matter.version || null,
        mainItemCode: matter.mainItemCode || null,
        subItemCode: matter.subItemCode || null,
        grandchildItemCode: matter.grandchildItemCode || null,
        mainItemName: matter.mainItemName || '',
        subItemName: matter.subItemName || '',
        grandchildItemName: matter.grandchildItemName || '',
        bases: Array.isArray(matter.bases) ? [...matter.bases] : [],
        materialIds: Array.isArray(matter.materialIds) ? [...matter.materialIds] : [],
        legalTimeLimit: matter.legalTimeLimit || null,
        committedTimeLimit: matter.committedTimeLimit || null,
        approvalLevel: matter.approvalLevel || '',
        provincialDepartmentOffice: matter.provincialDepartmentOffice || '',
        isValid: matter.isValid !== undefined ? matter.isValid : true,
        isPublish: matter.isPublish !== undefined ? matter.isPublish : false,
        approvalProcessDiagramId: matter.approvalProcessDiagramId || null,
        businessProcessDiagramId: matter.businessProcessDiagramId || null,
        createdTime: matter.createdTime || null,
        updateTime: matter.updateTime || null
      };

      // 更新选中的材料
      this.updateSelectedMaterials();
    },

    updateSelectedMaterials() {
      // 根据 materialIds 更新 selectedMaterials
      this.selectedMaterials = this.materialsList.filter(material =>
        this.form.materialIds.includes(material.id)
      );
    },

    updateForm(field, value) {
      this.form[field] = value;
    },

    updateBases(bases) {
      this.form.bases = bases;
    },

    addMaterial(material) {
      // 检查是否已添加
      if (!this.selectedMaterials.find(m => m.id === material.id)) {
        this.selectedMaterials.push(material);
        this.form.materialIds.push(material.id);
      }
    },

    removeMaterial(materialId) {
      const index = this.selectedMaterials.findIndex(m => m.id === materialId);
      if (index !== -1) {
        this.selectedMaterials.splice(index, 1);
        const idIndex = this.form.materialIds.indexOf(materialId);
        if (idIndex !== -1) {
          this.form.materialIds.splice(idIndex, 1);
        }
      }
    },

    updateMaterial({ materialId, field, value }) {
      const material = this.selectedMaterials.find(m => m.id === materialId);
      if (material) {
        material[field] = value;
      }
    },

    updateApprovalDiagram(diagramId) {
      this.form.approvalProcessDiagramId = diagramId;
    },

    updateBusinessDiagram(diagramId) {
      this.form.businessProcessDiagramId = diagramId;
    },

    clearApprovalDiagram() {
      this.form.approvalProcessDiagramId = null;
    },

    clearBusinessDiagram() {
      this.form.businessProcessDiagramId = null;
    },

    goBack() {
      this.$emit('back');
    },

    // 返回列表界面
    goBackToList() {
      this.$emit('back');
    },

    validateForm() {
      this.errors = {};

      // 验证主项名称
      if (!this.form.mainItemName || this.form.mainItemName.trim() === '') {
        this.errors.mainItemName = '主项名称不能为空';
      }

      // 验证版本号
      if (this.form.version && (isNaN(this.form.version) || this.form.version < 1)) {
        this.errors.version = '版本号必须是大于0的数字';
      }

      // 验证法定时限
      if (this.form.legalTimeLimit !== null && this.form.legalTimeLimit !== undefined && 
          (isNaN(this.form.legalTimeLimit) || this.form.legalTimeLimit < 0)) {
        this.errors.legalTimeLimit = '法定时限必须是非负数';
      }

      // 验证承诺时限
      if (this.form.committedTimeLimit !== null && this.form.committedTimeLimit !== undefined && 
          (isNaN(this.form.committedTimeLimit) || this.form.committedTimeLimit < 0)) {
        this.errors.committedTimeLimit = '承诺时限必须是非负数';
      }

      return Object.keys(this.errors).length === 0;
    },

    async handleSubmit() {
      if (!this.validateForm()) {
        // 表单验证失败，滚动到第一个错误
        this.$nextTick(() => {
          const firstError = document.querySelector('.error-message');
          if (firstError) {
            firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
          }
        });
        return;
      }

      try {
        let response;
        if (this.isEditMode) {
          response = await matterService.updateMatter(this.form.id, this.form);
        } else {
          // 确保版本号有效
          if (!this.form.version || this.form.version < 1) {
            this.form.version = 1;
          }
          response = await matterService.createMatter(this.form);
        }

        this.$emit('matter-updated', response);
        alert(this.isEditMode ? '事项更新成功' : '事项创建成功');
      } catch (error) {
        console.error('保存事项出错:', error);
        alert('保存失败: ' + (error.message || '未知错误'));
      }
    },

    // 加载所有事项用于拷贝功能
    async loadAllMatters() {
      try {
        this.allMatters = await matterService.getAllMatters();
        // 限制初始显示数量
        this.filteredMatters = this.allMatters.slice(0, 100);
      } catch (error) {
        console.error('加载事项列表失败:', error);
        // 不中断用户操作，只是不显示拷贝功能
        this.allMatters = [];
      }
    },

    // 处理事项搜索输入
    onMatterSearchInput() {
      if (!this.matterSearchQuery) {
        // 限制显示数量，只显示前100个事项
        this.filteredMatters = this.allMatters.slice(0, 100);
      } else {
        const query = this.matterSearchQuery.toLowerCase();
        // 过滤并限制显示数量
        this.filteredMatters = this.allMatters
          .filter(matter =>
            matter.__name__ && matter.__name__.toLowerCase().includes(query)
          )
          .slice(0, 100);
      }
      this.showMatterDropdown = true;
    },

    // 显示事项下拉列表
    showMatterDropdown() {
      this.showMatterDropdown = true;
    },

    // 隐藏事项下拉列表
    hideMatterDropdown() {
      // 延迟隐藏，确保点击选项时能正常触发
      setTimeout(() => {
        this.showMatterDropdown = false;
      }, 200);
    },

    // 从下拉列表中选择事项
    async selectMatterFromDropdown(matter) {
      this.matterSearchQuery = matter.__name__;
      this.showMatterDropdown = false;

      try {
        // 获取选中的事项详情
        const matterDetail = await matterService.getMatterById(matter.id);
        this.copyMatterData(matterDetail);
      } catch (error) {
        console.error('拷贝事项失败:', error);
        alert('拷贝事项失败: ' + (error.message || '未知错误'));
      }
    },

    // 拷贝事项数据到当前表单
    copyMatterData(matterData) {
      // 基本信息
      this.updateForm('mainItemCode', matterData.mainItemCode);
      this.updateForm('subItemCode', matterData.subItemCode);
      this.updateForm('grandchildItemCode', matterData.grandchildItemCode);
      this.updateForm('mainItemName', matterData.mainItemName);
      this.updateForm('subItemName', matterData.subItemName);
      this.updateForm('grandchildItemName', matterData.grandchildItemName);
      this.updateForm('version', matterData.version);
      
      // 经办依据
      this.updateBases([...matterData.bases]);

      // 时限和审批信息
      this.updateForm('legalTimeLimit', matterData.legalTimeLimit);
      this.updateForm('committedTimeLimit', matterData.committedTimeLimit);
      this.updateForm('approvalLevel', matterData.approvalLevel);
      this.updateForm('provincialDepartmentOffice', matterData.provincialDepartmentOffice);
      this.updateForm('isValid', matterData.isValid);
      this.updateForm('isPublish', matterData.isPublish);

      // 流程图
      this.updateForm('approvalProcessDiagramId', matterData.approvalProcessDiagramId);
      this.updateForm('businessProcessDiagramId', matterData.businessProcessDiagramId);

      // 材料
      this.form.materialIds = [...matterData.materialIds];
      this.updateSelectedMaterials();

      alert('事项数据拷贝成功');
    },

    // 打开API网址查看数据
    openApiUrl() {
      const url = `/api/matters/${this.form.id}`;
      window.open(url, '_blank');
    },

    // 处理查看更新记录事件
    handleViewRecord(record) {
      this.selectedUpdateRecord = record;
    }
  }
}
</script>

<style scoped>
.matter-detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 10px;
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

/* Tab页样式 */
.tabs {
  display: flex;
  margin-bottom: 20px;
  border-bottom: 1px solid #dcdfe6;
}

.tabs button {
  padding: 10px 20px;
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-bottom: none;
  border-radius: 4px 4px 0 0;
  cursor: pointer;
  margin-right: 5px;
}

.tabs button.active {
  background-color: #ffffff;
  border-bottom: 1px solid #ffffff;
  margin-bottom: -1px;
  font-weight: bold;
}

.matter-detail-content {
  background-color: white;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1;
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

/* 事项拷贝搜索框样式 */
.matter-copy-container {
  position: relative;
  flex: 1;
}

.matter-search-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.matter-search-input:focus {
  outline: none;
  border-color: #409eff;
}

.matter-dropdown-list {
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

.matter-dropdown-item {
  padding: 8px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
}

.matter-dropdown-item:hover {
  background-color: #f5f7fa;
}

.matter-dropdown-item:last-child {
  border-bottom: none;
}

.no-results {
  padding: 8px 12px;
  color: #909399;
  font-style: italic;
}

/* 更新记录详情样式 */
.update-record-detail-wrapper {
  margin-top: 20px;
}

.detail-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.detail-header h3 {
  margin: 0;
  margin-left: 10px;
}


@media (max-width: 768px) {
  .matter-detail-container {
    padding: 10px;
  }

  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .form-group {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>