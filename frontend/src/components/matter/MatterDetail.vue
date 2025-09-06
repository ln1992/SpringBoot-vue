<!-- src/components/matter/MatterDetail.vue -->
<template>
  <div class="matter-detail-container">
    <div class="header">
      <h2>{{ isEditMode ? '编辑事项' : '新增事项' }}</h2>
    </div>

    <div class="matter-detail-content">
      <form @submit.prevent="handleSubmit">
        <!-- ID、版本、发布状态和状态字段放在同一行 -->
        <div class="form-row">
          <div class="form-group form-group-id">
            <label>ID:</label>
            <input type="text" v-model="form.id" disabled>
          </div>

          <div class="form-group form-group-version">
            <label>版本:</label>
            <input type="text" v-model="form.version">
          </div>

          <div class="form-group form-group-status">
            <label>发布状态:</label>
            <select v-model="form.isPublish">
              <option :value="true">已发布</option>
              <option :value="false">未发布</option>
            </select>
          </div>

          <div class="form-group form-group-status">
            <label>状态:</label>
            <select v-model="form.isValid">
              <option :value="true">已上线</option>
              <option :value="false">已下线</option>
            </select>
          </div>
        </div>

        <!-- 主项、子项、孙项信息放在同一行 -->
        <div class="form-row item-info-row">
          <!-- 主项信息 -->
          <div class="form-group item-code-group">
            <label>主项编号 *</label>
            <input
              type="number"
              v-model.number="form.mainItemCode"
              required
              :class="{ 'error': errors.mainItemCode }"
            >
            <div class="error-message" v-if="errors.mainItemCode">{{ errors.mainItemCode }}</div>
          </div>
          <div class="form-group item-name-group">
            <label>主项名称 *</label>
            <input
              type="text"
              v-model="form.mainItemName"
              required
              :class="{ 'error': errors.mainItemName }"
            >
            <div class="error-message" v-if="errors.mainItemName">{{ errors.mainItemName }}</div>
          </div>

          <!-- 子项信息 -->
          <div class="form-group item-code-group">
            <label>子项编号</label>
            <input type="number" v-model.number="form.subItemCode">
          </div>
          <div class="form-group item-name-group">
            <label>子项名称</label>
            <input type="text" v-model="form.subItemName">
          </div>

          <!-- 孙项信息 -->
          <div class="form-group item-code-group">
            <label>孙项编号</label>
            <input type="number" v-model.number="form.grandchildItemCode">
          </div>
          <div class="form-group item-name-group">
            <label>孙项名称</label>
            <input type="text" v-model="form.grandchildItemName">
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
          <div class="materials-container">
            <!-- 材料表格 -->
            <div class="materials-table-container" v-if="selectedMaterials.length > 0">
              <table class="materials-table">
                <thead>
                <tr>
                  <th class="material-col-detail">材料明细</th>
                  <th class="material-col-review">审核要点</th>
                  <th class="material-col-auto-approval">智能秒批判断标准</th>
                  <th class="material-col-shared">是否共享</th>
                  <th class="material-col-source">材料来源</th>
                  <th class="material-col-processing">办理方式说明</th>
                  <th class="material-col-notification">适用告知承诺</th>
                  <th class="material-col-action">操作</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="(material, index) in selectedMaterials" :key="index">
                  <td>
                    <!-- materialDetail 字段改为可输入的下拉选择 -->
                    <div class="material-detail-autocomplete-container">
                      <div class="material-detail-input-wrapper">
                        <input
                          :ref="'materialInput' + index"
                          type="text"
                          v-model="material.materialDetail"
                          placeholder="输入或选择材料明细"
                          class="material-detail-input"
                          @focus="showMaterialDropdown(index)"
                          @blur="hideMaterialDropdown(index)"
                          @input="onMaterialInput(index, $event.target.value)"
                        />
                        <div
                          v-if="showMaterialDropdownList[index]"
                          class="material-dropdown-list"
                        >
                          <div
                            v-for="availableMaterial in filteredMaterialsList[index]"
                            :key="availableMaterial.id"
                            class="material-dropdown-item"
                            @mousedown="selectMaterialFromDropdown(index, availableMaterial)"
                          >
                            {{ availableMaterial.id }} - {{ availableMaterial.materialDetail }}
                          </div>
                        </div>
                      </div>
                    </div>
                  </td>
                  <td>
                    <!-- reviewPoint 字段改为只读显示 -->
                    <span class="readonly-field">{{ material.reviewPoint }}</span>
                  </td>
                  <td>
                    <!-- autoApprovalCriteria 字段改为只读显示 -->
                    <span class="readonly-field">{{ material.autoApprovalCriteria }}</span>
                  </td>
                  <td>
                    <!-- isShared 字段改为只读显示 -->
                    <span class="readonly-field">{{ material.isShared ? '是' : '否' }}</span>
                  </td>
                  <td>
                    <!-- source 字段改为只读显示 -->
                    <span class="readonly-field">{{ getMaterialSourceLabel(material.source) }}</span>
                  </td>
                  <td>
                    <!-- processingMethodAndInfoAccess 字段改为只读显示 -->
                    <span class="readonly-field">{{ material.processingMethodAndInfoAccess }}</span>
                  </td>
                  <td>
                    <!-- isEligibleForPromise 字段改为只读显示 -->
                    <span class="readonly-field">{{ material.isEligibleForPromise ? '是' : '否' }}</span>
                  </td>
                  <td>
                    <button
                      type="button"
                      class="remove-material-btn"
                      @click="removeMaterial(index)"
                    >
                      删除
                    </button>
                  </td>
                </tr>
                </tbody>
              </table>
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

        <!-- 时限、审批层级和省厅对口指导处室放在同一行 -->
        <div class="form-row time-approval-row">
          <div class="form-group">
            <label>法定时限 (天) *</label>
            <input
              type="number"
              v-model.number="form.legalTimeLimit"
              required
              :class="{ 'error': errors.legalTimeLimit }"
            >
            <div class="error-message" v-if="errors.legalTimeLimit">{{ errors.legalTimeLimit }}</div>
          </div>
          <div class="form-group">
            <label>承诺时限 (天) *</label>
            <input
              type="number"
              v-model.number="form.committedTimeLimit"
              required
              :class="{ 'error': errors.committedTimeLimit }"
            >
            <div class="error-message" v-if="errors.committedTimeLimit">{{ errors.committedTimeLimit }}</div>
          </div>
          <div class="form-group">
            <label>审批层级 *</label>
            <select
              v-model="form.approvalLevel"
              required
              :class="{ 'error': errors.approvalLevel }"
            >
              <option value="">请选择审批层级</option>
              <option
                v-for="level in approvalLevels"
                :key="level.name"
                :value="level.name"
              >
                {{ level.description }}
              </option>
            </select>
            <div class="error-message" v-if="errors.approvalLevel">{{ errors.approvalLevel }}</div>
          </div>

          <div class="form-group">
            <label>省厅对口指导处室 *</label>
            <select
              v-model="form.provincialDepartmentOffice"
              required
              :class="{ 'error': errors.provincialDepartmentOffice }"
            >
              <option value="">请选择省厅对口指导处室</option>
              <option
                v-for="office in provincialDepartmentOffices"
                :key="office.name"
                :value="office.name"
              >
                {{ office.description }}
              </option>
            </select>
            <div class="error-message" v-if="errors.provincialDepartmentOffice">{{ errors.provincialDepartmentOffice }}</div>
          </div>
        </div>

        <!-- 流程图上传 - 并列显示 -->
        <div class="form-group">
          <div class="process-diagrams-container">
            <!-- 审批流程图 -->
            <div class="process-diagram-section">
              <div class="process-diagram-selection">
                <div class="diagram-select-wrapper">
                  <label class="diagram-type-label">审批流程图:</label>
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

            <!-- 业务流程图 -->
            <div class="process-diagram-section">
              <div class="process-diagram-selection">
                <div class="diagram-select-wrapper">
                  <label class="diagram-type-label">业务经办流程图:</label>
                  <input
                    type="text"
                    class="diagram-search-input"
                    placeholder="搜索业务经办流程图..."
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
                  alt="业务经办流程图预览"
                />
              </div>
            </div>
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
      selectedMaterials: [], // 存储选中的材料详细信息
      showMaterialDropdownList: [], // 控制材料下拉列表显示
      filteredMaterialsList: [], // 过滤后的材料列表
      approvalDiagramSearchQuery: '',
      businessDiagramSearchQuery: '',
      approvalDiagramSearchResults: [],
      businessDiagramSearchResults: [],
      errors: {},
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
      this.selectedMaterials = []
      this.showMaterialDropdownList = []
      this.filteredMaterialsList = []

      // 为每个材料ID设置搜索查询文本和选中材料详情
      this.form.materialIds.forEach((materialId, index) => {
        if (materialId) {
          const material = this.materialsList.find(m => m.id === materialId)
          if (material) {
            this.materialSearchQueries[index] = `${material.id} - ${material.materialDetail}`
            // 使用深拷贝确保不会修改原始数据
            this.$set(this.selectedMaterials, index, JSON.parse(JSON.stringify(material)))
            // 确保 materialSource 字段正确映射到 source 字段
            if (material.materialSource) {
              this.$set(this.selectedMaterials[index], 'source', material.materialSource)
            }
          } else {
            this.materialSearchQueries[index] = materialId.toString()
            // 如果在材料列表中找不到，则创建一个空对象
            this.$set(this.selectedMaterials, index, {
              id: materialId,
              materialDetail: '',
              reviewPoint: '',
              autoApprovalCriteria: '',
              isShared: false,
              source: '',
              processingMethodAndInfoAccess: '',
              isEligibleForPromise: false
            })
          }
        } else {
          this.materialSearchQueries[index] = ''
          // 初始化空材料对象
          this.$set(this.selectedMaterials, index, {
            materialDetail: '',
            reviewPoint: '',
            autoApprovalCriteria: '',
            isShared: false,
            source: '',
            processingMethodAndInfoAccess: '',
            isEligibleForPromise: false
          })
        }
        this.materialSearchResults[index] = []
        this.$set(this.showMaterialDropdownList, index, false)
        this.$set(this.filteredMaterialsList, index, [...this.materialsList])
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

      // 清空错误信息
      this.errors = {}
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
      this.form.materialIds.push(null);
      this.materialSearchQueries.push('');
      this.materialSearchResults.push([]);
      this.selectedMaterials.push({
        materialDetail: '',
        reviewPoint: '',
        autoApprovalCriteria: '',
        isShared: false,
        source: '',
        processingMethodAndInfoAccess: '',
        isEligibleForPromise: false
      });
      this.showMaterialDropdownList.push(false);
      this.filteredMaterialsList.push([...this.materialsList]);
    },

    // 显示材料下拉列表
    showMaterialDropdown(index) {
      this.$set(this.showMaterialDropdownList, index, true);
      // 显示所有材料作为初始建议
      this.$set(this.filteredMaterialsList, index, [...this.materialsList]);
    },

    // 隐藏材料下拉列表
    hideMaterialDropdown(index) {
      setTimeout(() => {
        this.$set(this.showMaterialDropdownList, index, false);
      }, 200); // 延迟隐藏，确保点击选项时能正常选择
    },

    // 处理材料输入
    onMaterialInput(index, value) {
      // 更新材料明细
      this.$set(this.selectedMaterials[index], 'materialDetail', value);

      if (value.trim() === '') {
        // 如果输入为空，显示所有材料
        this.$set(this.filteredMaterialsList, index, [...this.materialsList]);
      } else {
        // 根据输入过滤材料列表
        const filtered = this.materialsList.filter(material =>
          material.materialDetail.toLowerCase().includes(value.toLowerCase()) ||
          material.id.toString().includes(value)
        );
        this.$set(this.filteredMaterialsList, index, filtered);
      }

      // 查找完全匹配的材料
      const exactMatch = this.materialsList.find(
        material => material.materialDetail === value
      );

      if (exactMatch) {
        // 如果找到完全匹配的材料，更新其他字段
        this.onMaterialDetailChange(index, value, exactMatch);
      } else {
        // 否则，将此视为自定义材料，清空其他字段
        this.$set(this.selectedMaterials[index], 'reviewPoint', '');
        this.$set(this.selectedMaterials[index], 'autoApprovalCriteria', '');
        this.$set(this.selectedMaterials[index], 'isShared', false);
        this.$set(this.selectedMaterials[index], 'source', '');
        this.$set(this.selectedMaterials[index], 'processingMethodAndInfoAccess', '');
        this.$set(this.selectedMaterials[index], 'isEligibleForPromise', false);
        this.$set(this.form.materialIds, index, null);
      }
    },

    // 从下拉列表选择材料
    selectMaterialFromDropdown(index, material) {
      // 更新材料明细
      this.$set(this.selectedMaterials[index], 'materialDetail', material.materialDetail);
      // 触发材料变更处理
      this.onMaterialDetailChange(index, material.materialDetail, material);
      // 隐藏下拉列表
      this.$set(this.showMaterialDropdownList, index, false);
      // 聚焦到下一个字段或保持焦点在当前输入框
      this.$nextTick(() => {
        const nextInput = this.$refs['materialInput' + index];
        if (nextInput && nextInput[0]) {
          nextInput[0].focus();
        }
      });
    },

    // 材料明细变更处理
    onMaterialDetailChange(index, selectedDetail, material = null) {
      if (!selectedDetail) {
        // 如果没有选择材料，清空当前材料的其他字段
        this.$set(this.selectedMaterials[index], 'reviewPoint', '');
        this.$set(this.selectedMaterials[index], 'autoApprovalCriteria', '');
        this.$set(this.selectedMaterials[index], 'isShared', false);
        this.$set(this.selectedMaterials[index], 'source', '');
        this.$set(this.selectedMaterials[index], 'processingMethodAndInfoAccess', '');
        this.$set(this.selectedMaterials[index], 'isEligibleForPromise', false);
        this.$set(this.form.materialIds, index, null);
        return;
      }

      // 如果没有传入material对象，则查找选中的材料
      const selectedMaterial = material || this.materialsList.find(
        material => material.materialDetail === selectedDetail
      );

      if (selectedMaterial) {
        // 更新材料的其他字段
        this.$set(this.selectedMaterials[index], 'reviewPoint', selectedMaterial.reviewPoint || '');
        this.$set(this.selectedMaterials[index], 'autoApprovalCriteria', selectedMaterial.autoApprovalCriteria || '');
        this.$set(this.selectedMaterials[index], 'isShared', selectedMaterial.isShared || false);
        this.$set(this.selectedMaterials[index], 'source', selectedMaterial.materialSource || '');
        this.$set(this.selectedMaterials[index], 'processingMethodAndInfoAccess', selectedMaterial.processingMethodAndInfoAccess || '');
        this.$set(this.selectedMaterials[index], 'isEligibleForPromise', selectedMaterial.isEligibleForPromise || false);

        // 更新 materialIds 数组
        this.$set(this.form.materialIds, index, selectedMaterial.id);
      } else {
        // 自定义材料，清空其他字段
        this.$set(this.selectedMaterials[index], 'reviewPoint', '');
        this.$set(this.selectedMaterials[index], 'autoApprovalCriteria', '');
        this.$set(this.selectedMaterials[index], 'isShared', false);
        this.$set(this.selectedMaterials[index], 'source', '');
        this.$set(this.selectedMaterials[index], 'processingMethodAndInfoAccess', '');
        this.$set(this.selectedMaterials[index], 'isEligibleForPromise', false);
        this.$set(this.form.materialIds, index, null);
      }
    },

    removeMaterial(index) {
      this.form.materialIds.splice(index, 1)
      this.materialSearchQueries.splice(index, 1)
      this.materialSearchResults.splice(index, 1)
      this.selectedMaterials.splice(index, 1)
      this.showMaterialDropdownList.splice(index, 1)
      this.filteredMaterialsList.splice(index, 1)
    },

    // 获取材料来源标签
    getMaterialSourceLabel(sourceValue) {
      if (!sourceValue) return '';

      const sourceMap = {
        'PERSONAL_SUBMISSION': '申请人自备',
        'SYSTEM_AUTO_SHARED': '系统自动获取'
      };

      return sourceMap[sourceValue] || sourceValue;
    },

    // 更新材料字段
    updateMaterialField(index, field, value) {
      if (field === 'materialDetail') {
        this.$set(this.selectedMaterials[index], field, value);
        // 当 materialDetail 改变时，更新对应的其他字段
        this.onMaterialDetailChange(index, value);
      }
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
      // 使用深拷贝确保不会修改原始数据
      const materialCopy = JSON.parse(JSON.stringify(material))
      // 确保 materialSource 字段正确映射到 source 字段
      if (materialCopy.materialSource) {
        materialCopy.source = materialCopy.materialSource
        delete materialCopy.materialSource
      }
      this.$set(this.selectedMaterials, index, materialCopy)
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

    // 表单验证
    validateForm() {
      this.errors = {};

      // 验证主项名称不能为空
      if (!this.form.mainItemName || this.form.mainItemName.trim() === '') {
        this.errors.mainItemName = '主项名称不能为空';
      }

      // 验证主项编号不能为空
      if (!this.form.mainItemCode) {
        this.errors.mainItemCode = '主项编号不能为空';
      }

      // 验证法定时限和承诺时限
      if (this.form.legalTimeLimit === null || this.form.legalTimeLimit === undefined) {
        this.errors.legalTimeLimit = '法定时限不能为空';
      }

      if (this.form.committedTimeLimit === null || this.form.committedTimeLimit === undefined) {
        this.errors.committedTimeLimit = '承诺时限不能为空';
      }

      // 验证审批层级
      if (!this.form.approvalLevel) {
        this.errors.approvalLevel = '审批层级不能为空';
      }

      // 验证省厅对口指导处室
      if (!this.form.provincialDepartmentOffice) {
        this.errors.provincialDepartmentOffice = '省厅对口指导处室不能为空';
      }

      // 返回验证结果
      return Object.keys(this.errors).length === 0;
    },

    // 表单提交处理
    async handleSubmit() {
      // 首先进行表单验证
      if (!this.validateForm()) {
        alert('请检查表单中的错误信息');
        return;
      }

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
          valid: this.form.isValid,  // 修复：使用后端字段名
          publish: this.form.isPublish,  // 修复：使用后端字段名
          // 添加材料详细信息
          materials: this.selectedMaterials.filter(material => material.id !== null)
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

/* 调整字段的宽度 */
.form-group-id {
  flex: 0 0 250px !important;
}

.form-group-version {
  flex: 0 0 250px !important;
}

.form-group-status {
  flex: 0 0 250px !important;
}

/* 主项、子项、孙项信息行样式 */
.item-info-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.item-code-group {
  flex: 0 0 40px; /* 编号字段窄一些 */
}

.item-name-group {
  flex: 1; /* 名称字段宽一些 */
  min-width: 260px;
}

.item-code-group label,
.item-name-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #303133;
}

.item-code-group input,
.item-name-group input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
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

/* 材料表格样式 */
.materials-table-container {
  overflow-x: auto;
}

.materials-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 15px;
}

.materials-table th,
.materials-table td {
  border: 1px solid #dcdfe6;
  padding: 8px 12px;
  text-align: left;
  vertical-align: top;
}

.materials-table th {
  background-color: #f5f7fa;
  font-weight: bold;
  white-space: normal; /* 允许换行 */
  word-wrap: break-word;
}

/* 材料列宽度定义 - 根据您的要求调整 */
.material-col-detail {
  width: 18%; /* 材料明细长一些 */
}

.material-col-review {
  width: 18%; /* 审核要点长一些 */
}

.material-col-auto-approval {
  width: 10%; /* 智能秒批判断标准窄一些 */
}

.material-col-shared {
  width: 7%; /* 是否共享窄一些 */
}

.material-col-source {
  width: 10%; /* 材料来源一般 */
}

.material-col-processing {
  width: 15%; /* 办理方式说明短一点 */
}

.material-col-notification {
  width: 7%; /* 适用告知承诺短一点 */
}

.material-col-action {
  width: 15%; /* 操作列 */
}

.materials-table td input,
.materials-table td select,
.materials-table td textarea {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 13px;
  box-sizing: border-box;
}

.materials-table td textarea {
  min-height: 60px;
  resize: vertical;
}

.materials-table .remove-material-btn {
  background-color: #f56c6c;
  color: white;
  border: none;
  padding: 6px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  white-space: nowrap;
}

.materials-table .remove-material-btn:hover {
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

/* 材料明细自动完成样式 */
.material-detail-autocomplete-container {
  position: relative;
  width: 100%;
}

.material-detail-input-wrapper {
  position: relative;
}

.material-detail-input {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 13px;
  box-sizing: border-box;
}

.material-dropdown-list {
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
  z-index: 1000;
}

.material-dropdown-item {
  padding: 8px 12px;
  cursor: pointer;
}

.material-dropdown-item:hover {
  background-color: #f5f7fa;
}

/* 流程图选择样式 */
.process-diagrams-container {
  display: flex;
  gap: 20px;
}

.process-diagram-section {
  flex: 1;
}

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

/* 错误信息样式 */
.error-message {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 5px;
}

/* 添加输入框错误状态样式 */
.form-group input.error {
  border-color: #f56c6c;
}

@media (max-width: 768px) {
  .matter-detail-container {
    padding: 10px;
  }

  .form-row {
    flex-direction: column;
    gap: 0;
  }

  /* 在移动端恢复字段的默认宽度 */
  .form-group-id,
  .form-group-version,
  .form-group-status,
  .form-group-code-super-narrow {
    flex: 1 !important;
  }

  .basis-item,
  .material-item,
  .process-diagram-selection {
    flex-direction: column;
  }

  .process-diagrams-container {
    flex-direction: column;
    gap: 15px;
  }

  .form-actions {
    display: flex;
    justify-content: space-between;
  }

  .form-actions button {
    flex: 1;
    margin: 0 5px;
  }

  /* 移动端材料表格样式 */
  .materials-table-container {
    font-size: 12px;
  }

  .materials-table th,
  .materials-table td {
    padding: 6px 8px;
  }

  .materials-table td input,
  .materials-table td select,
  .materials-table td textarea {
    font-size: 12px;
    padding: 4px 6px;
  }

  /* 在移动端进一步调整列宽 */
  .material-col-detail {
    width: 18%;
  }

  .material-col-review {
    width: 18%;
  }

  .material-col-auto-approval {
    width: 10%;
  }

  .material-col-shared {
    width: 7%;
  }

  .material-col-source {
    width: 10%;
  }

  .material-col-processing {
    width: 15%;
  }

  .material-col-notification {
    width: 7%;
  }

  .material-col-action {
    width: 15%;
  }

  /* 移动端主项、子项、孙项信息行样式 */
  .item-info-row {
    flex-direction: column;
    gap: 15px;
  }

  .item-code-group,
  .item-name-group {
    flex: 1;
    min-width: auto;
  }

  .diagram-type-label {
    font-weight: bold;
    color: #303133;
    margin-bottom: 5px;
    display: block;
  }

  .readonly-field {
    display: block;
    padding: 6px 8px;
    min-height: 32px;
    font-size: 13px;
    line-height: 1.4;
    word-wrap: break-word;
    word-break: break-all;
    background-color: #f5f7fa;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    color: #606266;
  }

  /* 确保只读字段在表格中有合适的显示 */
  .materials-table td .readonly-field {
    background-color: #f5f7fa;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
  }

  .material-detail-input {
    display: block;
    padding: 6px 8px;
    min-height: 32px;
    font-size: 13px;
    line-height: 1.4;
    word-wrap: break-word;
    word-break: break-all;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    color: #606266;
    width: 100%;
    box-sizing: border-box;
  }

  .material-dropdown-list {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    background: white;
    border: 1px solid #dcdfe6;
    border-top: none;
    border-radius: 0 0 4px 4px;
    max-height: 150px;
    overflow-y: auto;
    z-index: 1000;
  }

  .material-dropdown-item {
    padding: 6px 10px;
    font-size: 12px;
  }
}
</style>
