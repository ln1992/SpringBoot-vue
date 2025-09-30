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
        <div class="search-group">
          <input
            type="text"
            v-model="searchKeyword"
            placeholder="搜索材料明细..."
            @input="filterMaterials"
          >
        </div>
        <div class="filter-group">
          <select v-model="filterVersion" @change="filterMaterials">
            <option value="">所有版本</option>
            <option v-for="version in versions" :key="version" :value="version">
              {{ version }}
            </option>
          </select>
        </div>
        <div class="filter-group">
          <select v-model="filterStatus" @change="filterMaterials">
            <option value="all">全部</option>
            <option value="active">已上线</option>
            <option value="inactive">已下线</option>
          </select>
        </div>
        <div class="sort-group">
          <label>排序:</label>
          <select v-model="sortBy" @change="filterMaterials">
            <option value="id">ID</option>
            <option value="__name__">材料明细</option>
            <option value="version">版本</option>
            <option value="shared">是否共享</option>
            <option value="valid">是否有效</option>
          </select>
          <select v-model="sortDirection" @change="filterMaterials">
            <option value="desc">降序</option>
            <option value="asc">升序</option>
          </select>
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
          <div class="table-cell sortable" @click="sort('id')">
            ID
            <span v-if="sortBy === 'id'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell sortable" @click="sort('__name__')">
            材料明细
            <span v-if="sortBy === '__name__'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell sortable" @click="sort('version')">
            版本
            <span v-if="sortBy === 'version'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell">审核要点</div>
          <div class="table-cell">"智能秒批"判断标准</div>
          <div class="table-cell sortable" @click="sort('shared')">
            是否共享
            <span v-if="sortBy === 'shared'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell">材料来源</div>
          <div class="table-cell">办理方式及材料信息获取方式说明</div>
          <div class="table-cell">是否适用告知承诺</div>
          <div class="table-cell sortable" @click="sort('valid')">
            是否有效
            <span v-if="sortBy === 'valid'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
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
      filterVersion: '',
      // 排序相关数据
      sortBy: 'id',
      sortDirection: 'desc',
      // 版本数据
      versions: [],
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
    await this.fetchVersions();
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
    
    async fetchVersions() {
      try {
        this.versions = await materialService.getMaterialVersions();
      } catch (error) {
        console.error('获取材料版本失败:', error);
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
      
      // 版本筛选
      if (this.filterVersion !== '') {
        const version = parseInt(this.filterVersion);
        if (!isNaN(version)) {
          result = result.filter(material => material.version === version);
        }
      }

      // 应用排序
      this.sortMaterials(result);
      
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

    handleMaterialUpdated() {
      this.selectedMaterial = null;
      this.showAddForm = false;
      this.editingMaterial = null;
      this.fetchMaterials();
    },

    async handleMaterialSaved() {
      this.hideMaterialForm();
      await this.fetchMaterials();
    },

    // 添加resetToListView方法，用于从App.vue中调用返回列表视图
    resetToListView() {
      // 清空选中的材料
      this.selectedMaterial = null;
      // 隐藏新增表单
      this.showAddForm = false;
      // 重置分页
      this.currentPage = 1;
      // 重新获取数据确保列表是最新的
      this.fetchMaterials();
    },
    
    // 添加显示指定材料详情的方法
    async showMaterialDetail(materialId) {
      try {
        // 获取材料详情
        const material = await materialService.getMaterialById(materialId);
        // 设置为选中的材料，触发详情视图
        this.selectedMaterial = material;
      } catch (error) {
        console.error('获取材料详情失败:', error);
        alert('获取材料详情失败: ' + (error.message || '未知错误'));
      }
    },

    // 排序功能
    sortMaterials(materials) {
      const field = this.sortBy;
      const direction = this.sortDirection;
      
      materials.sort((a, b) => {
        let valueA = a[field];
        let valueB = b[field];
        
        // 特殊处理名称字段
        if (field === '__name__') {
          valueA = a.__name__ || a.materialDetail || '';
          valueB = b.__name__ || b.materialDetail || '';
        }
        
        // 处理布尔值字段
        if (field === 'shared' || field === 'valid' || field === 'eligibleForPromise') {
          valueA = a[field] ? 1 : 0;
          valueB = b[field] ? 1 : 0;
        }
        
        // 处理null或undefined值
        if (valueA == null && valueB == null) return 0;
        if (valueA == null) return direction === 'asc' ? -1 : 1;
        if (valueB == null) return direction === 'asc' ? 1 : -1;
        
        // 比较值
        let comparison = 0;
        if (typeof valueA === 'string' && typeof valueB === 'string') {
          comparison = valueA.localeCompare(valueB);
        } else {
          comparison = valueA < valueB ? -1 : valueA > valueB ? 1 : 0;
        }
        
        return direction === 'asc' ? comparison : -comparison;
      });
    },
    
    // 点击列标题排序
    sort(field) {
      if (this.sortBy === field) {
        // 如果当前已经是这个字段，则切换排序方向
        this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
      } else {
        // 切换到新字段，默认降序
        this.sortBy = field;
        this.sortDirection = 'desc';
      }
      this.filterMaterials();
    },
    
    // 格式化日期显示
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      const seconds = String(date.getSeconds()).padStart(2, '0');
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    }
  },
  watch: {
    filterStatus() {
      this.filterMaterials();
    },
    searchKeyword() {
      this.filterMaterials();
    },
    filterVersion() {
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

/* 添加搜索和筛选区域样式 */
.filter-section {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f5f5;
  border-radius: 4px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-group,
.search-group,
.sort-group {
  display: flex;
  align-items: center;
  gap: 5px;
}

.filter-group label,
.search-group label,
.sort-group label {
  white-space: nowrap;
}

.filter-group input,
.filter-group select,
.search-group input,
.sort-group select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  min-width: 120px;
}

.filter-group input[type="number"] {
  min-width: 80px;
}

.search-group input {
  min-width: 150px;
}

.loading,
.error,
.no-data {
  text-align: center;
  padding: 40px 20px;
}

.materials-table {
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 20px;
  max-height: 600px;
  overflow-y: auto;
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
  position: relative;
  z-index: 1;
  cursor: pointer;
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
  min-width: 0;
  word-wrap: break-word;
  word-break: break-word;
  white-space: normal;
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
.table-cell:nth-child(11) { flex: 0 0 90px; } /* 操作列 */

@media (max-width: 768px) {
  .materials-table {
    font-size: 14px;
  }

  .table-cell {
    padding: 8px;
  }

  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-group,
  .search-group,
  .sort-group {
    width: 100%;
  }

  .filter-group input,
  .filter-group select,
  .search-group input,
  .sort-group select {
    width: 100%;
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
  
  /* 在小屏幕上调整列宽 */
  .table-cell:nth-child(1) { flex: 0 0 30px; }
  .table-cell:nth-child(2) { flex: 1; min-width: 100px; }
  .table-cell:nth-child(3) { flex: 0 0 50px; }
  .table-cell:nth-child(4) { flex: 0 0 80px; }
  .table-cell:nth-child(5) { flex: 0 0 100px; }
  .table-cell:nth-child(6) { flex: 0 0 50px; }
  .table-cell:nth-child(7) { flex: 0 0 80px; }
  .table-cell:nth-child(8) { flex: 0 0 120px; }
  .table-cell:nth-child(9) { flex: 0 0 50px; }
  .table-cell:nth-child(10) { flex: 0 0 50px; }
  .table-cell:nth-child(11) { flex: 0 0 120px; }
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
