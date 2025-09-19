<!-- src/components/UpdateRecord/UpdateRecordList.vue -->
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
          <div class="table-cell sortable" @click="sort('createdTime')">
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
          <div class="table-cell" @click="selectRecord(record)">
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
  methods: {
    // 查看记录详情，在当前页面中显示
    selectRecord(record) {
      console.log('点击记录:', record);
      this.selectedRecord = record;
      console.log('设置selectedRecord后:', this.selectedRecord);
    },

    // 返回列表视图
    goBackToList() {
      this.selectedRecord = null;
      // 如果当前组件嵌套在其他组件中，同时通知父组件
      this.$emit('back-to-list');
    },

    async fetchRecords() {
      this.loading = true
      this.error = null
      try {
        let url = '/update-records';
        const params = {};
        
        // 如果提供了filterEntityId，则使用专门的API端点获取记录
        if (this.filterEntityId) {
          url = `/update-records/entity-id/${this.filterEntityId}`;
        } else {
          // 添加过滤和排序参数
          if (this.searchKeyword) {
            params.entityName = this.searchKeyword;
          }
          
          if (this.effectiveFilterEntityType) {
            params.entityType = this.effectiveFilterEntityType;
          }
          
          if (this.filterOperationType) {
            params.operationType = this.filterOperationType;
          }
          
          params.sortBy = this.sortBy;
          params.sortDirection = this.sortDirection;
        }
        
        const response = await api.http.get(url, { params });
        this.records = response.data;
        
        this.filterRecords()
      } catch (err) {
        console.error('获取更新记录失败:', err)
        this.error = err.message || '获取更新记录失败'
      } finally {
        this.loading = false
      }
    },

    filterRecords() {
      this.filteredRecords = this.records.filter(record => {
        // 实体类型筛选 - 支持新的流程图类型
        if (this.effectiveFilterEntityType) {
          // 特殊处理流程图类型
          if (this.effectiveFilterEntityType === 'ApprovalProcessDiagram' && record.entityType !== 'ApprovalProcessDiagram') {
            return false;
          }
          if (this.effectiveFilterEntityType === 'BusinessProcessDiagram' && record.entityType !== 'BusinessProcessDiagram') {
            return false;
          }
          // 处理其他类型
          if (this.effectiveFilterEntityType !== 'ApprovalProcessDiagram' && 
              this.effectiveFilterEntityType !== 'BusinessProcessDiagram' && 
              record.entityType !== this.effectiveFilterEntityType) {
            return false;
          }
        }

        // 实体ID筛选
        if (this.filterEntityId && record.entityId !== this.filterEntityId) {
          return false;
        }

        // 操作类型筛选
        if (this.filterOperationType && record.operationType !== this.filterOperationType) {
          return false;
        }

        // 关键词搜索
        if (this.searchKeyword && !this.filterEntityId) {
          const keyword = this.searchKeyword.toLowerCase();
          const entityName = (record.entityName || '').toLowerCase();
          if (!entityName.includes(keyword)) {
            return false;
          }
        }

        return true;
      });

      // 重置到第一页
      this.currentPage = 1;
    },

    getOperationTypeLabel(operationType) {
      const labels = {
        'CREATE': '创建',
        'UPDATE': '更新',
        'DELETE': '删除',
        'BATCH_COPY': '批量拷贝',
        'BATCH_PUBLISH': '批量发布'
      }
      return labels[operationType] || operationType
    },

    formatDate(dateString) {
      if (!dateString) return '-'
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN')
    },

    // 删除记录
    async deleteRecord(id) {
      if (!confirm('确定要删除这条更新记录吗？此操作不可恢复。')) {
        return;
      }

      try {
        await api.http.delete(`/update-records/${id}`);
        // 删除成功后刷新列表
        await this.fetchRecords();
        alert('删除成功');
      } catch (err) {
        console.error('删除记录失败:', err);
        if (err.response) {
          if (err.response.status === 404) {
            alert('记录不存在');
          } else if (err.response.status === 500) {
            alert('服务器内部错误，请稍后重试');
          } else {
            var message = '未知错误';
            if (err.response.data && err.response.data.message) {
              message = err.response.data.message;
            } else if (err.response.statusText) {
              message = err.response.statusText;
            }
            alert('删除失败: ' + message);
          }
        } else {
          alert('删除失败: 网络错误或服务器无响应');
        }
      }
    },

    // 上一页
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--
        window.scrollTo(0, 0)
      }
    },

    // 下一页
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++
        window.scrollTo(0, 0)
      }
    },

    // 改变每页显示数量
    handlePageSizeChange() {
      // 重置到第一页
      this.currentPage = 1
      // 滚动到顶部
      window.scrollTo(0, 0)
    },

    // 添加resetToListView方法，用于从App.vue中调用返回列表视图
    resetToListView() {
      this.selectedRecord = null;
    },
    
    getEntityTypeLabel(entityType) {
      const labels = {
        'Material': '材料',
        'Matter': '事项',
        'ApprovalProcessDiagram': '审批流程图',
        'BusinessProcessDiagram': '业务经办流程图'
      };
      return labels[entityType] || entityType;
    },
    
    // 排序功能
    sort(field) {
      if (this.sortBy === field) {
        // 如果当前已经是这个字段，则切换排序方向
        this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
      } else {
        // 切换到新字段，默认降序
        this.sortBy = field;
        this.sortDirection = 'desc';
      }
      this.fetchRecords();
    }
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
  }
}
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
  color: #1890ff;
  cursor: pointer;
  text-decoration: underline;
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