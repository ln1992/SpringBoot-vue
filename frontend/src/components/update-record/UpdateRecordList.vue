<!-- src/components/UpdateRecord/UpdateRecordList.vue -->
<template>
  <div class="update-record-list-container">
    <!-- 更新记录列表界面 -->
    <div v-if="!selectedRecord && !showAddForm">
      <div class="header">
        <h2>更新记录</h2>
        <div class="header-actions">
          <button class="refresh-btn" @click="fetchRecords">刷新</button>
        </div>
      </div>

      <div class="filter-section">
        <div class="filter-group">
          <label>实体类型筛选:</label>
          <select v-model="filterEntityType" @change="filterRecords">
            <option value="">全部</option>
            <option value="Material">材料</option>
            <option value="Matter">事项</option>
            <option value="ProcessDiagram">流程图</option>
          </select>
        </div>
        <div class="filter-group">
          <label>操作类型筛选:</label>
          <select v-model="filterOperationType" @change="filterRecords">
            <option value="">全部</option>
            <option value="CREATE">创建</option>
            <option value="UPDATE">更新</option>
            <option value="DELETE">删除</option>
          </select>
        </div>
        <div class="search-group">
          <input
            type="text"
            v-model="searchKeyword"
            placeholder="搜索实体名称..."
            @input="filterRecords"
          >
        </div>
      </div>

      <div class="loading" v-if="loading">
        <p>正在加载更新记录数据...</p>
      </div>

      <div class="error" v-else-if="error">
        <p>加载失败: {{ error }}</p>
        <button @click="fetchRecords">重试</button>
      </div>

      <div class="no-data" v-else-if="paginatedRecords.length === 0">
        <p>暂无更新记录数据</p>
      </div>

      <!-- 更新记录表格 -->
      <div class="records-table" v-else>
        <div class="table-header">
          <div class="table-cell">ID</div>
          <div class="table-cell">实体名称</div>
          <div class="table-cell">实体ID</div>
          <div class="table-cell">实体类型</div>
          <div class="table-cell">操作类型</div>
          <div class="table-cell">操作用户</div>
          <div class="table-cell">创建时间</div>
        </div>
        <div 
          class="table-row" 
          v-for="record in paginatedRecords" 
          :key="record.id"
          @click="selectRecord(record)"
        >
          <div class="table-cell">{{ record.id }}</div>
          <div class="table-cell">{{ record.entityName || '-' }}</div>
          <div class="table-cell">{{ record.entityId }}</div>
          <div class="table-cell">{{ record.entityType }}</div>
          <div class="table-cell">{{ getOperationTypeLabel(record.operationType) }}</div>
          <div class="table-cell">{{ record.operator }}</div>
          <div class="table-cell">{{ formatDate(record.createdTime) }}</div>
        </div>
      </div>
      
      <!-- 自定义分页组件 -->
      <div class="pagination" v-if="paginatedRecords.length > 0">
        <div class="pagination-controls">
          <button 
            :disabled="currentPage === 1"
            @click="currentPage > 1 && (currentPage--)">
            上一页
          </button>
          
          <span>
            第 {{ currentPage }} 页，共 {{ totalPages }} 页 (总计 {{ filteredRecords.length }} 条)
          </span>
          
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
  data() {
    return {
      records: [],
      filteredRecords: [],
      selectedRecord: null,
      loading: false,
      error: null,
      filterEntityType: '',
      filterOperationType: '',
      searchKeyword: '',
      showAddForm: false,
      // 分页相关数据
      currentPage: 1,
      pageSize: 10
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
    }
  },
  mounted() {
    this.fetchRecords()
  },
  methods: {
    async fetchRecords() {
      this.loading = true
      this.error = null
      try {
        // 修复API调用方式，移除多余的/api前缀，因为http实例已经配置了baseURL
        const response = await api.http.get('/update-records')
        this.records = response.data
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
        // 实体类型筛选
        if (this.filterEntityType && record.entityType !== this.filterEntityType) {
          return false
        }
        
        // 操作类型筛选
        if (this.filterOperationType && record.operationType !== this.filterOperationType) {
          return false
        }
        
        // 关键词搜索
        if (this.searchKeyword) {
          const keyword = this.searchKeyword.toLowerCase()
          const entityName = (record.entityName || '').toLowerCase()
          if (!entityName.includes(keyword)) {
            return false
          }
        }
        
        return true
      })
      
      // 重置到第一页
      this.currentPage = 1
    },
    
    selectRecord(record) {
      this.selectedRecord = record
    },
    
    goBackToList() {
      this.selectedRecord = null
      this.fetchRecords() // 返回列表时刷新数据
    },
    
    getOperationTypeLabel(operationType) {
      const labels = {
        'CREATE': '创建',
        'UPDATE': '更新',
        'DELETE': '删除'
      }
      return labels[operationType] || operationType
    },
    
    formatDate(dateString) {
      if (!dateString) return '-'
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN')
    },
    
    // 添加resetToListView方法以符合规范
    resetToListView() {
      this.selectedRecord = null
      this.showAddForm = false
      this.fetchRecords()
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
    
    // 处理每页显示条数变化
    handlePageSizeChange() {
      // 重置到第一页
      this.currentPage = 1
    }
  }
}
</script>

<style scoped>
.update-record-list-container {
  padding: 20px;
  /* 移除底部边距，使用全局布局解决遮挡问题 */
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

.add-btn {
  background-color: #409eff;
  color: white;
}

.filter-section {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-group, .search-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-group label {
  white-space: nowrap;
}

.filter-group select, .search-group input {
  padding: 6px 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.search-group input {
  min-width: 200px;
}

.loading, .error, .no-data {
  text-align: center;
  padding: 40px 20px;
}

.error {
  color: #f56c6c;
}

.no-data {
  color: #909399;
}

.records-table {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  background-color: #f5f7fa;
  font-weight: bold;
}

.table-row {
  display: flex;
  border-top: 1px solid #ebeef5;
  cursor: pointer;
  transition: background-color 0.2s;
}

.table-row:hover {
  background-color: #f5f7fa;
}

.table-cell {
  flex: 1;
  padding: 12px;
  min-width: 0;
  word-break: break-word;
  display: flex;
  align-items: center;
}

.table-cell:first-child {
  flex: 0 0 80px;
}

.table-cell:nth-child(2) {
  flex: 0 0 150px;
}

.table-cell:nth-child(3) {
  flex: 0 0 100px;
}

.table-cell:nth-child(4) {
  flex: 0 0 120px;
}

.table-cell:nth-child(5) {
  flex: 0 0 100px;
}

.table-cell:nth-child(6) {
  flex: 0 0 120px;
}

.table-cell:last-child {
  flex: 0 0 160px;
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding: 20px 0;
  flex-wrap: wrap;
  gap: 10px;
  /* 使用z-index确保分页控件在最上层，不被页脚遮挡 */
  position: relative;
  z-index: 1001;
  background-color: white;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 20px;
}

.pagination-controls button {
  padding: 8px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #fff;
  cursor: pointer;
  transition: all 0.3s;
}

.pagination-controls button:hover:not(:disabled) {
  background-color: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
}

.pagination-controls button:disabled {
  color: #c0c4cc;
  cursor: not-allowed;
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-size-selector select {
  padding: 6px 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.pagination-info {
  color: #606266;
  font-size: 14px;
}

@media (max-width: 768px) {
  .filter-section {
    flex-direction: column;
  }
  
  .filter-group, .search-group {
    width: 100%;
  }
  
  .search-group input {
    min-width: auto;
    flex: 1;
  }
  
  .pagination {
    flex-direction: column;
    gap: 10px;
  }
}
</style>