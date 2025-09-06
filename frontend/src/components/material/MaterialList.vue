<!-- src/components/material/MaterialList.vue -->
<template>
  <div class="material-list-container">
    <!-- 材料列表界面 -->
    <div v-if="!selectedMaterial && !showAddForm">
      <div class="header">
        <h2>材料清单</h2>
        <div class="header-actions">
          <button class="refresh-btn" @click="fetchMaterials">刷新</button>
          <button class="add-btn" @click="showAddMaterialForm">新增材料</button>
        </div>
      </div>

      <div class="filter-section">
        <div class="filter-group">
          <label>状态筛选:</label>
          <select v-model="filterStatus" @change="filterMaterials">
            <option value="all">全部</option>
            <option value="active">已上线</option>
            <option value="inactive">已下线</option>
          </select>
        </div>
        <div class="search-group">
          <input
            type="text"
            v-model="searchKeyword"
            placeholder="搜索材料明细..."
            @input="filterMaterials"
          >
        </div>
      </div>

      <div class="loading" v-if="loading">
        <p>正在加载材料数据...</p>
      </div>

      <div class="error" v-else-if="error">
        <p>加载失败: {{ error }}</p>
        <button @click="fetchMaterials">重试</button>
      </div>

      <div class="no-data" v-else-if="filteredMaterials.length === 0">
        <p>暂无材料数据</p>
        <button class="add-btn" @click="showAddMaterialForm">新增第一个材料</button>
      </div>

      <!-- 材料表格 -->
      <div class="materials-table" v-else>
        <div class="table-header">
          <div class="table-cell">ID</div>
          <div class="table-cell">材料明细</div>
          <div class="table-cell">版本</div>
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
          v-for="material in filteredMaterials"
          :key="material.id"
        >
          <div class="table-cell">{{ material.id }}</div>
          <div class="table-cell material-name" @click="viewMaterial(material)">
            {{ material.__name__ || '-' }}
          </div>
          <div class="table-cell">{{ material.version || '-' }}</div>
          <div class="table-cell">{{ material.reviewPoint || '-' }}</div>
          <div class="table-cell">{{ material.autoApprovalCriteria || '-' }}</div>
          <div class="table-cell">{{ material.shared ? '是' : '否' }}</div>
          <div class="table-cell">{{ getMaterialSourceText(material.materialSource) || '-' }}</div>
          <div class="table-cell">{{ getProcessingMethodText(material.processingMethodAndInfoAccess) || '-' }}</div>
          <div class="table-cell">{{ material.eligibleForPromise ? '是' : '否' }}</div>
          <div class="table-cell">
            <span :class="['status-badge', material.valid ? 'status-active' : 'status-inactive']">
              {{ material.valid ? '已上线' : '已下线' }}
            </span>
          </div>
          <div class="table-cell action-cell">
            <div class="action-buttons">
              <button
                v-if="material.valid"
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
      v-else-if="selectedMaterial && !showAddForm"
      :material="selectedMaterial"
      @back="goBackToList"
      @material-updated="handleMaterialUpdated"
    />

    <!-- 新增/编辑材料表单 -->
    <div class="material-form-container" v-else-if="showAddForm">
      <MaterialDetail
        :material="editingMaterial || {}"
        @back="hideMaterialForm"
        @material-updated="handleMaterialSaved"
      />
    </div>
  </div>
</template>

<script>
// 引入MaterialDetail组件
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
      filteredMaterials: [],
      loading: true,
      error: null,
      selectedMaterial: null,
      showAddForm: false,
      editingMaterial: null,
      filterStatus: 'all',
      searchKeyword: ''
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
            this.filterMaterials();
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

    filterMaterials() {
      let result = [...this.materials];

      // 状态筛选
      if (this.filterStatus !== 'all') {
        const isValid = this.filterStatus === 'active';
        result = result.filter(material => material.valid === isValid);
      }

      // 关键词搜索
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase();
        result = result.filter(material =>
          material.materialDetail && material.materialDetail.toLowerCase().includes(keyword)
        );
      }

      this.filteredMaterials = result;
    },

    // 获取材料来源文本
    getMaterialSourceText(source) {
      const sourceMap = {
        'PERSONAL_SUBMISSION': '申请人自备',
        'SYSTEM_AUTO_SHARED': '系统自动获取',
        'WANG_SHAN_PROCESSING': '网上办理'
      };
      return sourceMap[source] || source;
    },

    // 获取办理方式文本
    getProcessingMethodText(method) {
      const methodMap = {
        'ONLINE_PROCESSING': '网上办理，线上提交材料',
        'SYSTEM_AUTO_WITH_FALLBACK': '系统自动获取，如数据不全则需申请者提交',
        'PAPER_CERTIFICATE': '纸质证书需申请者提交'
      };
      return methodMap[method] || method;
    },

    // 查看材料详情
    viewMaterial(material) {
      this.selectedMaterial = material;
    },

    // 返回列表页
    goBackToList() {
      this.selectedMaterial = null;
    },

    // 处理材料更新事件
    handleMaterialUpdated(updatedMaterial) {
      // 更新列表中的材料
      const index = this.materials.findIndex(m => m.id === updatedMaterial.id);
      if (index !== -1) {
        this.materials.splice(index, 1, updatedMaterial);
      }

      // 更新selectedMaterial为最新的数据，保持在详情页面
      this.selectedMaterial = updatedMaterial;

      this.filterMaterials();
      // 不再自动返回列表页面，保持在详情页面
    },

    // 显示新增材料表单
    showAddMaterialForm() {
      this.editingMaterial = null;
      this.showAddForm = true;
    },

    // 显示编辑材料表单
    editMaterial(material) {
      this.editingMaterial = material;
      this.showAddForm = true;
    },

    // 隐藏材料表单
    hideMaterialForm() {
      this.showAddForm = false;
      this.editingMaterial = null;
    },

    // 处理材料保存事件
    handleMaterialSaved(savedMaterial) {
      // 如果是新增材料，添加到列表中
      if (!this.editingMaterial) {
        this.materials.push(savedMaterial);
      } else {
        // 如果是编辑材料，更新列表中的材料
        const index = this.materials.findIndex(m => m.id === savedMaterial.id);
        if (index !== -1) {
          this.materials.splice(index, 1, savedMaterial);
        }
      }

      // 隐藏表单并刷新显示
      this.hideMaterialForm();
      this.filterMaterials();
      alert('材料保存成功');
    },

    // 删除材料
    async deleteMaterial(id) {
      if (!confirm('确定要删除这个材料吗？')) {
        return;
      }

      try {
        const response = await fetch(API_DELETE(id), {
          method: 'DELETE'
        });

        if (response.ok) {
          // 从列表中移除材料
          this.materials = this.materials.filter(material => material.id !== id);
          this.filterMaterials();

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

    // 切换材料状态（上线/下线）
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
          // 更新材料状态
          const index = this.materials.findIndex(m => m.id === id);
          if (index !== -1) {
            this.materials[index].valid = isValid;

            // 如果正在查看这个材料，也更新selectedMaterial中的状态
            if (this.selectedMaterial && this.selectedMaterial.id === id) {
              this.selectedMaterial.valid = isValid;
            }
          }
          this.filterMaterials();
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

    // 重置到列表视图的方法（供父组件调用）
    resetToListView() {
      this.selectedMaterial = null;
      this.showAddForm = false;
      this.editingMaterial = null;
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

.filter-section {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-group,
.search-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-group label {
  font-weight: bold;
  color: #303133;
}

.filter-group select,
.search-group input {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
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
  /* 材料明细列 - 更长 */
  flex: 3;
  cursor: pointer;
  color: #409eff;
}

.table-cell:nth-child(3) {
  /* 版本列 */
  flex: 0 0 60px;
}

.table-cell:nth-child(4) {
  /* 审核要点列 */
  flex: 1.5;
}

.table-cell:nth-child(5) {
  /* "智能秒批"判断标准列 */
  flex: 1.5;
}

.table-cell:nth-child(6) {
  /* 是否共享列 */
  flex: 0 0 80px;
}

.table-cell:nth-child(7) {
  /* 材料来源列 */
  flex: 1.2;
}

.table-cell:nth-child(8) {
  /* 办理方式列 */
  flex: 1.5;
}

.table-cell:nth-child(9) {
  /* 是否适用告知承诺列 - 更短 */
  flex: 0 0 60px;
}

.table-cell:nth-child(10) {
  /* 是否有效列 */
  flex: 0 0 80px;
}

.table-cell:nth-child(11) {
  /* 操作列 */
  flex: 0 0 150px;
}

.material-name {
  font-weight: 500;
}

.table-cell:nth-child(2):hover {
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

.material-form-container {
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
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

  .filter-section {
    flex-direction: column;
    gap: 10px;
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

  .table-cell:nth-child(11) {
    flex: 0 0 100%;
    justify-content: flex-start;
  }

  .action-buttons {
    flex-direction: row;
    justify-content: flex-start;
  }
}
</style>
