<template>
  <div class="matter-list-container">
    <div class="header">
      <h2>事项清单</h2>
      <div class="header-actions">
        <button class="refresh-btn" @click="fetchMatters">刷新</button>
        <button class="add-btn" @click="showAddForm">新增事项</button>
      </div>
    </div>

    <div class="loading" v-if="loading">
      <p>正在加载事项数据...</p>
    </div>

    <div class="error" v-else-if="error">
      <p>加载失败: {{ error }}</p>
      <button @click="fetchMatters">重试</button>
    </div>

    <div class="no-data" v-else-if="matters.length === 0">
      <p>暂无事项数据</p>
      <button class="add-btn" @click="showAddForm">新增第一个事项</button>
    </div>

    <div class="matters-table" v-else>
      <div class="table-header">
        <div class="table-cell">ID</div>
        <div class="table-cell">主项名称</div>
        <div class="table-cell">子项名称</div>
        <div class="table-cell">孙项名称</div>
        <div class="table-cell">法定时限</div>
        <div class="table-cell">承诺时限</div>
        <div class="table-cell">审批层级</div>
        <div class="table-cell">省厅对口指导处室</div>
        <div class="table-cell">版本</div>
        <div class="table-cell">发布状态</div>
        <div class="table-cell">状态</div>
        <div class="table-cell">操作</div>
      </div>

      <div
        class="table-row"
        v-for="matter in matters"
        :key="matter.id"
      >
        <div class="table-cell">{{ matter.id }}</div>
        <div class="table-cell matter-name" @click="editMatter(matter)">
          {{ formatMainItemName(matter.mainItemCode, matter.mainItemName) }}
        </div>
        <div class="table-cell">
          {{ formatSubItemName(matter.mainItemCode, matter.subItemCode, matter.subItemName) }}
        </div>
        <div class="table-cell">
          {{ formatGrandchildItemName(matter.mainItemCode, matter.subItemCode, matter.grandchildItemCode, matter.grandchildItemName) }}
        </div>
        <div class="table-cell">{{ matter.legalTimeLimit || '-' }}天</div>
        <div class="table-cell">{{ matter.committedTimeLimit || '-' }}天</div>
        <div class="table-cell">{{ getApprovalLevelDescription(matter.approvalLevel) || matter.approvalLevel || '-' }}</div>
        <div class="table-cell">{{ getProvincialDepartmentOfficeDescription(matter.provincialDepartmentOffice) || matter.provincialDepartmentOffice || '-' }}</div>
        <div class="table-cell">{{ matter.version || '-' }}</div>
        <div class="table-cell">
          <span :class="['status-badge', matter.isPublish ? 'status-active' : 'status-inactive']">
            {{ matter.isPublish ? '已发布' : '未发布' }}
          </span>
        </div>
        <div class="table-cell">
          <span :class="['status-badge', matter.isValid ? 'status-active' : 'status-inactive']">
            {{ matter.isValid ? '已上线' : '已下线' }}
          </span>
        </div>
        <div class="table-cell">
          <div class="action-buttons">
            <!-- 状态操作按钮 -->
            <template v-if="matter.isValid">
              <button
                class="offline-btn"
                @click.stop="toggleMatterStatus(matter.id, false)"
              >
                下线
              </button>
            </template>
            <template v-else>
              <button
                class="online-btn"
                @click.stop="toggleMatterStatus(matter.id, true)"
              >
                上线
              </button>
              <button
                class="delete-btn"
                @click.stop="deleteMatter(matter.id)"
              >
                删除
              </button>
            </template>

            <!-- 发布操作按钮 -->
            <template v-if="matter.isValid && !matter.isPublish">
              <button
                class="publish-btn"
                @click.stop="togglePublishStatus(matter.id, true)"
              >
                发布
              </button>
            </template>
            <template v-else-if="matter.isValid && matter.isPublish">
              <button
                class="unpublish-btn"
                @click.stop="togglePublishStatus(matter.id, false)"
              >
                取消发布
              </button>
            </template>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增/编辑事项弹窗 -->
    <div class="modal" v-if="showMatterForm" @click="closeForm">
      <div class="modal-content form-modal" @click.stop>
        <span class="close" @click="closeForm">&times;</span>
        <h3>{{ editingMatter ? '编辑事项' : '新增事项' }}</h3>
        <form @submit.prevent="saveMatter">
          <div class="form-group" v-if="editingMatter">
            <label>ID:</label>
            <input type="text" v-model="form.id" disabled>
          </div>

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

          <div class="form-group">
            <label>法定时限 (天) *</label>
            <input type="number" v-model.number="form.legalTimeLimit" required>
          </div>

          <div class="form-group">
            <label>承诺时限 (天) *</label>
            <input type="number" v-model.number="form.committedTimeLimit" required>
          </div>

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

          <!-- 版本、发布状态、状态字段移到最后 -->
          <div class="form-row">
            <div class="form-group">
              <label>版本:</label>
              <input type="text" v-model="form.version">
            </div>

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
            <!-- 当发布状态为true或状态为false时，只显示关闭按钮 -->
            <template v-if="editingMatter && (form.isPublish === true || form.isValid === false)">
              <button
                type="button"
                @click="closeForm"
              >
                关闭
              </button>
            </template>
            <!-- 其他情况显示取消和更新按钮 -->
            <template v-else>
              <button
                type="button"
                @click="closeForm"
              >
                取消
              </button>
              <button
                type="submit"
                class="save-btn"
              >
                {{ editingMatter ? '更新' : '创建' }}
              </button>
            </template>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
// API端点常量
const API_BASE_URL = 'http://localhost:8000/api/matters'
const API_MATERIALS_URL = 'http://localhost:8000/api/materials'
const API_GET_ALL = API_BASE_URL
const API_CREATE = API_BASE_URL
const API_UPDATE = (id) => `${API_BASE_URL}/${id}`
const API_DELETE = (id) => `${API_BASE_URL}/${id}`
const API_ACTIVATE = (id) => `${API_BASE_URL}/${id}/activate`
const API_DEACTIVATE = (id) => `${API_BASE_URL}/${id}/deactivate`
const API_PUBLISH = (id) => `${API_BASE_URL}/${id}/publish`
const API_UNPUBLISH = (id) => `${API_BASE_URL}/${id}/unpublish`

export default {
  name: 'MatterList',
  data() {
    return {
      matters: [],
      materialsList: [], // 存储所有材料列表
      approvalProcessDiagrams: [], // 存储所有审批流程图
      businessProcessDiagrams: [], // 存储所有业务流程图
      approvalDiagramSearchQuery: '', // 审批流程图搜索查询
      businessDiagramSearchQuery: '', // 业务流程图搜索查询
      approvalDiagramSearchResults: [], // 审批流程图搜索结果
      businessDiagramSearchResults: [], // 业务流程图搜索结果
      loading: true,
      error: null,
      selectedMatter: null,
      showMatterForm: false,
      editingMatter: null,
      materialSearchQueries: [], // 材料搜索查询
      materialSearchResults: [], // 材料搜索结果
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
  async mounted() {
    await this.fetchMatters()
    await this.fetchMaterials() // 获取材料列表
    await this.fetchProcessDiagrams() // 获取流程图列表
  },
  methods: {
    async fetchMatters() {
      this.loading = true
      this.error = null

      try {
        const response = await fetch(API_GET_ALL)

        if (response.ok) {
          const contentType = response.headers.get('content-type')
          if (contentType && contentType.includes('application/json')) {
            this.matters = await response.json()
          } else {
            throw new Error('服务器返回的不是JSON格式数据')
          }
        } else {
          this.error = `HTTP Error: ${response.status} ${response.statusText}`
        }
      } catch (error) {
        this.error = error.message || '网络错误'
        console.error('获取事项列表出错:', error)
      } finally {
        this.loading = false
      }
    },

    // 获取所有有效材料列表
    async fetchMaterials() {
      try {
        const response = await fetch(`${API_MATERIALS_URL}/search/valid?isValid=true`);
        if (response.ok) {
          this.materialsList = await response.json();
        }
      } catch (error) {
        console.error('获取材料列表出错:', error);
      }
    },

    // 获取所有有效流程图列表
    async fetchProcessDiagrams() {
      try {
        // 获取审批流程图
        const approvalResponse = await fetch('http://localhost:8000/api/process-diagrams/approval/search/valid?isValid=true');
        if (approvalResponse.ok) {
          this.approvalProcessDiagrams = await approvalResponse.json();
        }

        // 获取业务流程图
        const businessResponse = await fetch('http://localhost:8000/api/process-diagrams/business/search/valid?isValid=true');
        if (businessResponse.ok) {
          this.businessProcessDiagrams = await businessResponse.json();
        }
      } catch (error) {
        console.error('获取流程图列表出错:', error);
      }
    },

    // 获取审批层级描述
    getApprovalLevelDescription(approvalLevel) {
      if (!approvalLevel) return ''
      const level = this.approvalLevels.find(l => l.name === approvalLevel)
      return level ? level.description : ''
    },

    // 获取省厅对口指导处室描述
    getProvincialDepartmentOfficeDescription(office) {
      if (!office) return ''
      const dept = this.provincialDepartmentOffices.find(o => o.name === office)
      return dept ? dept.description : ''
    },

    // 格式化显示主项名称（编号 + 名称）
    formatMainItemName(code, name) {
      if (!code && !name) return '-';
      if (!code) return name;
      if (!name) return `${code}`;
      return `${code}.${name}`;
    },

    // 格式化显示子项名称（主项编号.子项编号.子项名称）
    formatSubItemName(mainCode, subCode, name) {
      if (!mainCode && !subCode && !name) return '-';
      if (!mainCode && !subCode) return name || '-';
      if (!name) return `${mainCode || ''}.${subCode || ''}`;
      return `${mainCode || ''}.${subCode || ''}.${name}`;
    },

    // 格式化显示孙项名称（主项编号.子项编号.孙项编号.孙项名称）
    formatGrandchildItemName(mainCode, subCode, grandchildCode, name) {
      if (!mainCode && !subCode && !grandchildCode && !name) return '-';
      if (!mainCode && !subCode && !grandchildCode) return name || '-';
      if (!name) return `${mainCode || ''}.${subCode || ''}.${grandchildCode || ''}`;
      return `${mainCode || ''}.${subCode || ''}.${grandchildCode || ''}.${name}`;
    },

    showAddForm() {
      this.editingMatter = null
      this.resetForm()
      this.showMatterForm = true
    },

    resetForm() {
      this.form = {
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
      }
      this.materialSearchQueries = []
      this.materialSearchResults = []
      this.approvalDiagramSearchQuery = ''
      this.businessDiagramSearchQuery = ''
      this.approvalDiagramSearchResults = []
      this.businessDiagramSearchResults = []
    },

    closeForm() {
      this.showMatterForm = false
      this.editingMatter = null
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
        this.approvalDiagramSearchResults = [];
        return;
      }

      // 过滤流程图列表
      const filtered = this.approvalProcessDiagrams.filter(diagram =>
        diagram.id.toString().includes(query) ||
        (diagram.imageName && diagram.imageName.includes(query))
      );

      this.approvalDiagramSearchResults = filtered;
    },

    // 审批流程图搜索框获得焦点时显示所有流程图
    onApprovalDiagramSearchFocus() {
      // 如果搜索框为空，显示所有流程图
      if (!this.approvalDiagramSearchQuery || this.approvalDiagramSearchQuery.trim() === '') {
        this.approvalDiagramSearchResults = [...this.approvalProcessDiagrams];
      }
    },

    // 选择审批流程图
    selectApprovalDiagram(diagram) {
      this.form.approvalProcessDiagramId = diagram.id;
      this.approvalDiagramSearchQuery = `${diagram.id} - ${diagram.imageName}`;
      this.approvalDiagramSearchResults = [];
    },

    // 清除审批流程图选择
    clearApprovalDiagram() {
      this.form.approvalProcessDiagramId = null;
      this.approvalDiagramSearchQuery = '';
      this.approvalDiagramSearchResults = [];
    },

    // 处理业务流程图搜索输入
    onBusinessDiagramSearchInput(query) {
      if (query.trim() === '') {
        this.businessDiagramSearchResults = [];
        return;
      }

      // 过滤流程图列表
      const filtered = this.businessProcessDiagrams.filter(diagram =>
        diagram.id.toString().includes(query) ||
        (diagram.imageName && diagram.imageName.includes(query))
      );

      this.businessDiagramSearchResults = filtered;
    },

    // 业务流程图搜索框获得焦点时显示所有流程图
    onBusinessDiagramSearchFocus() {
      // 如果搜索框为空，显示所有流程图
      if (!this.businessDiagramSearchQuery || this.businessDiagramSearchQuery.trim() === '') {
        this.businessDiagramSearchResults = [...this.businessProcessDiagrams];
      }
    },

    // 选择业务流程图
    selectBusinessDiagram(diagram) {
      this.form.businessProcessDiagramId = diagram.id;
      this.businessDiagramSearchQuery = `${diagram.id} - ${diagram.imageName}`;
      this.businessDiagramSearchResults = [];
    },

    // 清除业务流程图选择
    clearBusinessDiagram() {
      this.form.businessProcessDiagramId = null;
      this.businessDiagramSearchQuery = '';
      this.businessDiagramSearchResults = [];
    },

    editMatter(matter) {
      this.editingMatter = matter;
      // 将选中的事项数据填充到表单中
      this.form = {
        id: matter.id,
        version: matter.version,
        mainItemCode: matter.mainItemCode,
        subItemCode: matter.subItemCode,
        grandchildItemCode: matter.grandchildItemCode,
        mainItemName: matter.mainItemName || '',
        subItemName: matter.subItemName || '',
        grandchildItemName: matter.grandchildItemName || '',
        bases: [...(matter.bases || [])],
        materialIds: [...(matter.materialIds || [])],
        legalTimeLimit: matter.legalTimeLimit,
        committedTimeLimit: matter.committedTimeLimit,
        approvalLevel: matter.approvalLevel || '',
        provincialDepartmentOffice: matter.provincialDepartmentOffice || '',
        isValid: matter.isValid !== undefined ? matter.isValid : true,
        isPublish: matter.isPublish !== undefined ? matter.isPublish : false,
        approvalProcessDiagramId: matter.approvalProcessDiagramId || null,
        businessProcessDiagramId: matter.businessProcessDiagramId || null
      };

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
      });

      // 初始化流程图搜索查询
      this.approvalDiagramSearchQuery = '';
      this.businessDiagramSearchQuery = '';
      this.approvalDiagramSearchResults = [];
      this.businessDiagramSearchResults = [];

      // 设置审批流程图搜索查询文本
      if (this.form.approvalProcessDiagramId) {
        const diagram = this.approvalProcessDiagrams.find(d => d.id === this.form.approvalProcessDiagramId);
        if (diagram) {
          this.approvalDiagramSearchQuery = `${diagram.id} - ${diagram.imageName}`;
        }
      }

      // 设置业务流程图搜索查询文本
      if (this.form.businessProcessDiagramId) {
        const diagram = this.businessProcessDiagrams.find(d => d.id === this.form.businessProcessDiagramId);
        if (diagram) {
          this.businessDiagramSearchQuery = `${diagram.id} - ${diagram.imageName}`;
        }
      }

      this.showMatterForm = true;
    },

    async saveMatter() {
      try {
        // 验证并转换materialIds
        const validMaterialIds = this.form.materialIds
          .filter(id => id !== null && id !== undefined)
          .map(id => {
            const numId = typeof id === 'string' ? parseInt(id, 10) : id;
            return isNaN(numId) ? null : numId;
          })
          .filter(id => id !== null);

        // 验证流程图ID
        const validApprovalDiagramId = this.form.approvalProcessDiagramId ? 
          (typeof this.form.approvalProcessDiagramId === 'string' ? 
            parseInt(this.form.approvalProcessDiagramId, 10) : 
            this.form.approvalProcessDiagramId) : 
          null;
        
        const validBusinessDiagramId = this.form.businessProcessDiagramId ? 
          (typeof this.form.businessProcessDiagramId === 'string' ? 
            parseInt(this.form.businessProcessDiagramId, 10) : 
            this.form.businessProcessDiagramId) : 
          null;

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
        };

        // 验证必填字段
        if (!matterData.mainItemName || !matterData.subItemName || !matterData.grandchildItemName) {
          alert('主项名称、子项名称和孙项名称不能为空');
          return;
        }

        if (matterData.legalTimeLimit === null || matterData.committedTimeLimit === null) {
          alert('法定时限和承诺时限必须填写');
          return;
        }

        let response;
        let apiEndpoint;
        let method;

        if (this.editingMatter) {
          // 更新事项
          apiEndpoint = API_UPDATE(this.form.id);
          method = 'PUT';
        } else {
          // 新增事项
          apiEndpoint = API_CREATE;
          method = 'POST';
        }

        // 发送请求
        response = await fetch(apiEndpoint, {
          method: method,
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(matterData)
        });

        if (response.ok) {
          await this.fetchMatters();
          this.closeForm();
          alert(this.editingMatter ? '事项更新成功' : '事项创建成功');
        } else {
          const errorText = await response.text();
          console.error('Server error response:', errorText);
          
          let errorMessage = `HTTP ${response.status}: ${response.statusText}`;
          try {
            const errorJson = JSON.parse(errorText);
            if (errorJson.message) {
              errorMessage = errorJson.message;
            }
          } catch (e) {
            // 如果响应不是JSON格式，使用原始错误信息
            errorMessage = errorText;
          }
          
          alert((this.editingMatter ? '更新' : '创建') + '失败: ' + errorMessage);
        }
      } catch (error) {
        console.error('保存事项出错:', error);
        alert('保存失败: ' + error.message);
      }
    },

    async toggleMatterStatus(id, isValid) {
      try {
        let response;
        let action = isValid ? '上线' : '下线';

        if (isValid) {
          // 上线事项
          response = await fetch(API_ACTIVATE(id), {
            method: 'PUT'
          });
        } else {
          // 下线事项
          response = await fetch(API_DEACTIVATE(id), {
            method: 'PUT'
          });
        }

        if (response.ok) {
          await this.fetchMatters();
          alert(`事项已${action}`);
        } else if (response.status === 404) {
          alert('事项不存在');
        } else {
          alert(`${action}失败: ${response.status}`);
        }
      } catch (error) {
        console.error(`更新事项状态出错:`, error);
        alert(`${action}失败: ${error.message}`);
      }
    },

    async togglePublishStatus(id, isPublish) {
      try {
        let response;
        let action = isPublish ? '发布' : '取消发布';

        if (isPublish) {
          // 发布事项
          response = await fetch(API_PUBLISH(id), {
            method: 'PUT'
          });
        } else {
          // 取消发布事项
          response = await fetch(API_UNPUBLISH(id), {
            method: 'PUT'
          });
        }

        if (response.ok) {
          await this.fetchMatters();
          alert(`事项已${action}`);
        } else if (response.status === 404) {
          alert('事项不存在');
        } else {
          alert(`${action}失败: ${response.status}`);
        }
      } catch (error) {
        console.error(`更新事项发布状态出错:`, error);
        alert(`${action}失败: ${error.message}`);
      }
    },

    async deleteMatter(id) {
      if (!confirm('确定要删除这个事项吗？')) {
        return
      }

      try {
        const response = await fetch(API_DELETE(id), {
          method: 'DELETE'
        })

        if (response.ok) {
          await this.fetchMatters()
          alert('事项删除成功')
        } else {
          alert('删除失败: ' + response.status)
        }
      } catch (error) {
        console.error('删除事项出错:', error)
        alert('删除失败: ' + error.message)
      }
    },

    // 获取审批流程图URL
    getApprovalProcessDiagramUrl(id) {
      const diagram = this.approvalProcessDiagrams.find(d => d.id === id);
      return diagram ? diagram.imageDataUrl : '';
    },

    // 获取业务流程图URL
    getBusinessProcessDiagramUrl(id) {
      const diagram = this.businessProcessDiagrams.find(d => d.id === id);
      return diagram ? diagram.imageDataUrl : '';
    },
  },
  computed: {
    // 过滤审批流程图
    filteredApprovalProcessDiagrams() {
      if (!this.approvalDiagramSearch) {
        return this.approvalProcessDiagrams;
      }
      return this.approvalProcessDiagrams.filter(diagram => 
        diagram.imageName && diagram.imageName.includes(this.approvalDiagramSearch)
      );
    },
    
    // 过滤业务流程图
    filteredBusinessProcessDiagrams() {
      if (!this.businessDiagramSearch) {
        return this.businessProcessDiagrams;
      }
      return this.businessProcessDiagrams.filter(diagram => 
        diagram.imageName && diagram.imageName.includes(this.businessDiagramSearch)
      );
    }
  },
}
</script>

<style scoped>
.matter-list-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
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

.refresh-btn, .add-btn {
  background-color: #409eff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.add-btn {
  background-color: #67c23a;
}

.refresh-btn:hover {
  background-color: #66b1ff;
}

.add-btn:hover {
  background-color: #85ce61;
}

.loading, .error, .no-data {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.error {
  color: #f56c6c;
}

.error button, .no-data button {
  margin-top: 10px;
  background-color: #409eff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.matters-table {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.table-header {
  display: flex;
  background-color: #f5f7fa;
  font-weight: bold;
  border-bottom: 2px solid #dcdfe6;
}

.table-row {
  display: flex;
  border-bottom: 1px solid #dcdfe6;
}

.table-row:hover {
  background-color: #f5f7fa;
}

.table-row:last-child {
  border-bottom: none;
}

.table-cell {
  flex: 1;
  padding: 12px 10px;
  word-break: break-word;
  font-size: 14px;
  color: #606266;
  min-width: 0;
  display: flex;
  align-items: center;
}

.table-cell:first-child {
  flex: 0 0 60px;
}

/* 主项名称可点击样式 */
.matter-name {
  cursor: pointer;
  color: #409eff;
  font-weight: 500;
}

.matter-name:hover {
  color: #66b1ff;
  text-decoration: underline;
}

/* 状态标签样式 */
.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.status-active {
  background-color: #f0f9eb;
  color: #67c23a;
  border: 1px solid #c2e7b0;
}

.status-inactive {
  background-color: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fbc4c4;
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.offline-btn, .online-btn, .delete-btn, .publish-btn, .unpublish-btn {
  padding: 4px 8px;
  border-radius: 3px;
  cursor: pointer;
  font-size: 12px;
  border: none;
  white-space: nowrap;
}

.offline-btn {
  background-color: #e6a23c;
  color: white;
}

.offline-btn:hover {
  background-color: #ebb563;
}

.online-btn {
  background-color: #67c23a;
  color: white;
}

.online-btn:hover {
  background-color: #85ce61;
}

.delete-btn {
  background-color: #f56c6c;
  color: white;
}

.delete-btn:hover {
  background-color: #f78989;
}

.publish-btn {
  background-color: #409eff;
  color: white;
}

.publish-btn:hover {
  background-color: #66b1ff;
}

.unpublish-btn {
  background-color: #909399;
  color: white;
}

.unpublish-btn:hover {
  background-color: #a6a9ad;
}

/* 弹窗样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  padding: 20px;
  border-radius: 4px;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
  position: relative;
}

.form-modal {
  max-width: 600px;
}

.close {
  position: absolute;
  top: 10px;
  right: 15px;
  font-size: 24px;
  cursor: pointer;
  color: #909399;
}

.close:hover {
  color: #303133;
}

.matter-detail p {
  margin: 10px 0;
  line-height: 1.5;
}

.process-diagrams h4 {
  margin: 15px 0 5px 0;
}

.process-diagram-image {
  max-width: 100%;
  height: auto;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.modal-actions {
  margin-top: 20px;
  text-align: right;
}

.modal-actions button {
  margin-left: 10px;
  background-color: #409eff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.modal-actions button:hover {
  background-color: #66b1ff;
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
.image-upload-container {
  width: 100%;
}

.image-preview {
  margin-top: 10px;
}

.image-preview img {
  max-width: 100%;
  max-height: 200px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.search-box {
  margin: 10px 0;
}

.search-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-actions {
  margin-top: 20px;
  text-align: right;
}

.form-actions button {
  margin-left: 10px;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.form-actions button[type="button"] {
  background-color: #909399;
  color: white;
  border: none;
}

.form-actions button[type="button"]:hover {
  background-color: #a6a9ad;
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
  .matters-table {
    font-size: 12px;
  }

  .table-cell {
    padding: 8px 5px;
  }

  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .header-actions {
    width: 100%;
    justify-content: space-between;
  }

  .action-buttons {
    flex-direction: column;
    gap: 3px;
  }

  .basis-item,
  .material-item,
  .process-diagram-selection {
    flex-direction: column;
  }

  .form-row {
    flex-direction: column;
    gap: 0;
  }
}
</style>
</file>