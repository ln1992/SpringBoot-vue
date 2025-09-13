<!-- src/components/matter/MatterDetail.vue -->
<template>
  <div class="matter-detail-container">
    <div class="header">
      <h2>{{ isEditMode ? '编辑事项' : '新增事项' }}</h2>
      <button v-if="isEditMode" class="api-btn" @click="openApiUrl" title="查看API数据">API</button>
    </div>

    <div class="matter-detail-content">
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
    ActionButtons
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
      ]
    }
  },
  computed: {
    isEditMode() {
      return !!this.matter.id
    }
  },
  created() {
    this.resetForm()
    // 只在新增模式下加载所有事项用于拷贝功能
    if (!this.isEditMode) {
      this.loadAllMatters();
    }
  },
  methods: {
    resetForm() {
      // 初始化表单逻辑
      this.initializeForm()
      this.initializeMaterials()
    },

    initializeForm() {
      this.form = {
        id: this.matter.id,
        version: this.matter.version,
        mainItemCode: this.matter.mainItemCode,
        subItemCode: this.matter.subItemCode,
        grandchildItemCode: this.matter.grandchildItemCode,
        mainItemName: this.matter.mainItemName || '',
        subItemName: this.matter.subItemName || '',
        grandchildItemName: this.matter.grandchildItemName || '',
        bases: [...(this.matter.bases || [])],
        materialIds: [...(this.matter.materialIds || [])],
        legalTimeLimit: this.matter.legalTimeLimit,
        committedTimeLimit: this.matter.committedTimeLimit,
        approvalLevel: this.matter.approvalLevel || '',
        provincialDepartmentOffice: this.matter.provincialDepartmentOffice || '',
        isValid: this.matter.isValid !== undefined ? this.matter.isValid : true,
        isPublish: this.matter.isPublish !== undefined ? this.matter.isPublish : false,
        approvalProcessDiagramId: this.matter.approvalProcessDiagramId || null,
        businessProcessDiagramId: this.matter.businessProcessDiagramId || null,
        createdTime: this.matter.createdTime || null,
        updateTime: this.matter.updateTime || null
      }
    },

    initializeMaterials() {
      this.selectedMaterials = []
      this.form.materialIds.forEach((materialId) => {
        if (materialId) {
          const material = this.materialsList.find(m => m.id === materialId)
          if (material) {
            // 确保包含所有必要字段
            this.selectedMaterials.push({
              ...material,
              __name__: material.__name__ || ''
            })
          }
        }
      })
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

    // 处理事项拷贝
    async handleMatterCopy() {
      if (!this.selectedMatterForCopy) {
        return;
      }

      try {
        // 获取选中的事项详情
        const matter = await matterService.getMatterById(this.selectedMatterForCopy);
        this.copyMatterData(matter);
        // 重置选择，允许再次选择同一事项
        this.$nextTick(() => {
          this.selectedMatterForCopy = '';
        });
      } catch (error) {
        console.error('拷贝事项失败:', error);
        alert('拷贝事项失败: ' + (error.message || '未知错误'));
      }
    },

    // 拷贝事项数据到当前表单
    copyMatterData(matterData) {
      // 基本信息
      this.form.mainItemCode = matterData.mainItemCode;
      this.form.subItemCode = matterData.subItemCode;
      this.form.grandchildItemCode = matterData.grandchildItemCode;
      this.form.mainItemName = matterData.mainItemName;
      this.form.subItemName = matterData.subItemName;
      this.form.grandchildItemName = matterData.grandchildItemName;

      // 经办依据
      this.form.bases = matterData.bases ? [...matterData.bases] : [];

      // 材料ID列表
      this.form.materialIds = matterData.materialIds ? [...matterData.materialIds] : [];

      // 重新初始化材料
      this.initializeMaterials();

      // 时限信息
      this.form.legalTimeLimit = matterData.legalTimeLimit;
      this.form.committedTimeLimit = matterData.committedTimeLimit;

      // 审批层级和省厅对口指导处室
      this.form.approvalLevel = matterData.approvalLevel;
      this.form.provincialDepartmentOffice = matterData.provincialDepartmentOffice;

      // 流程图ID
      this.form.approvalProcessDiagramId = matterData.approvalProcessDiagramId;
      this.form.businessProcessDiagramId = matterData.businessProcessDiagramId;

      // 注意：不拷贝发布状态和ID
      // this.form.id = matterData.id;
      // this.form.version = matterData.version;
      // this.form.isValid = matterData.isValid;
      // this.form.isPublish = matterData.isPublish;

      alert('事项数据拷贝成功');
    },

    updateForm(field, value) {
      this.form[field] = value
    },

    updateBases(bases) {
      this.form.bases = bases
    },

    addMaterial() {
      this.selectedMaterials.push({
        materialDetail: '',
        reviewPoint: '',
        autoApprovalCriteria: '',
        shared: false,
        materialSource: '',
        processingMethodAndInfoAccess: '',
        eligibleForPromise: false,
        __name__: ''
      })
    },

    removeMaterial(index) {
      this.selectedMaterials.splice(index, 1)
    },

    updateMaterial(index, material) {
      this.$set(this.selectedMaterials, index, material)
    },

    updateApprovalDiagram(diagramId) {
      this.form.approvalProcessDiagramId = diagramId
    },

    updateBusinessDiagram(diagramId) {
      this.form.businessProcessDiagramId = diagramId
    },

    clearApprovalDiagram() {
      this.form.approvalProcessDiagramId = null
    },

    clearBusinessDiagram() {
      this.form.businessProcessDiagramId = null
    },

    goBack() {
      this.$emit('back')
    },

    async handleSubmit() {
      // 表单验证
      if (!this.validateForm()) {
        return
      }

      const formData = this.buildFormData()

      try {
        let response
        if (this.isEditMode) {
          response = await matterService.updateMatter(this.form.id, formData)
        } else {
          response = await matterService.createMatter(formData)
        }

        this.$emit('matter-updated', response)
        alert(this.isEditMode ? '事项更新成功' : '事项创建成功')
      } catch (error) {
        console.error('保存事项出错:', error)
        alert('保存失败: ' + error.message)
      }
    },

    validateForm() {
      this.errors = {}

      // 验证主项编号和名称
      if (!this.form.mainItemCode) {
        this.errors.mainItemCode = '主项编号不能为空'
      }
      if (!this.form.mainItemName) {
        this.errors.mainItemName = '主项名称不能为空'
      }

      // 验证法定时限和承诺时限
      if (this.form.legalTimeLimit === null || this.form.legalTimeLimit === undefined) {
        this.errors.legalTimeLimit = '法定时限不能为空'
      }
      if (this.form.committedTimeLimit === null || this.form.committedTimeLimit === undefined) {
        this.errors.committedTimeLimit = '承诺时限不能为空'
      }

      // 验证审批层级
      if (!this.form.approvalLevel) {
        this.errors.approvalLevel = '审批层级不能为空'
      }

      // 验证省厅对口指导处室
      if (!this.form.provincialDepartmentOffice) {
        this.errors.provincialDepartmentOffice = '省厅对口指导处室不能为空'
      }

      return Object.keys(this.errors).length === 0
    },

    buildFormData() {
      // 过滤掉空的材料ID
      const validMaterialIds = this.selectedMaterials
        .filter(material => material.id)
        .map(material => material.id)

      return {
        id: this.form.id,
        version: this.form.version ? parseInt(this.form.version) : null,
        mainItemCode: this.form.mainItemCode,
        subItemCode: this.form.subItemCode,
        grandchildItemCode: this.form.grandchildItemCode,
        mainItemName: this.form.mainItemName,
        subItemName: this.form.subItemName,
        grandchildItemName: this.form.grandchildItemName,
        bases: this.form.bases.filter(basis => basis !== ''),
        materialIds: validMaterialIds,
        legalTimeLimit: this.form.legalTimeLimit,
        committedTimeLimit: this.form.committedTimeLimit,
        approvalLevel: this.form.approvalLevel,
        provincialDepartmentOffice: this.form.provincialDepartmentOffice,
        approvalProcessDiagramId: this.form.approvalProcessDiagramId,
        businessProcessDiagramId: this.form.businessProcessDiagramId,
        valid: this.form.isValid,
        publish: this.form.isPublish,
        materials: this.selectedMaterials.filter(material => material.id)
      }
    },

    // 打开API网址查看数据
    openApiUrl() {
      if (this.form.id) {
        // 构造API URL，假设API端点为 /api/matters/{id}
        const apiUrl = `${window.location.origin}/api/matters/${this.form.id}`;
        window.open(apiUrl, '_blank');
      } else {
        alert('事项ID不存在，无法打开API链接');
      }
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

.matter-detail-content {
  background-color: white;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1001;
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
