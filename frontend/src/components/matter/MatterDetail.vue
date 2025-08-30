<!-- src/components/matter/MatterDetail.vue -->
<template>
  <div class="matter-detail-container">
    <div class="header">
      <h2>{{ isEditMode ? '编辑事项' : '新增事项' }}</h2>
    </div>

    <div class="matter-detail-content">
      <form @submit.prevent="handleSubmit">
        <!-- ID和版本字段放在同一行 -->
        <div class="form-row">
          <div class="form-group form-group-id">
            <label>ID:</label>
            <input type="text" v-model="form.id" disabled>
          </div>

          <div class="form-group">
            <label>版本:</label>
            <input type="text" v-model="form.version">
          </div>
        </div>

        <!-- 主项信息 -->
        <div class="form-row">
          <div class="form-group">
            <label>主项编号</label>
            <input type="number" v-model.number="form.mainItemCode">
          </div>
          <div class="form-group">
            <label>主项名称 *</label>
            <input type="text" v-model="form.mainItemName" required>
          </div>
        </div>

        <!-- 子项信息 -->
        <div class="form-row">
          <div class="form-group">
            <label>子项编号</label>
            <input type="number" v-model.number="form.subItemCode">
          </div>
          <div class="form-group">
            <label>子项名称 *</label>
            <input type="text" v-model="form.subItemName" required>
          </div>
        </div>

        <!-- 孙项信息 -->
        <div class="form-row">
          <div class="form-group">
            <label>孙项编号</label>
            <input type="number" v-model.number="form.grandchildItemCode">
          </div>
          <div class="form-group">
            <label>孙项名称 *</label>
            <input type="text" v-model="form.grandchildItemName" required>
          </div>
        </div>

        <!-- 经办依据 -->
        <div class="form-group">
          <label>经办依据:</label>
          <div class="basis-list-container">
            <div
              class="basis-item"
              v-for="(basis, index) in form.bases"
              :key="index"
            >
              <input
                type="text"
                v-model="form.bases[index]"
                placeholder="请输入经办依据"
              >
              <button
                type="button"
                class="remove-basis-btn"
                @click="removeBasis(index)"
              >
                删除
              </button>
            </div>
            <button
              type="button"
              class="add-basis-btn"
              @click="addBasis"
            >
              + 新增法条
            </button>
          </div>
        </div>

        <!-- 关联材料 -->
        <div class="form-group">
          <label>关联材料:</label>
          <div class="material-selection-container">
            <div
              class="material-item"
              v-for="(materialId, index) in form.materialIds"
              :key="index"
            >
              <div class="material-select-wrapper">
                <input
                  type="text"
                  class="material-search-input"
                  :placeholder="'搜索材料...'"
                  v-model="materialSearchQueries[index]"
                  @input="onMaterialSearchInput(index, $event.target.value)"
                  @focus="onMaterialSearchFocus(index)"
                >
                <div
                  class="material-search-dropdown"
                  v-if="materialSearchResults[index] && materialSearchResults[index].length > 0"
                >
                  <div
                    class="material-search-option"
                    v-for="material in materialSearchResults[index]"
                    :key="material.id"
                    @click="selectMaterial(index, material)"
                  >
                    {{ material.id }} - {{ material.materialDetail }}
                  </div>
                </div>
              </div>
              <button
                type="button"
                class="remove-material-btn"
                @click="removeMaterial(index)"
              >
                删除
              </button>
            </div>
            <button
              type="button"
              class="add-material-btn"
              @click="addMaterial"
            >
              + 添加材料
            </button>
          </div>
        </div>

        <!-- 时限信息放在同一行 -->
        <div class="form-row">
          <div class="form-group">
            <label>法定时限 (天) *</label>
            <input type="number" v-model.number="form.legalTimeLimit" required>
          </div>
          <div class="form-group">
            <label>承诺时限 (天) *</label>
            <input type="number" v-model.number="form.committedTimeLimit" required>
          </div>
        </div>

        <!-- 审批层级和省厅对口指导处室放在同一行 -->
        <div class="form-row">
          <div class="form-group">
            <label>审批层级 *</label>
            <select v-model="form.approvalLevel" required>
              <option value="">请选择审批层级</option>
              <option
                v-for="level in approvalLevels"
                :key="level.name"
                :value="level.name"
              >
                {{ level.description }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label>省厅对口指导处室 *</label>
            <select v-model="form.provincialDepartmentOffice" required>
              <option value="">请选择省厅对口指导处室</option>
              <option
                v-for="office in provincialDepartmentOffices"
                :key="office.name"
                :value="office.name"
              >
                {{ office.description }}
              </option>
            </select>
          </div>
        </div>

        <!-- 流程图上传 -->
        <div class="form-group">
          <label>审批流程图:</label>
          <div class="process-diagram-selection">
            <div class="diagram-select-wrapper">
              <input
                type="text"
                class="diagram-search-input"
                placeholder="搜索审批流程图..."
                v-model="approvalDiagramSearchQuery"
                @input="onApprovalDiagramSearchInput($event.target.value)"
                @focus="onApprovalDiagramSearchFocus"
              >
              <div
                class="diagram-search-dropdown"
                v-if="approvalDiagramSearchResults.length > 0"
              >
                <div
                  class="diagram-search-option"
                  v-for="diagram in approvalDiagramSearchResults"
                  :key="diagram.id"
                  @click="selectApprovalDiagram(diagram)"
                >
                  {{ diagram.id }} - {{ diagram.imageName }}
                </div>
              </div>
            </div>
            <button
              v-if="form.approvalProcessDiagramId"
              type="button"
              class="clear-selection-btn"
              @click="clearApprovalDiagram"
            >
              清除
            </button>
          </div>
          <div v-if="form.approvalProcessDiagramId" class="image-preview">
            <img
              :src="getApprovalProcessDiagramUrl(form.approvalProcessDiagramId)"
              alt="审批流程图预览"
            />
          </div>
        </div>

        <div class="form-group">
          <label>业务流程图:</label>
          <div class="process-diagram-selection">
            <div class="diagram-select-wrapper">
              <input
                type="text"
                class="diagram-search-input"
                placeholder="搜索业务流程图..."
                v-model="businessDiagramSearchQuery"
                @input="onBusinessDiagramSearchInput($event.target.value)"
                @focus="onBusinessDiagramSearchFocus"
              >
              <div
                class="diagram-search-dropdown"
                v-if="businessDiagramSearchResults.length > 0"
              >
                <div
                  class="diagram-search-option"
                  v-for="diagram in businessDiagramSearchResults"
                  :key="diagram.id"
                  @click="selectBusinessDiagram(diagram)"
                >
                  {{ diagram.id }} - {{ diagram.imageName }}
                </div>
              </div>
            </div>
            <button
              v-if="form.businessProcessDiagramId"
              type="button"
              class="clear-selection-btn"
              @click="clearBusinessDiagram"
            >
              清除
            </button>
          </div>
          <div v-if="form.businessProcessDiagramId" class="image-preview">
            <img
              :src="getBusinessProcessDiagramUrl(form.businessProcessDiagramId)"
              alt="业务流程图预览"
            />
          </div>
        </div>

        <!-- 发布状态和状态放在同一行 -->
        <div class="form-row">
          <div class="form-group">
            <label>发布状态:</label>
            <select v-model="form.isPublish">
              <option :value="true">已发布</option>
              <option :value="false">未发布</option>
            </select>
          </div>

          <div class="form-group">
            <label>状态:</label>
            <select v-model="form.isValid">
              <option :value="true">已上线</option>
              <option :value="false">已下线</option>
            </select>
          </div>
        </div>

        <div class="form-actions">
          <button type="button" @click="goBack" class="back-btn-form">返回</button>
          <button type="submit" class="save-btn">{{ isEditMode ? '保存' : '创建' }}</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
const API_BASE_URL = 'http://localhost:8000/api/matters'
const API_UPDATE = (id) => `${API_BASE_URL}/${id}`
const API_CREATE = API_BASE_URL
const API_MATERIALS_URL = 'http://localhost:8000/api/materials'

export default {
  name: 'MatterDetail',
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
        businessProcessDiagramId: null
      },
      materialSearchQueries: [],
      materialSearchResults: [],
      approvalDiagramSearchQuery: '',
      businessDiagramSearchQuery: '',
      approvalDiagramSearchResults: [],
      businessDiagramSearchResults: [],
      // 审批层级枚举
      approvalLevels: [
        { name: 'PROVINCIAL', description: '省级' },
        { name: 'PROVINCIAL_MUNICIPAL', description: '省市两级' },
        { name: 'PROVINCIAL_MUNICIPAL_COUNTY', description: '省市县三级' },
        { name: 'MUNICIPAL', description: '设区的市' },
        { name: 'MUNICIPAL_COUNTY', description: '市县两级' },
        { name: 'COUNTY', description: '县级' }
      ],
      // 省厅对口指导处室枚举
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
      return !!this.matter.id;
    }
  },
  created() {
    // 初始化表单数据
    this.resetForm()
  },
  methods: {
    resetForm() {
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
        businessProcessDiagramId: this.matter.businessProcessDiagramId || null
      }

      // 初始化材料搜索查询
      this.materialSearchQueries = []
      this.materialSearchResults = []

      // 为每个材料ID设置搜索查询文本
      this.form.materialIds.forEach((materialId, index) => {
        if (materialId) {
          const material = this.materialsList.find(m => m.id === materialId)
          if (material) {
            this.materialSearchQueries[index] = `${material.id} - ${material.materialDetail}`
          } else {
            this.materialSearchQueries[index] = materialId.toString()
          }
        } else {
          this.materialSearchQueries[index] = ''
        }
        this.materialSearchResults[index] = []
      })

      // 初始化流程图搜索查询
      this.approvalDiagramSearchQuery = ''
      this.businessDiagramSearchQuery = ''
      this.approvalDiagramSearchResults = []
      this.businessDiagramSearchResults = []

      // 设置审批流程图搜索查询文本
      if (this.form.approvalProcessDiagramId) {
        const diagram = this.approvalProcessDiagrams.find(d => d.id === this.form.approvalProcessDiagramId)
        if (diagram) {
          this.approvalDiagramSearchQuery = `${diagram.id} - ${diagram.imageName}`
        }
      }

      // 设置业务流程图搜索查询文本
      if (this.form.businessProcessDiagramId) {
        const diagram = this.businessProcessDiagrams.find(d => d.id === this.form.businessProcessDiagramId)
        if (diagram) {
          this.businessDiagramSearchQuery = `${diagram.id} - ${diagram.imageName}`
        }
      }
    },

    // 返回列表
    goBack() {
      this.$emit('back')
    },

    addBasis() {
      this.form.bases.push('')
    },

    removeBasis(index) {
      this.form.bases.splice(index, 1)
    },

    addMaterial() {
      this.form.materialIds.push(null)
      this.materialSearchQueries.push('')
      this.materialSearchResults.push([])
    },

    removeMaterial(index) {
      this.form.materialIds.splice(index, 1)
      this.materialSearchQueries.splice(index, 1)
      this.materialSearchResults.splice(index, 1)
    },

    // 材料搜索输入处理
    onMaterialSearchInput(index, query) {
      if (!this.materialSearchResults[index]) {
        this.$set(this.materialSearchResults, index, [])
      }

      if (query.trim() === '') {
        this.$set(this.materialSearchResults, index, [])
        return
      }

      // 过滤材料列表
      const filtered = this.materialsList.filter(material =>
        material.id.toString().includes(query) ||
        (material.materialDetail && material.materialDetail.includes(query))
      )

      this.$set(this.materialSearchResults, index, filtered)
    },

    // 材料搜索框获得焦点时显示所有材料
    onMaterialSearchFocus(index) {
      if (!this.materialSearchResults[index]) {
        this.$set(this.materialSearchResults, index, [])
      }

      // 如果搜索框为空，显示所有材料
      if (!this.materialSearchQueries[index] || this.materialSearchQueries[index].trim() === '') {
        this.$set(this.materialSearchResults, index, this.materialsList)
      }
    },

    // 选择材料
    selectMaterial(index, material) {
      this.$set(this.form.materialIds, index, material.id)
      this.$set(this.materialSearchQueries, index, `${material.id} - ${material.materialDetail}`)
      this.$set(this.materialSearchResults, index, [])
    },

    // 处理审批流程图搜索输入
    onApprovalDiagramSearchInput(query) {
      if (query.trim() === '') {
        this.approvalDiagramSearchResults = []
        return
      }

      // 过滤流程图列表
      const filtered = this.approvalProcessDiagrams.filter(diagram =>
        diagram.id.toString().includes(query) ||
        (diagram.imageName && diagram.imageName.includes(query))
      )

      this.approvalDiagramSearchResults = filtered
    },

    // 审批流程图搜索框获得焦点时显示所有流程图
    onApprovalDiagramSearchFocus() {
      // 如果搜索框为空，显示所有流程图
      if (!this.approvalDiagramSearchQuery || this.approvalDiagramSearchQuery.trim() === '') {
        this.approvalDiagramSearchResults = [...this.approvalProcessDiagrams]
      }
    },

    // 选择审批流程图
    selectApprovalDiagram(diagram) {
      this.form.approvalProcessDiagramId = diagram.id
      this.approvalDiagramSearchQuery = `${diagram.id} - ${diagram.imageName}`
      this.approvalDiagramSearchResults = []
    },

    // 清除审批流程图选择
    clearApprovalDiagram() {
      this.form.approvalProcessDiagramId = null
      this.approvalDiagramSearchQuery = ''
      this.approvalDiagramSearchResults = []
    },

    // 处理业务流程图搜索输入
    onBusinessDiagramSearchInput(query) {
      if (query.trim() === '') {
        this.businessDiagramSearchResults = []
        return
      }

      // 过滤流程图列表
      const filtered = this.businessProcessDiagrams.filter(diagram =>
        diagram.id.toString().includes(query) ||
        (diagram.imageName && diagram.imageName.includes(query))
      )

      this.businessDiagramSearchResults = filtered
    },

    // 业务流程图搜索框获得焦点时显示所有流程图
    onBusinessDiagramSearchFocus() {
      // 如果搜索框为空，显示所有流程图
      if (!this.businessDiagramSearchQuery || this.businessDiagramSearchQuery.trim() === '') {
        this.businessDiagramSearchResults = [...this.businessProcessDiagrams]
      }
    },

    // 选择业务流程图
    selectBusinessDiagram(diagram) {
      this.form.businessProcessDiagramId = diagram.id
      this.businessDiagramSearchQuery = `${diagram.id} - ${diagram.imageName}`
      this.businessDiagramSearchResults = []
    },

    // 清除业务流程图选择
    clearBusinessDiagram() {
      this.form.businessProcessDiagramId = null
      this.businessDiagramSearchQuery = ''
      this.businessDiagramSearchResults = []
    },

    // 获取审批流程图URL
    getApprovalProcessDiagramUrl(id) {
      const diagram = this.approvalProcessDiagrams.find(d => d.id === id)
      return diagram ? diagram.imageDataUrl : ''
    },

    // 获取业务流程图URL
    getBusinessProcessDiagramUrl(id) {
      const diagram = this.businessProcessDiagrams.find(d => d.id === id)
      return diagram ? diagram.imageDataUrl : ''
    },

    // 表单提交处理
    async handleSubmit() {
      try {
        // 验证并转换materialIds
        const validMaterialIds = this.form.materialIds
          .filter(id => id !== null && id !== undefined)
          .map(id => {
            const numId = typeof id === 'string' ? parseInt(id, 10) : id
            return isNaN(numId) ? null : numId
          })
          .filter(id => id !== null)

        // 验证流程图ID
        const validApprovalDiagramId = this.form.approvalProcessDiagramId ?
          (typeof this.form.approvalProcessDiagramId === 'string' ?
            parseInt(this.form.approvalProcessDiagramId, 10) :
            this.form.approvalProcessDiagramId) :
          null

        const validBusinessDiagramId = this.form.businessProcessDiagramId ?
          (typeof this.form.businessProcessDiagramId === 'string' ?
            parseInt(this.form.businessProcessDiagramId, 10) :
            this.form.businessProcessDiagramId) :
          null

        // 构造要发送的数据对象
        const matterData = {
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
          approvalProcessDiagramId: isNaN(validApprovalDiagramId) ? null : validApprovalDiagramId,
          businessProcessDiagramId: isNaN(validBusinessDiagramId) ? null : validBusinessDiagramId,
          isValid: this.form.isValid,
          isPublish: this.form.isPublish
        }

        // 验证必填字段
        if (!matterData.mainItemName || !matterData.subItemName || !matterData.grandchildItemName) {
          alert('主项名称、子项名称和孙项名称不能为空')
          return
        }

        if (matterData.legalTimeLimit === null || matterData.committedTimeLimit === null) {
          alert('法定时限和承诺时限必须填写')
          return
        }

        let response;
        if (this.isEditMode) {
          // 编辑模式 - 更新事项
          response = await fetch(API_UPDATE(this.form.id), {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify(matterData)
          })
        } else {
          // 新增模式 - 创建事项
          response = await fetch(API_CREATE, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify(matterData)
          })
        }

        if (response.ok) {
          const updatedMatter = await response.json()
          this.$emit('matter-updated', updatedMatter)
          alert(this.isEditMode ? '事项更新成功' : '事项创建成功')
        } else {
          const errorText = await response.text()
          console.error('Server error response:', errorText)

          let errorMessage = `HTTP ${response.status}: ${response.statusText}`
          try {
            const errorJson = JSON.parse(errorText)
            if (errorJson.message) {
              errorMessage = errorJson.message
            }
          } catch (e) {
            // 如果响应不是JSON格式，使用原始错误信息
            errorMessage = errorText
          }

          alert((this.isEditMode ? '更新' : '创建') + '失败: ' + errorMessage)
        }
      } catch (error) {
        console.error('保存事项出错:', error)
        alert('保存失败: ' + error.message)
      }
    }
  }
}
</script>

<style scoped>.matter-detail-container {
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

/* 表单样式 */
.form-group {
  margin-bottom: 15px;
}

.form-row {
  display: flex;
  gap: 15px;
}

.form-row .form-group {
  flex: 1;
  margin-bottom: 15px;
}

/* 调整ID字段的宽度 */
.form-group-id {
  flex: 0 0 120px; /* ID字段更窄 */
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #303133;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group input:disabled {
  background-color: #f5f7fa;
  cursor: not-allowed;
}

.form-group textarea {
  min-height: 60px;
  resize: vertical;
}

.form-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

/* 经办依据列表样式 */
.basis-list-container {
  width: 100%;
}

.basis-item {
  display: flex;
  margin-bottom: 10px;
  gap: 10px;
}

.basis-item input {
  flex: 1;
}

.remove-basis-btn {
  background-color: #f56c6c;
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  white-space: nowrap;
}

.remove-basis-btn:hover {
  background-color: #f78989;
}

.add-basis-btn {
  background-color: #409eff;
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.add-basis-btn:hover {
  background-color: #66b1ff;
}

/* 材料选择样式 */
.material-selection-container {
  width: 100%;
}

.material-item {
  display: flex;
  margin-bottom: 10px;
  gap: 10px;
}

.material-select-wrapper {
  flex: 1;
  position: relative;
}

.material-search-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.material-search-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #dcdfe6;
  border-top: none;
  border-radius: 0 0 4px 4px;
  max-height: 200px;
  overflow-y: auto;
  z-index: 100;
}

.material-search-option {
  padding: 8px 12px;
  cursor: pointer;
}

.material-search-option:hover {
  background-color: #f5f7fa;
}

.remove-material-btn {
  background-color: #f56c6c;
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  white-space: nowrap;
}

.remove-material-btn:hover {
  background-color: #f78989;
}

.add-material-btn {
  background-color: #409eff;
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.add-material-btn:hover {
  background-color: #66b1ff;
}

/* 流程图选择样式 */
.process-diagram-selection {
  display: flex;
  gap: 10px;
}

.diagram-select-wrapper {
  flex: 1;
  position: relative;
}

.diagram-search-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.diagram-search-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #dcdfe6;
  border-top: none;
  border-radius: 0 0 4px 4px;
  max-height: 200px;
  overflow-y: auto;
  z-index: 100;
}

.diagram-search-option {
  padding: 8px 12px;
  cursor: pointer;
}

.diagram-search-option:hover {
  background-color: #f5f7fa;
}

.clear-selection-btn {
  background-color: #f56c6c;
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  white-space: nowrap;
}

.clear-selection-btn:hover {
  background-color: #f78989;
}

/* 图片上传样式 */
.image-preview {
  margin-top: 10px;
}

.image-preview img {
  max-width: 100%;
  max-height: 200px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.form-actions {
  margin-top: 20px;
  text-align: right;
  position: relative;
  z-index: 1002;
}

.form-actions button {
  margin-left: 10px;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  position: relative;
  z-index: 1003;
}

.form-actions button[type="button"] {
  background-color: #909399;
  color: white;
  border: none;
}

.form-actions button[type="button"]:hover {
  background-color: #a6a9ad;
}

.back-btn-form {
  background-color: #909399 !important;
  color: white !important;
  border: none !important;
}

.back-btn-form:hover {
  background-color: #a6a9ad !important;
}

.save-btn {
  background-color: #67c23a;
  color: white;
  border: none;
}

.save-btn:hover {
  background-color: #85ce61;
}

@media (max-width: 768px) {
  .matter-detail-container {
    padding: 10px;
  }

  .form-row {
    flex-direction: column;
    gap: 0;
  }

  /* 在移动端恢复ID字段的默认宽度 */
  .form-group-id {
    flex: 1;
  }

  .basis-item,
  .material-item,
  .process-diagram-selection {
    flex-direction: column;
  }

  .form-actions {
    display: flex;
    justify-content: space-between;
  }

  .form-actions button {
    flex: 1;
    margin: 0 5px;
  }
}
</style>
