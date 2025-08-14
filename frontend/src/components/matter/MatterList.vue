<!-- src/components/matter/MatterList.vue -->
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
          {{ matter.mainItemName || '-' }}
        </div>
        <div class="table-cell">{{ matter.subItemName || '-' }}</div>
        <div class="table-cell">{{ matter.grandchildItemName || '-' }}</div>
        <div class="table-cell">{{ matter.legalTimeLimit || '-' }}天</div>
        <div class="table-cell">{{ matter.committedTimeLimit || '-' }}天</div>
        <div class="table-cell">{{ getApprovalLevelDescription(matter.approvalLevel) || matter.approvalLevel || '-' }}</div>
        <div class="table-cell">{{ getProvincialDepartmentOfficeDescription(matter.provincialDepartmentOffice) || matter.provincialDepartmentOffice || '-' }}</div>
        <div class="table-cell">
          <span :class="['status-badge', matter.isValid ? 'status-active' : 'status-inactive']">
            {{ matter.isValid ? '已上线' : '已下线' }}
          </span>
        </div>
        <div class="table-cell">
          <div class="action-buttons">
            <button
              v-if="matter.isValid"
              class="offline-btn"
              @click.stop="toggleMatterStatus(matter.id, false)"
            >
              下线
            </button>
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
          </div>
        </div>
      </div>
    </div>

    <!-- 事项详情弹窗 -->
    <div class="modal" v-if="selectedMatter" @click="closeModal">
      <div class="modal-content" @click.stop>
        <span class="close" @click="closeModal">&times;</span>
        <h3>事项详情</h3>
        <div class="matter-detail">
          <p><strong>ID:</strong> {{ selectedMatter.id }}</p>
          <p><strong>主项名称:</strong> {{ selectedMatter.mainItemName || '无' }}</p>
          <p><strong>子项名称:</strong> {{ selectedMatter.subItemName || '无' }}</p>
          <p><strong>孙项名称:</strong> {{ selectedMatter.grandchildItemName || '无' }}</p>
          <p><strong>经办依据:</strong>
            <span v-if="selectedMatter.basisList && selectedMatter.basisList.length > 0">
              {{ selectedMatter.basisList.join(', ') }}
            </span>
            <span v-else>无</span>
          </p>
          <p><strong>材料ID列表:</strong>
            <span v-if="selectedMatter.materialIds && selectedMatter.materialIds.length > 0">
              {{ selectedMatter.materialIds.join(', ') }}
            </span>
            <span v-else>无</span>
          </p>
          <p><strong>法定时限:</strong> {{ selectedMatter.legalTimeLimit || '未设置' }}天</p>
          <p><strong>承诺时限:</strong> {{ selectedMatter.committedTimeLimit || '未设置' }}天</p>
          <p><strong>审批层级:</strong> {{ getApprovalLevelDescription(selectedMatter.approvalLevel) || selectedMatter.approvalLevel || '未设置' }}</p>
          <p><strong>省厅对口指导处室:</strong> {{ getProvincialDepartmentOfficeDescription(selectedMatter.provincialDepartmentOffice) || selectedMatter.provincialDepartmentOffice || '未设置' }}</p>
          <p><strong>状态:</strong>
            <span :class="['status-badge', selectedMatter.isValid ? 'status-active' : 'status-inactive']">
              {{ selectedMatter.isValid ? '已上线' : '已下线' }}
            </span>
          </p>
        </div>
        <div class="modal-actions">
          <button @click="editMatter(selectedMatter)">编辑</button>
          <button @click="closeModal">关闭</button>
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

          <div class="form-group">
            <label>主项名称 *</label>
            <input type="text" v-model="form.mainItemName" required>
          </div>

          <div class="form-group">
            <label>子项名称 *</label>
            <input type="text" v-model="form.subItemName" required>
          </div>

          <div class="form-group">
            <label>孙项名称 *</label>
            <input type="text" v-model="form.grandchildItemName" required>
          </div>

          <div class="form-group">
            <label>经办依据:</label>
            <div class="basis-list-container">
              <div
                class="basis-item"
                v-for="(basis, index) in form.basisList"
                :key="index"
              >
                <input
                  type="text"
                  v-model="form.basisList[index]"
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
                <select v-model="form.materialIds[index]">
                  <option value="">请选择材料</option>
                  <option
                    v-for="material in materialsList"
                    :key="material.id"
                    :value="material.id"
                  >
                    {{ material.id }} - {{ material.materialDetails }}
                  </option>
                </select>
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
            <input type="number" v-model="form.legalTimeLimit" required>
          </div>

          <div class="form-group">
            <label>承诺时限 (天) *</label>
            <input type="number" v-model="form.committedTimeLimit" required>
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

          <div class="form-group">
            <label>状态:</label>
            <select v-model="form.isValid">
              <option :value="true">已上线</option>
              <option :value="false">已下线</option>
            </select>
          </div>

          <div class="form-actions">
            <button type="button" @click="closeForm">取消</button>
            <button type="submit" class="save-btn">{{ editingMatter ? '更新' : '创建' }}</button>
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

export default {
  name: 'MatterList',
  data() {
    return {
      matters: [],
      materialsList: [], // 存储所有材料列表
      loading: true,
      error: null,
      selectedMatter: null,
      showMatterForm: false,
      editingMatter: null,
      form: {
        id: null,
        mainItemName: '',
        subItemName: '',
        grandchildItemName: '',
        basisList: [],
        materialIds: [],
        legalTimeLimit: null,
        committedTimeLimit: null,
        approvalLevel: '',
        provincialDepartmentOffice: '',
        isValid: true
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

    // 获取所有材料列表
    async fetchMaterials() {
      try {
        const response = await fetch(API_MATERIALS_URL)
        if (response.ok) {
          this.materialsList = await response.json()
        }
      } catch (error) {
        console.error('获取材料列表出错:', error)
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

    viewMatterDetail(matter) {
      this.selectedMatter = matter
    },

    closeModal() {
      this.selectedMatter = null
    },

    showAddForm() {
      this.editingMatter = null
      this.resetForm()
      this.showMatterForm = true
    },

    resetForm() {
      this.form = {
        id: null,
        mainItemName: '',
        subItemName: '',
        grandchildItemName: '',
        basisList: [],
        materialIds: [],
        legalTimeLimit: null,
        committedTimeLimit: null,
        approvalLevel: '',
        provincialDepartmentOffice: '',
        isValid: true
      }
    },

    closeForm() {
      this.showMatterForm = false
      this.editingMatter = null
    },

    addBasis() {
      this.form.basisList.push('')
    },

    removeBasis(index) {
      this.form.basisList.splice(index, 1)
    },

    addMaterial() {
      this.form.materialIds.push('')
    },

    removeMaterial(index) {
      this.form.materialIds.splice(index, 1)
    },

    editMatter(matter) {
      this.editingMatter = matter
      // 将选中的事项数据填充到表单中
      this.form = {
        id: matter.id,
        mainItemName: matter.mainItemName || '',
        subItemName: matter.subItemName || '',
        grandchildItemName: matter.grandchildItemName || '',
        basisList: [...(matter.basisList || [])],
        materialIds: [...(matter.materialIds || [])],
        legalTimeLimit: matter.legalTimeLimit,
        committedTimeLimit: matter.committedTimeLimit,
        approvalLevel: matter.approvalLevel || '',
        provincialDepartmentOffice: matter.provincialDepartmentOffice || '',
        isValid: matter.isValid !== undefined ? matter.isValid : true
      }

      this.showMatterForm = true
    },

    async saveMatter() {
      try {
        // 确保 materialIds 是正确的格式
        const validMaterialIds = this.form.materialIds
          .filter(id => id !== null && id !== undefined && id !== '')
          .map(id => {
            // 确保ID是数字类型
            return typeof id === 'string' ? parseInt(id, 10) : id;
          })
          .filter(id => !isNaN(id));

        // 创建要发送的数据对象
        const matterToSave = {
          id: this.form.id,
          mainItemName: this.form.mainItemName,
          subItemName: this.form.subItemName,
          grandchildItemName: this.form.grandchildItemName,
          basisList: this.form.basisList.filter(basis => basis !== ''),
          materialIds: validMaterialIds,  // 确保这是数字数组
          legalTimeLimit: this.form.legalTimeLimit ? parseInt(this.form.legalTimeLimit) : null,
          committedTimeLimit: this.form.committedTimeLimit ? parseInt(this.form.committedTimeLimit) : null,
          approvalLevel: this.form.approvalLevel,
          provincialDepartmentOffice: this.form.provincialDepartmentOffice,
          isValid: this.form.isValid
        };

        console.log('Sending matter data:', JSON.stringify(matterToSave, null, 2));

        let response;

        if (this.editingMatter) {
          // 更新事项
          response = await fetch(API_UPDATE(this.form.id), {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify(matterToSave)
          });
        } else {
          // 新增事项
          response = await fetch(API_CREATE, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify(matterToSave)
          });
        }

        if (response.ok) {
          await this.fetchMatters();
          this.closeForm();
          alert(this.editingMatter ? '事项更新成功' : '事项创建成功');
        } else {
          const errorText = await response.text();
          console.error('Server error response:', errorText);
          alert((this.editingMatter ? '更新' : '创建') + '失败: ' + response.status + ' - ' + errorText);
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
    }
  }
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

.offline-btn, .online-btn, .delete-btn {
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

.material-item select {
  flex: 1;
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
  .material-item {
    flex-direction: column;
  }
}
</style>
