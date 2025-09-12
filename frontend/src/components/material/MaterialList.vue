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

      <div class="no-data" v-else-if="paginatedMaterials.length === 0">
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
          v-for="material in paginatedMaterials"
          :key="material.id"
          @click="viewMaterial(material)"
        >
          <div class="table-cell">{{ material.id }}</div>
          <div class="table-cell material-name">
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
                :disabled="false"
              >
                下线
              </button>
              <template v-else>
                <button
                  class="online-btn"
                  @click.stop="toggleMaterialStatus(material.id, true)"
                  :disabled="false"
                >
                  上线
                </button>
                <button
                  class="delete-btn"
                  @click.stop="deleteMaterial(material.id)"
                  :disabled="material.valid"
                  :title="material.valid ? '请先下线材料再删除' : ''"
                >
                  删除
                </button>
              </template>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页控件 -->
      <div class="pagination" v-if="paginatedMaterials.length > 0">
        <div class="pagination-controls">
          <button
            :disabled="currentPage === 1"
            @click.stop="currentPage > 1 && (currentPage--)">
            上一页
          </button>
          <span>第 {{ currentPage }} 页，共 {{ totalPages }} 页 (总计 {{ filteredMaterials.length }} 条)</span>
          <button
            :disabled="currentPage === totalPages"
            @click.stop="currentPage < totalPages && (currentPage++)">
            下一页
          </button>
        </div>
        <div class="page-size-selector">
          <label>每页显示:</label>
          <select v-model="pageSize" @change="handlePageSizeChange">
            <option value="5">5</option>
            <option value="10">10</option>
            <option value="20">20</option>
            <option value="50">50</option>
          </select>
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
        :material="editingMaterial || undefined"
        @back="hideMaterialForm"
        @material-updated="handleMaterialSaved"
      />
    </div>
  </div>
</template>

<script>
// 引入MaterialDetail组件
import MaterialDetail from './MaterialDetail.vue';
import { materialService } from '../../api';

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
      searchKeyword: '',
      currentPage: 1,
      pageSize: 10 // 每页显示10条记录
    };
  },
  computed: {
    totalPages() {
      return Math.ceil(this.filteredMaterials.length / this.pageSize);
    },
    paginatedMaterials() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.filteredMaterials.slice(start, end);
    }
  },
  async mounted() {
    await this.fetchMaterials();
  },
  methods: {
    async fetchMaterials() {
      this.loading = true;
      this.error = null;
      this.currentPage = 1;

      try {
        this.materials = await materialService.getAllMaterials();
        this.filterMaterials();
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
          (material.materialDetail && material.materialDetail.toLowerCase().includes(keyword)) ||
          (material.__name__ && material.__name__.toLowerCase().includes(keyword))
        );
      }

      this.filteredMaterials = result;
      // 重置到第一页
      this.currentPage = 1;
    },

    handlePageSizeChange() {
      // 当页面大小改变时，重置到第一页
      this.currentPage = 1;
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

    viewMaterial(material) {
      this.selectedMaterial = material;
    },

    goBackToList() {
      this.selectedMaterial = null;
    },

    hideMaterialForm() {
      this.showAddForm = false;
      this.editingMaterial = null;
    },

    showAddMaterialForm() {
      this.showAddForm = true;
      this.editingMaterial = null;
    },

    async toggleMaterialStatus(materialId, valid) {
      try {
        console.log('切换材料状态:', materialId, '目标状态:', valid);
        if (valid) {
          // 上线材料
          await materialService.activateMaterial(materialId);
        } else {
          // 下线材料
          await materialService.deactivateMaterial(materialId);
        }
        await this.fetchMaterials();
      } catch (error) {
        console.error('更新材料状态失败:', error);
        alert('更新材料状态失败: ' + (error.message || '未知错误'));
      }
    },

    async deleteMaterial(materialId) {
      // 查找要删除的材料
      const material = this.materials.find(m => m.id === materialId);

      // 检查材料是否已下线，只有已下线的材料才能删除
      if (material && material.valid) {
        alert('只能删除已下线的材料，请先下线该材料再删除。');
        return;
      }

      if (!confirm('确定要删除这个材料吗？')) {
        return;
      }

      try {
        await materialService.deleteMaterial(materialId);
        await this.fetchMaterials();
      } catch (error) {
        console.error('删除材料失败:', error);
        alert('删除材料失败: ' + (error.message || '未知错误'));
      }
    },

    async handleMaterialSaved(material) {
      this.showAddForm = false;
      this.editingMaterial = null;
      await this.fetchMaterials();
    },

    async handleMaterialUpdated() {
      this.selectedMaterial = null;
      await this.fetchMaterials();
    },

    // 添加resetToListView方法，用于从App.vue中调用返回列表视图
    resetToListView() {
      this.selectedMaterial = null;
      this.showAddForm = false;
      this.editingMaterial = null;
    }
  },
  watch: {
    filterStatus() {
      this.filterMaterials();
    },
    searchKeyword() {
      this.filterMaterials();
    }
  }
};
</script>

<style scoped>
.material-list-container {
  padding: 20px;
  position: relative;
  z-index: 1;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.filter-group,
.search-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-group label {
  font-weight: bold;
}

.filter-group select,
.search-group input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.materials-table {
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  background-color: #f8f9fa;
  font-weight: bold;
  border-bottom: 1px solid #ddd;
}

.table-row {
  display: flex;
  border-bottom: 1px solid #eee;
  transition: background-color 0.2s;
  cursor: pointer;
  position: relative;
  z-index: 1;
}

.table-row:hover {
  background-color: #f5f5f5;
}

.table-cell {
  flex: 1;
  padding: 12px;
  border-right: 1px solid #eee;
  display: flex;
  align-items: center;
  min-width: 0; /* 添加此属性以防止内容溢出 */
  word-wrap: break-word; /* 允许长单词换行 */
  word-break: break-word; /* 允许单词内换行 */
  white-space: normal; /* 允许正常换行 */
}

.table-cell:last-child {
  border-right: none;
}

/* 进一步优化列宽以适应屏幕显示 */
.table-cell:nth-child(1) { flex: 0 0 30px; }   /* ID列 */
.table-cell:nth-child(2) { flex: 1; min-width: 80px; max-width: 120px; } /* 材料明细列 */
.table-cell:nth-child(3) { flex: 0 0 40px; }  /* 版本列 */
.table-cell:nth-child(4) { flex: 0 0 90px; }  /* 审核要点列 */
.table-cell:nth-child(5) { flex: 0 0 80px; } /* 智能秒批判断标准列 */
.table-cell:nth-child(6) { flex: 0 0 50px; }  /* 是否共享列 */
.table-cell:nth-child(7) { flex: 0 0 80px; } /* 材料来源列 */
.table-cell:nth-child(8) { flex: 0 0 100px; } /* 办理方式及材料信息获取方式说明列 */
.table-cell:nth-child(9) { flex: 0 0 60px; } /* 是否适用告知承诺列 */
.table-cell:nth-child(10) { flex: 0 0 50px; } /* 是否有效列 */
.table-cell:nth-child(11) { flex: 0 0 90px; }/* 操作列 */

/* 特殊处理需要换行的列 */
.table-cell:nth-child(2),
.table-cell:nth-child(4),
.table-cell:nth-child(5),
.table-cell:nth-child(7),
.table-cell:nth-child(8) {
  align-items: flex-start; /* 顶部对齐 */
}

.material-name {
  color: #007bff;
  text-decoration: underline;
  z-index: 2;
  position: relative;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: normal;
  word-wrap: break-word;
  word-break: break-word;
}

.material-name:hover {
  color: #0056b3;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.status-active {
  background-color: #d4edda;
  color: #155724;
}

.status-inactive {
  background-color: #f8d7da;
  color: #721c24;
}

.action-buttons {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

button {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: background-color 0.2s;
  position: relative;
  z-index: 2;
}

.add-btn {
  background-color: #28a745;
  color: white;
}

.add-btn:hover {
  background-color: #218838;
}

.refresh-btn {
  background-color: #17a2b8;
  color: white;
}

.refresh-btn:hover {
  background-color: #138496;
}

.online-btn {
  background-color: #28a745;
  color: white;
}

.online-btn:hover {
  background-color: #218838;
}

.online-btn:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.offline-btn {
  background-color: #ffc107;
  color: #212529;
}

.offline-btn:hover {
  background-color: #e0a800;
}

.offline-btn:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.delete-btn {
  background-color: #dc3545;
  color: white;
}

.delete-btn:hover {
  background-color: #c82333;
}

.delete-btn:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #dee2e6;
  position: relative;
  z-index: 1;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 15px;
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-size-selector label {
  font-weight: bold;
}

.page-size-selector select {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.pagination button {
  padding: 8px 16px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  position: relative;
  z-index: 2;
}

.pagination button:hover:not(:disabled) {
  background-color: #0056b3;
}

.pagination button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.loading,
.error,
.no-data {
  text-align: center;
  padding: 40px 20px;
}

.loading p,
.error p,
.no-data p {
  margin: 0 0 20px 0;
  font-size: 16px;
}

@media (max-width: 768px) {
  .materials-table {
    font-size: 14px;
  }

  .table-cell {
    padding: 8px;
  }

  .filter-section {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }

  .pagination {
    flex-direction: column;
    gap: 15px;
  }

  .pagination-controls {
    flex-wrap: wrap;
    justify-content: center;
    text-align: center;
  }
}
</style>
