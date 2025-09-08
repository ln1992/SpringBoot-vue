<!-- src/components/matter/MatterDetail.vue -->
<template>
  <div class="matter-detail-container">
    <div class="header">
      <h2>{{ isEditMode ? '编辑事项' : '新增事项' }}</h2>
    </div>

    <div class="matter-detail-content">
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
            this.selectedMaterials.push(JSON.parse(JSON.stringify(material)))
          }
        }
      })
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
        isShared: false,
        source: '',
        processingMethodAndInfoAccess: '',
        isEligibleForPromise: false
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

    validateForm() {
      this.errors = {}

      if (!this.form.mainItemName || this.form.mainItemName.trim() === '') {
        this.errors.mainItemName = '主项名称不能为空'
      }

      if (!this.form.mainItemCode) {
        this.errors.mainItemCode = '主项编号不能为空'
      }

      if (this.form.legalTimeLimit === null || this.form.legalTimeLimit === undefined) {
        this.errors.legalTimeLimit = '法定时限不能为空'
      }

      if (this.form.committedTimeLimit === null || this.form.committedTimeLimit === undefined) {
        this.errors.committedTimeLimit = '承诺时限不能为空'
      }

      if (!this.form.approvalLevel) {
        this.errors.approvalLevel = '审批层级不能为空'
      }

      if (!this.form.provincialDepartmentOffice) {
        this.errors.provincialDepartmentOffice = '省厅对口指导处室不能为空'
      }

      return Object.keys(this.errors).length === 0
    },

    async handleSubmit() {
      if (!this.validateForm()) {
        alert('请检查表单中的错误信息')
        return
      }

      try {
        const matterData = this.prepareMatterData()
        let response

        if (this.isEditMode) {
          response = await matterService.updateMatter(this.form.id, matterData);
        } else {
          response = await matterService.createMatter(matterData);
        }

        this.$emit('matter-updated', response);
        alert(this.isEditMode ? '事项更新成功' : '事项创建成功');
      } catch (error) {
        console.error('保存事项出错:', error);
        alert('保存失败: ' + error.message);
      }
    },

    prepareMatterData() {
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

.matter-detail-content {
  background-color: white;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1001;
}

@media (max-width: 768px) {
  .matter-detail-container {
    padding: 10px;
  }
}
</style>
