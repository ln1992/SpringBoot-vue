<!-- src/components/update-record/UpdateRecordList.vue -->
<template>
  <div class="update-record-list-container">
    <!-- 更新记录列表界面 -->
    <div v-if="!selectedRecord">
      <div class="header">
        <h2>更新记录</h2>
        <div class="header-actions" v-if="!hideActions">
          <button class="refresh-btn" @click="fetchRecords">刷新</button>
        </div>
      </div>

      <div class="filter-section" v-if="!filterEntityId && !hideFilters">
        <div class="search-group">
          <input
            type="text"
            v-model="searchKeyword"
            placeholder="搜索实体名称..."
            @input="filterRecords"
          >
        </div>
        <div class="filter-group">
          <label>实体类型筛选:</label>
          <select v-model="localFilterEntityType" @change="filterRecords">
            <option value="">全部</option>
            <option value="Material">材料</option>
            <option value="Matter">事项</option>
            <option value="ApprovalProcessDiagram">审批流程图</option>
            <option value="BusinessProcessDiagram">业务经办流程图</option>
          </select>
        </div>
        <div class="filter-group">
          <label>操作类型筛选:</label>
          <select v-model="filterOperationType" @change="filterRecords">
            <option value="">全部</option>
            <option value="CREATE">创建</option>
            <option value="UPDATE">更新</option>
            <option value="DELETE">删除</option>
            <option value="BATCH_COPY">批量拷贝</option>
            <option value="BATCH_PUBLISH">批量发布</option>
          </select>
        </div>
        <div class="sort-group">
          <label>排序:</label>
          <select v-model="sortBy" @change="fetchRecords">
            <option value="createdTime">创建时间</option>
            <option value="entityName">实体名称</option>
            <option value="entityType">实体类型</option>
            <option value="operationType">操作类型</option>
          </select>
          <select v-model="sortDirection" @change="fetchRecords">
            <option value="desc">降序</option>
            <option value="asc">升序</option>
          </select>
        </div>
      </div>

      <div class="loading" v-if="loading">
        <p>正在加载更新记录数据...</p>
      </div>

      <div class="error" v-else-if="error">
        <p>加载失败: {{ error }}</p>
        <button @click="fetchRecords">重试</button>
      </div>

      <div class="no-data" v-else-if="filteredRecords.length === 0">
        <p>暂无更新记录数据</p>
      </div>

      <!-- 更新记录表格 -->
      <div class="records-table" v-else>
        <div class="table-header">
          <div class="table-cell sortable" @click="sort('id')">
            ID
            <span v-if="sortBy === 'id'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell sortable" @click="sort('entityName')">
            实体名称
            <span v-if="sortBy === 'entityName'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell">实体ID</div>
          <div class="table-cell sortable" @click="sort('entityType')">
            实体类型
            <span v-if="sortBy === 'entityType'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell sortable" @click="sort('operationType')">
            操作类型
            <span v-if="sortBy === 'operationType'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell">操作用户</div>
          <div class="table-cell sortable" @click="sort('createdTime')">
            创建时间
            <span v-if="sortBy === 'createdTime'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
        </div>
        <div
          class="table-row"
          v-for="record in paginatedRecords"
          :key="record.id"
        >
          <div class="table-cell record-id" @click="selectRecord(record)">{{ record.id }}</div>
          <div
            class="table-cell entity-name"
            @click="selectRecord(record)"
          >
            {{ record.entityName || '-' }}
          </div>
          <div class="table-cell">{{ record.entityId }}</div>
          <div class="table-cell">{{ getEntityTypeLabel(record.entityType) }}</div>
          <div class="table-cell">{{ getOperationTypeLabel(record.operationType) }}</div>
          <div class="table-cell">{{ record.operator || '-' }}</div>
          <div class="table-cell">{{ formatDate(record.createdTime) }}</div>
        </div>
      </div>

      <!-- 分页控件 -->
      <div class="pagination" v-if="paginatedRecords.length > 0 && !hidePagination">
        <div class="pagination-controls">
          <button
            :disabled="currentPage === 1"
            @click="currentPage > 1 && (currentPage--)">
            上一页
          </button>
          <span>第 {{ currentPage }} 页，共 {{ totalPages }} 页 (总计 {{ filteredRecords.length }} 条)</span>
          <button
            :disabled="currentPage === totalPages"
            @click="currentPage < totalPages && (currentPage++)">
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

    <!-- 更新记录详情界面 -->
    <UpdateRecordDetail
      v-else-if="selectedRecord"
      :record="selectedRecord"
      @back="goBackToList"
    />
  </div>
</template>

<script>
import api from '../../api/index.js'
import UpdateRecordDetail from './UpdateRecordDetail.vue'

export default {
  name: 'UpdateRecordList',
  components: {
    UpdateRecordDetail
  },
  props: {
    filterEntityType: {
      type: String,
      default: ''
    },
    filterEntityId: {
      type: [String, Number],
      default: null
    },
    hideActions: {
      type: Boolean,
      default: false
    },
    hideFilters: {
      type: Boolean,
      default: false
    },
    hidePagination: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      records: [],
      filteredRecords: [],
      loading: false,
      error: null,
      localFilterEntityType: '',
      filterOperationType: '',
      searchKeyword: '',
      filterNewType: '',  // 新增的筛选类型变量
      // 分页相关数据
      currentPage: 1,
      pageSize: 10,
      selectedRecord: null,
      // 排序相关数据
      sortBy: 'createdTime',
      sortDirection: 'desc'
    }
  },
  computed: {
    // 计算总页数
    totalPages() {
      return Math.ceil(this.filteredRecords.length / this.pageSize)
    },

    // 计算分页后的记录
    paginatedRecords() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredRecords.slice(start, end)
    },

    effectiveFilterEntityType() {
      return this.filterEntityType || this.localFilterEntityType;
    }
  },
  mounted() {
    this.fetchRecords()
  },
  watch: {
    filterEntityType: {
      handler() {
        this.filterRecords();
      }
    },
    filterEntityId: {
      handler() {
        this.filterRecords();
      }
    }
  },
  methods: {
    async fetchRecords() {
      this.loading = true;
      this.error = null;
      try {
        const response = await api.updateRecordService.getAllUpdateRecords();
        this.records = response.data || response;
        this.filterRecords();
      } catch (error) {
        console.error('获取更新记录失败:', error);
        this.error = error.message || '网络错误';
        this.records = [];
        this.filteredRecords = [];
      } finally {
        this.loading = false;
      }
    },

    filterRecords() {
      let result = [...this.records];

      // 应用实体类型筛选
      if (this.effectiveFilterEntityType) {
        result = result.filter(record => record.entityType === this.effectiveFilterEntityType);
      }

      // 应用操作类型筛选
      if (this.filterOperationType) {
        result = result.filter(record => record.operationType === this.filterOperationType);
      }

      // 应用搜索关键词
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase();
        result = result.filter(record =>
          (record.entityName && record.entityName.toLowerCase().includes(keyword)) ||
          (record.description && record.description.toLowerCase().includes(keyword))
        );
      }

      // 应用实体ID筛选
      if (this.filterEntityId) {
        result = result.filter(record => record.entityId == this.filterEntityId);
      }

      // 应用排序
      if (this.sortBy && this.sortDirection) {
        result.sort((a, b) => {
          let valueA = a[this.sortBy];
          let valueB = b[this.sortBy];

          // 处理日期字段
          if (this.sortBy === 'createdTime') {
            valueA = new Date(valueA).getTime();
            valueB = new Date(valueB).getTime();
          }

          // 处理null值
          if (valueA == null && valueB == null) return 0;
          if (valueA == null) return this.sortDirection === 'asc' ? -1 : 1;
          if (valueB == null) return this.sortDirection === 'asc' ? 1 : -1;

          // 比较值
          let comparison = 0;
          if (typeof valueA === 'string' && typeof valueB === 'string') {
            comparison = valueA.localeCompare(valueB);
          } else {
            comparison = valueA < valueB ? -1 : valueA > valueB ? 1 : 0;
          }

          return this.sortDirection === 'asc' ? comparison : -comparison;
        });
      }

      this.filteredRecords = result;
      this.currentPage = 1; // 重置到第一页
    },

    selectRecord(record) {
      this.selectedRecord = record;
    },

    goBackToList() {
      this.selectedRecord = null;
    },

    getEntityTypeLabel(type) {
      const typeMap = {
        'Material': '材料',
        'Matter': '事项',
        'ApprovalProcessDiagram': '审批流程图',
        'BusinessProcessDiagram': '业务经办流程图'
      };
      return typeMap[type] || type;
    },

    getOperationTypeLabel(type) {
      const typeMap = {
        'CREATE': '创建',
        'UPDATE': '更新',
        'DELETE': '删除',
        'BATCH_COPY': '批量拷贝',
        'BATCH_PUBLISH': '批量发布'
      };
      return typeMap[type] || type;
    },

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
    },

    sort(field) {
      if (this.sortBy === field) {
        // 如果当前已经是这个字段，则切换排序方向
        this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
      } else {
        // 切换到新字段，默认降序
        this.sortBy = field;
        this.sortDirection = 'desc';
      }
      this.filterRecords();
    },

    handlePageSizeChange() {
      this.currentPage = 1;
      this.filterRecords();
    },

    resetToListView() {
      this.selectedRecord = null;
      this.fetchRecords();
    }
  }
};
</script>

<style scoped>
.update-record-list-container {
  padding: 20px;
  /* 添加相对定位和z-index确保容器在最上层 */
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
  margin: 0;
}

.header-actions button {
  margin-left: 10px;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.refresh-btn {
  background-color: #f0f0f0;
  color: #333;
}

.filter-section {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  flex-wrap: wrap;
  align-items: center;
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

.filter-group select,
.search-group input,
.sort-group select {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.loading,
.error,
.no-data {
  text-align: center;
  padding: 40px 20px;
}

.error {
  color: #d32f2f;
}

.records-table {
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
}

.table-header {
  display: flex;
  background-color: #f5f7fa;
  font-weight: bold;
  border-bottom: 1px solid #ddd;
}

.table-cell {
  flex: 1;
  padding: 12px 15px;
  text-align: left;
  border-right: 1px solid #eee;
  min-width: 0;
  word-wrap: break-word;
}

.table-cell:last-child {
  border-right: none;
}

.record-id {
  cursor: pointer;
  color: #1890ff;
  text-decoration: underline;
}

.record-id:hover {
  color: #40a9ff;
}

.table-row {
  display: flex;
  border-bottom: 1px solid #eee;
  transition: background-color 0.2s;
}

.table-row:last-child {
  border-bottom: none;
}

.table-row:hover {
  background-color: #f9f9f9;
}

.entity-name {
  color: #333;
  cursor: pointer;
}

.entity-name:hover {
  color: #40a9ff;
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

.sortable {
  cursor: pointer;
  user-select: none;
}

.sortable:hover {
  background-color: #e6f7ff;
}

@media (max-width: 768px) {
  .update-record-list-container {
    padding: 10px;
  }

  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .filter-section {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-group,
  .search-group,
  .sort-group {
    width: 100%;
  }

  .filter-group select,
  .search-group input,
  .sort-group select {
    flex: 1;
  }

  .pagination {
    flex-direction: column;
  }

  .table-cell {
    padding: 8px 10px;
    font-size: 14px;
  }
}
</style>
