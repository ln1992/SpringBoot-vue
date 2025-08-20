<!-- src/components/material/MaterialList.vue -->
<template>
  <div class="material-list-container">
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

    <div class="materials-table" v-else>
      <div class="table-header">
        <div class="table-cell">ID</div>
        <div class="table-cell">材料明细</div>
        <div class="table-cell">审核点</div>
        <div class="table-cell">自动审批标准</div>
        <div class="table-cell">共享</div>
        <div class="table-cell">材料来源</div>
        <div class="table-cell">处理方式</div>
        <div class="table-cell">承诺资格</div>
        <div class="table-cell">状态</div>
        <div class="table-cell">操作</div>
      </div>

      <div
        class="table-row"
        v-for="material in materials"
        :key="material.id"
      >
        <div class="table-cell">{{ material.id }}</div>
        <div class="table-cell material-name" @click="editMaterial(material)">
          {{ material.materialDetail || '-' }}
        </div>
        <div class="table-cell">{{ material.reviewPoint || '-' }}</div>
        <div class="table-cell">{{ material.autoApprovalCriteria || '-' }}</div>
        <div class="table-cell">{{ material.isShared ? '是' : '否' }}</div>
        <div class="table-cell">{{ material.materialSource || '-' }}</div>
        <div class="table-cell">{{ material.processingMethodAndInfoAccess || '-' }}</div>
        <div class="table-cell">{{ material.isEligibleForPromise ? '是' : '否' }}</div>
        <div class="table-cell">
          <span :class="['status-badge', material.isValid ? 'status-active' : 'status-inactive']">
            {{ material.isValid ? '已上线' : '已下线' }}
          </span>
        </div>
        <div class="table-cell">
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

    <!-- 材料详情弹窗 -->
    <div class="modal" v-if="selectedMaterial" @click="closeModal">
      <div class="modal-content" @click.stop>
        <span class="close" @click="closeModal">&times;</span>
        <h3>材料详情</h3>
        <div class="material-detail">
          <p><strong>ID:</strong> {{ selectedMaterial.id }}</p>
          <p><strong>材料明细:</strong> {{ selectedMaterial.materialDetail || '无' }}</p>
          <p><strong>审核点:</strong> {{ selectedMaterial.reviewPoint || '无' }}</p>
          <p><strong>自动审批标准:</strong> {{ selectedMaterial.autoApprovalCriteria || '无' }}</p>
          <p><strong>是否共享:</strong> {{ selectedMaterial.isShared ? '是' : '否' }}</p>
          <p><strong>材料来源:</strong> {{ selectedMaterial.materialSource || '未指定' }}</p>
          <p><strong>处理方式:</strong> {{ selectedMaterial.processingMethodAndInfoAccess || '未指定' }}</p>
          <p><strong>承诺资格:</strong> {{ selectedMaterial.isEligibleForPromise ? '是' : '否' }}</p>
          <p><strong>状态:</strong>
            <span :class="['status-badge', selectedMaterial.isValid ? 'status-active' : 'status-inactive']">
              {{ selectedMaterial.isValid ? '已上线' : '已下线' }}
            </span>
          </p>
        </div>
        <div class="modal-actions">
          <button @click="editMaterial(selectedMaterial)">编辑</button>
          <button @click="closeModal">关闭</button>
        </div>
      </div>
    </div>

    <!-- 新增/编辑材料弹窗 -->
    <div class="modal" v-if="showMaterialForm" @click="closeForm">
      <div class="modal-content form-modal" @click.stop>
        <span class="close" @click="closeForm">&times;</span>
        <h3>{{ editingMaterial ? '编辑材料' : '新增材料' }}</h3>
        <form @submit.prevent="saveMaterial">
          <div class="form-group" v-if="editingMaterial">
            <label>ID:</label>
            <input type="text" v-model="form.id" disabled>
          </div>

          <div class="form-group">
            <label>材料明细 *</label>
            <input type="text" v-model="form.materialDetail" required>
          </div>

          <div class="form-group">
            <label>审核点:</label>
            <textarea v-model="form.reviewPoint"></textarea>
          </div>

          <div class="form-group">
            <label>自动审批标准:</label>
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
              <option value="PERSONAL_SUBMISSION">个人提交网上办理</option>
              <option value="SYSTEM_AUTO_SHARED">系统自动获取</option>
            </select>
          </div>

          <div class="form-group">
            <label>处理方式:</label>
            <input type="text" v-model="form.processingMethodAndInfoAccess">
          </div>

          <div class="form-group">
            <label>承诺资格:</label>
            <select v-model="form.isEligibleForPromise">
              <option :value="true">是</option>
              <option :value="false">否</option>
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
            <button type="submit" class="save-btn">{{ editingMaterial ? '更新' : '创建' }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
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

    viewMaterialDetail(material) {
      this.selectedMaterial = material;
    },

    closeModal() {
      this.selectedMaterial = null;
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
          alert('材料删除成功');
        } else {
          alert('删除失败: ' + response.status);
        }
      } catch (error) {
        console.error('删除材料出错:', error);
        alert('删除失败: ' + error.message);
      }
    }
  }
};
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

.materials-table {
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

.table-cell:nth-child(2) {
  flex: 2;
  cursor: pointer;
  color: #409eff;
  font-weight: 500;
}

.table-cell:nth-child(2):hover {
  color: #66b1ff;
  text-decoration: underline;
}

.table-cell:nth-child(3),
.table-cell:nth-child(4) {
  flex: 1.5;
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

.material-detail p {
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
  .materials-table {
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
}
</style>
