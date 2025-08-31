<!-- src/components/material/MaterialList.vue -->
<template>
  <div class="material-list-container">
    <!-- 材料列表界面 -->
    <div v-if="!selectedMaterial">
      <div class="header">
        <h2>材料清单</h2>
        <div class="header-actions">
          <button class="refresh-btn" @click="fetchMaterials">刷新</button>
          <button class="add-btn" @click="showAddForm">新增材料</button>
        </div>
      </div>

      <div class="loading" v-if="loading">
        <p>正在加载材料数据...</p>
      </div>

      <div class="error" v-else-if="error">
        <p>加载失败: {{ error }}</p>
        <button @click="fetchMaterials">重试</button>
      </div>

      <div class="no-data" v-else-if="materials.length === 0">
        <p>暂无材料数据</p>
        <button class="add-btn" @click="showAddForm">新增第一个材料</button>
      </div>

      <!-- 材料表格 -->
      <div class="materials-table" v-else>
        <div class="table-header">
          <div class="table-cell">ID</div>
          <div class="table-cell">材料明细</div>
          <div class="table-cell">审核要点</div>
          <div class="table-cell">"智能秒批"判断标准</div>
          <div class="table-cell">是否共享</div>
          <div class="table-cell">材料来源</div>
          <div class="table-cell">办理方式及材料信息获取方式说明</div>
          <div class="table-cell">是否适用告知承诺</div>
          <div class="table-cell">是否有效</div>
          <div class="table-cell">操作</div>
        </div>

        <div
          class="table-row"
          v-for="material in materials"
          :key="material.id"
          @click="showMaterialDetail(material)"
        >
          <div class="table-cell">{{ material.id }}</div>
          <div class="table-cell material-name">
            {{ material.materialDetail || '-' }}
          </div>
          <div class="table-cell">{{ material.reviewPoint || '-' }}</div>
          <div class="table-cell">{{ material.autoApprovalCriteria || '-' }}</div>
          <div class="table-cell">{{ material.isShared ? '是' : '否' }}</div>
          <div class="table-cell">{{ getMaterialSourceText(material.materialSource) || '-' }}</div>
          <div class="table-cell">{{ material.processingMethodAndInfoAccess || '-' }}</div>
          <div class="table-cell">{{ material.isEligibleForPromise ? '是' : '否' }}</div>
          <div class="table-cell">
            <span :class="['status-badge', material.isValid ? 'status-active' : 'status-inactive']">
              {{ material.isValid ? '已上线' : '已下线' }}
            </span>
          </div>
          <div class="table-cell action-cell">
            <div class="action-buttons">
              <button
                v-if="material.isValid"
                class="offline-btn"
                @click.stop="toggleMaterialStatus(material.id, false)"
              >
                下线
              </button>
              <template v-else>
                <button
                  class="online-btn"
                  @click.stop="toggleMaterialStatus(material.id, true)"
                >
                  上线
                </button>
                <button
                  class="delete-btn"
                  @click.stop="deleteMaterial(material.id)"
                >
                  删除
                </button>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 材料详情界面 -->
    <MaterialDetail
      v-else
      :material="selectedMaterial"
      @back="goBackToList"
      @material-updated="handleMaterialUpdated"
    />

    <!-- 新增材料弹窗 -->
    <div class="modal" v-if="showMaterialForm" @click="closeForm">
      <div class="modal-content form-modal" @click.stop>
        <span class="close" @click="closeForm">&times;</span>
        <h3>新增材料</h3>
        <form @submit.prevent="saveMaterial">
          <div class="form-group">
            <label>材料明细 *</label>
            <input type="text" v-model="form.materialDetail" required>
          </div>

          <div class="form-group">
            <label>审核要点:</label>
            <textarea v-model="form.reviewPoint"></textarea>
          </div>

          <div class="form-group">
            <label>"智能秒批"判断标准:</label>
            <textarea v-model="form.autoApprovalCriteria"></textarea>
          </div>

          <div class="form-group">
            <label>是否共享:</label>
            <select v-model="form.isShared">
              <option :value="true">是</option>
              <option :value="false">否</option>
            </select>
          </div>

          <div class="form-group">
            <label>材料来源:</label>
            <select v-model="form.materialSource">
              <option value="PERSONAL_SUBMISSION">申请人自备</option>
              <option value="SYSTEM_AUTO_SHARED">系统自动获取</option>
            </select>
          </div>

          <div class="form-group">
            <label>办理方式及材料信息获取方式说明:</label>
            <input type="text" v-model="form.processingMethodAndInfoAccess">
          </div>

          <div class="form-group">
            <label>是否适用告知承诺:</label>
            <select v-model="form.isEligibleForPromise">
              <option :value="true">是</option>
              <option :value="false">否</option>
            </select>
          </div>

          <div class="form-group">
            <label>是否有效:</label>
            <select v-model="form.isValid">
              <option :value="true">已上线</option>
              <option :value="false">已下线</option>
            </select>
          </div>

          <div class="form-actions">
            <button
              type="button"
              @click="closeForm"
            >
              返回
            </button>
            <button
              type="submit"
              class="save-btn"
            >
              创建
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>// 引入MaterialDetail组件
import MaterialDetail from './MaterialDetail.vue';

// API端点常量
const API_BASE_URL = 'http://localhost:8000/api/materials';
const API_GET_ALL = `${API_BASE_URL}`;
const API_CREATE = `${API_BASE_URL}`;
const API_GET_BY_ID = (id) => `${API_BASE_URL}/${id}`;
const API_UPDATE = (id) => `${API_BASE_URL}/${id}`;
const API_DELETE = (id) => `${API_BASE_URL}/${id}`;
const API_ACTIVATE = (id) => `${API_BASE_URL}/${id}/activate`;
const API_DEACTIVATE = (id) => `${API_BASE_URL}/${id}/deactivate`;

export default {
  name: 'MaterialList',
  components: {
    MaterialDetail
  },
  data() {
    return {
      materials: [],
      loading: true,
      error: null,
      selectedMaterial: null,
      showMaterialForm: false,
      editingMaterial: null,
      form: {
        id: null,
        materialDetail: '',
        reviewPoint: '',
        autoApprovalCriteria: '',
        isShared: false,
        materialSource: 'PERSONAL_SUBMISSION',
        processingMethodAndInfoAccess: '',
        isEligibleForPromise: false,
        isValid: true
      }
    };
  },
  async mounted() {
    await this.fetchMaterials();
  },
  methods: {
    async fetchMaterials() {
      this.loading = true;
      this.error = null;

      try {
        const response = await fetch(API_GET_ALL);

        if (response.ok) {
          const contentType = response.headers.get('content-type');
          if (contentType && contentType.includes('application/json')) {
            this.materials = await response.json();
          } else {
            throw new Error('服务器返回的不是JSON格式数据');
          }
        } else {
          this.error = `HTTP Error: ${response.status} ${response.statusText}`;
        }
      } catch (error) {
        this.error = error.message || '网络错误';
        console.error('获取材料列表出错:', error);
      } finally {
        this.loading = false;
      }
    },

    // 获取材料来源文本
    getMaterialSourceText(source) {
      const sourceMap = {
        'PERSONAL_SUBMISSION': '申请人自备',
        'SYSTEM_AUTO_SHARED': '系统自动获取'
      };
      return sourceMap[source] || source;
    },

    // 显示材料详情
    showMaterialDetail(material) {
      this.selectedMaterial = material;
    },

    // 返回列表页
    goBackToList() {
      console.log('MaterialList: 接收到返回事件，当前selectedMaterial值:', this.selectedMaterial);
      this.selectedMaterial = null;
      console.log('MaterialList: 已将selectedMaterial设置为null');
    },

    // 处理材料更新事件
    handleMaterialUpdated(updatedMaterial) {
      console.log('接收到material-updated事件，更新的材料数据:', updatedMaterial);
      // 更新列表中的材料
      const index = this.materials.findIndex(m => m.id === updatedMaterial.id);
      if (index !== -1) {
        this.materials.splice(index, 1, updatedMaterial);
      }
      // 更新选中的材料
      this.selectedMaterial = updatedMaterial;
    },

    showAddForm() {
      this.editingMaterial = null;
      this.resetForm();
      this.showMaterialForm = true;
    },

    resetForm() {
      this.form = {
        id: null,
        materialDetail: '',
        reviewPoint: '',
        autoApprovalCriteria: '',
        isShared: false,
        materialSource: 'PERSONAL_SUBMISSION',
        processingMethodAndInfoAccess: '',
        isEligibleForPromise: false,
        isValid: true
      };
    },

    closeForm() {
      this.showMaterialForm = false;
      this.editingMaterial = null;
    },

    editMaterial(material) {
      this.editingMaterial = material;
      // 将选中的材料数据填充到表单中
      this.form = {
        id: material.id,
        materialDetail: material.materialDetail,
        reviewPoint: material.reviewPoint,
        autoApprovalCriteria: material.autoApprovalCriteria,
        isShared: material.isShared,
        materialSource: material.materialSource,
        processingMethodAndInfoAccess: material.processingMethodAndInfoAccess,
        isEligibleForPromise: material.isEligibleForPromise,
        isValid: material.isValid
      };
      this.showMaterialForm = true;
    },

    async saveMaterial() {
      try {
        let response;

        if (this.editingMaterial) {
          // 更新材料
          response = await fetch(API_UPDATE(this.form.id), {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify(this.form)
          });
        } else {
          // 新增材料
          response = await fetch(API_CREATE, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify(this.form)
          });
        }

        if (response.ok) {
          await this.fetchMaterials();
          this.closeForm();
          alert(this.editingMaterial ? '材料更新成功' : '材料创建成功');
        } else {
          const errorText = await response.text();
          alert((this.editingMaterial ? '更新' : '创建') + '失败: ' + response.status + ' - ' + errorText);
        }
      } catch (error) {
        console.error('保存材料出错:', error);
        alert('保存失败: ' + error.message);
      }
    },

    // 添加 deleteMaterial 方法
    async deleteMaterial(id) {
      if (!confirm('确定要删除这个材料吗？')) {
        return;
      }

      try {
        const response = await fetch(API_DELETE(id), {
          method: 'DELETE'
        });

        if (response.ok) {
          await this.fetchMaterials();
          // 如果正在查看被删除的材料，则返回列表
          if (this.selectedMaterial && this.selectedMaterial.id === id) {
            this.selectedMaterial = null;
          }
          alert('材料删除成功');
        } else {
          alert('删除失败: ' + response.status);
        }
      } catch (error) {
        console.error('删除材料出错:', error);
        alert('删除失败: ' + error.message);
      }
    },

    async toggleMaterialStatus(id, isValid) {
      try {
        let response;
        let action = isValid ? '上线' : '下线';

        if (isValid) {
          // 上线材料
          response = await fetch(API_ACTIVATE(id), {
            method: 'PUT'
          });
        } else {
          // 下线材料
          response = await fetch(API_DEACTIVATE(id), {
            method: 'PUT'
          });
        }

        if (response.ok) {
          await this.fetchMaterials();
          alert(`材料已${action}`);
        } else if (response.status === 404) {
          alert('材料不存在');
        } else {
          alert(`${action}失败: ${response.status}`);
        }
      } catch (error) {
        console.error(`更新材料状态出错:`, error);
        alert(`${action}失败: ${error.message}`);
      }
    },

    // 添加这个方法用于重置到列表视图
    resetToListView() {
      this.selectedMaterial = null;
      this.showMaterialForm = false;
    }
  }
}
</script>

<style scoped>
.material-list-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}

.header h2 {
  color: #303133;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.refresh-btn,
.add-btn {
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

.loading,
.error,
.no-data {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.error {
  color: #f56c6c;
}

.error button,
.no-data button {
  margin-top: 10px;
  background-color: #409eff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.materials-table {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
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
  cursor: pointer;
  transition: background-color 0.2s;
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

/* 特定列的宽度调整 */
.table-cell:nth-child(1) {
  /* ID列 */
  flex: 0 0 60px;
}

.table-cell:nth-child(2) {
  /* 材料明细列 */
  flex: 2;
}

.table-cell:nth-child(10) {
  /* 操作列 */
  flex: 0 0 120px;
}

.material-name {
  font-weight: 500;
  color: #409eff;
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
.action-cell {
  justify-content: flex-start;
}

.action-buttons {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.offline-btn,
.online-btn,
.delete-btn {
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

/* 响应式设计 */
@media (max-width: 768px) {
  .material-list-container {
    padding: 10px;
  }

  .header {
    flex-direction: column;
    align-items: stretch;
  }

  .header-actions {
    justify-content: center;
  }

  .table-header,
  .table-row {
    flex-wrap: wrap;
  }

  .table-cell {
    flex: 0 0 50%;
    padding: 8px 5px;
    border-bottom: 1px solid #eee;
  }

  .table-cell:nth-child(1),
  .table-cell:nth-child(2) {
    flex: 0 0 100%;
  }

  .table-cell:nth-child(10) {
    flex: 0 0 100%;
    justify-content: flex-start;
  }

  .action-buttons {
    flex-direction: row;
    justify-content: flex-start;
  }

  .modal-content {
    width: 95%;
    padding: 15px;
  }
}
</style>
