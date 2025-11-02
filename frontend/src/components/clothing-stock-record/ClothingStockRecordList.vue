<!-- src/components/clothing-stock-record/ClothingStockRecordList.vue -->
<template>
  <div class="stock-record-container">
    <!-- 库存记录列表界面 -->
    <div v-if="!selectedStockRecord">
      <div class="header">
        <h2>库存记录管理</h2>
        <div class="header-actions">
          <button class="refresh-btn" @click="fetchStockRecords">刷新</button>
        </div>
      </div>

      <!-- 添加搜索和筛选区域 -->
      <div class="filter-section" v-if="!hideFilters">
        <div class="search-group">
          <input
            type="text"
            v-model="searchKeyword"
            placeholder="搜索库存记录..."
            @input="filterStockRecords"
          >
        </div>
        <!-- 添加尺码筛选下拉框 -->
        <div class="filter-group">
          <select v-model="filterSize" @change="filterStockRecords">
            <option value="">所有尺码</option>
            <option value="S">S</option>
            <option value="M">M</option>
            <option value="L">L</option>
          </select>
        </div>
        <div class="filter-group">
          <select v-model="filterOperationType" @change="filterStockRecords">
            <option value="">所有操作类型</option>
            <option value="INBOUND">入库</option>
            <option value="OUTBOUND">出库</option>
          </select>
        </div>
        <div class="sort-group">
          <label>排序:</label>
          <select v-model="sortBy" @change="sortStockRecords">
            <option value="id">ID</option>
            <option value="clothingId">服装ID</option>
            <option value="createdTime">创建时间</option>
          </select>
          <select v-model="sortDirection" @change="sortStockRecords">
            <option value="desc">降序</option>
            <option value="asc">升序</option>
          </select>
        </div>
      </div>

      <div class="loading" v-if="loading">
        <p>正在加载库存记录数据...</p>
      </div>

      <div class="error" v-else-if="error">
        <p>加载失败: {{ error }}</p>
        <button @click="fetchStockRecords">重试</button>
      </div>

      <div class="no-data" v-else-if="filteredStockRecords.length === 0">
        <div class="no-data-content">
          <h3>暂无库存记录数据</h3>
          <p>当前没有库存操作记录</p>
        </div>
      </div>

      <div class="stock-records-table" v-else>
        <div class="table-header">
          <div class="table-cell sortable" @click="sort('id')">
            ID
            <span v-if="sortBy === 'id'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell sortable" @click="sort('clothingId')">
            服装ID
            <span v-if="sortBy === 'clothingId'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell">服装名称</div>
          <div class="table-cell">尺寸</div>
          <div class="table-cell sortable" @click="sort('operationType')">
            操作类型
            <span v-if="sortBy === 'operationType'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell">数量</div>
          <div class="table-cell">操作前库存</div>
          <div class="table-cell">操作后库存</div>
          <div class="table-cell sortable" @click="sort('createdTime')">
            创建时间
            <span v-if="sortBy === 'createdTime'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell">操作员</div>
          <div class="table-cell">状态</div>
        </div>

        <div
          class="table-row"
          v-for="record in paginatedStockRecords"
          :key="record.id"
          @click="viewStockRecordDetail(record)"
        >
          <div class="table-cell">{{ record.id }}</div>
          <div class="table-cell">{{ record.clothingId }}</div>
          <div class="table-cell">{{ record.clothingName || '-' }}</div>
          <div class="table-cell">{{ record.size }}</div>
          <div class="table-cell">
            <span :class="['operation-type',
              record.operationType === 'INBOUND' ? 'inbound' : 'outbound']">
              {{ record.operationType === 'INBOUND' ? '入库' : '出库' }}
            </span>
          </div>
          <div class="table-cell">{{ record.quantity }}</div>
          <div class="table-cell">{{ record.previousStock !== null ? record.previousStock : '-' }}</div>
          <div class="table-cell">{{ record.currentStock !== null ? record.currentStock : '-' }}</div>
          <div class="table-cell">{{ formatDate(record.createdTime) }}</div>
          <div class="table-cell">{{ record.operator || '-' }}</div>
          <div class="table-cell">
            <span :class="['status-badge', record.valid ? 'status-active' : 'status-inactive']">
              {{ record.valid ? '已上线' : '已下线' }}
            </span>
          </div>
        </div>
      </div>

      <!-- 分页控件 -->
      <div class="pagination" v-if="paginatedStockRecords.length > 0 && !hidePagination">
        <div class="pagination-controls">
          <button
            :disabled="currentPage === 1"
            @click="currentPage > 1 && (currentPage--)">
            上一页
          </button>
          <span>第 {{ currentPage }} 页，共 {{ totalPages }} 页 (总计 {{ filteredStockRecords.length }} 条)</span>
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

    <!-- 库存记录详情界面 -->
    <ClothingStockRecordDetail
      v-else-if="selectedStockRecord"
      :stock-record="selectedStockRecord"
      @back="selectedStockRecord = null"
      @stock-record-updated="handleStockRecordUpdated"
    />
  </div>
</template>

<script>
import ClothingStockRecordDetail from './ClothingStockRecordDetail.vue';
import { clothingStockRecordService } from '../../api';

export default {
  name: 'ClothingStockRecordList',
  components: {
    ClothingStockRecordDetail
  },
  props: {
    filterClothingId: {
      type: Number,
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
      stockRecords: [],
      filteredStockRecords: [],
      selectedStockRecord: null,
      loading: true,
      error: null,
      currentPage: 1,
      pageSize: 10,
      // 添加搜索和筛选相关数据
      filterOperationType: '',
      searchKeyword: '',
      filterSize: '', // 添加尺码筛选数据
      // 排序相关数据
      sortBy: 'id',
      sortDirection: 'desc'
    };
  },
  computed: {
    totalPages() {
      return Math.ceil(this.filteredStockRecords.length / this.pageSize);
    },
    paginatedStockRecords() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.filteredStockRecords.slice(start, end);
    }
  },
  async mounted() {
    await this.fetchStockRecords();
  },
  methods: {
    async fetchStockRecords() {
      this.loading = true;
      this.error = null;
      this.currentPage = 1;

      try {
        const response = await clothingStockRecordService.getAll();
        this.stockRecords = response.data || [];

        // 初始化过滤后的数据
        this.filterStockRecords();
      } catch (error) {
        this.error = error.message || '网络错误';
        console.error('获取库存记录列表出错:', error);
      } finally {
        this.loading = false;
      }
    },

    // 添加过滤方法
    filterStockRecords() {
      let result = [...this.stockRecords];

      // 根据服装ID筛选（如果提供了filterClothingId）
      if (this.filterClothingId !== null) {
        result = result.filter(record => record.clothingId === this.filterClothingId);
      }

      // 操作类型筛选
      if (this.filterOperationType) {
        result = result.filter(record => record.operationType === this.filterOperationType);
      }

      // 尺码筛选
      if (this.filterSize) {
        result = result.filter(record => record.size === this.filterSize);
      }

      // 关键词搜索
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase();
        result = result.filter(record =>
          (record.clothingName && record.clothingName.toLowerCase().includes(keyword)) ||
          (record.operator && record.operator.toLowerCase().includes(keyword)) ||
          (record.size && record.size.toLowerCase().includes(keyword))
        );
      }

      // 应用排序
      this.sortStockRecords(result);

      this.filteredStockRecords = result;
      // 重置到第一页
      this.currentPage = 1;
    },

    handlePageSizeChange() {
      // 当页面大小改变时，重置到第一页
      this.currentPage = 1;
    },

    viewStockRecordDetail(record) {
      this.selectedStockRecord = record;
    },

    // 添加resetToListView方法，用于从App.vue中调用返回列表视图
    resetToListView() {
      this.selectedStockRecord = null;
    },

    // 排序功能
    sortStockRecords(records) {
      const field = this.sortBy;
      const direction = this.sortDirection;

      records.sort((a, b) => {
        let valueA = a[field];
        let valueB = b[field];

        // 处理日期字段
        if (field === 'createdTime') {
          valueA = new Date(a.createdTime).getTime();
          valueB = new Date(b.createdTime).getTime();
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
      this.filterStockRecords();
    },

    // 格式化日期显示
    formatDate(dateString) {
      if (!dateString) return '-';
      const date = new Date(dateString);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      const seconds = String(date.getSeconds()).padStart(2, '0');
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    },

    // 处理库存记录更新
    handleStockRecordUpdated(updatedStockRecord) {
      this.selectedStockRecord = updatedStockRecord;
      // 更新列表中的记录
      const index = this.stockRecords.findIndex(record => record.id === updatedStockRecord.id);
      if (index !== -1) {
        this.stockRecords.splice(index, 1, updatedStockRecord);
        this.filterStockRecords();
      }
    }
  },
  watch: {
    filterOperationType() {
      this.filterStockRecords();
    },
    searchKeyword() {
      this.filterStockRecords();
    },
    // 添加尺码筛选监听
    filterSize() {
      this.filterStockRecords();
    }
  }
};
</script>

<style scoped>
.stock-record-container {
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

.loading,
.error,
.no-data {
  text-align: center;
  padding: 40px 20px;
}

.no-data-content h3 {
  margin-top: 0;
}

.stock-records-table {
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
.table-cell:nth-child(1) { flex: 0 0 60px; }   /* ID列 */
.table-cell:nth-child(2) { flex: 0 0 60px; }  /* 服装ID列 */
.table-cell:nth-child(3) { flex: 2; min-width: 150px; } /* 服装名称列 */
.table-cell:nth-child(4) { flex: 0 0 60px; }  /* 尺寸列 */
.table-cell:nth-child(5) { flex: 0 0 80px; } /* 操作类型列 */
.table-cell:nth-child(6) { flex: 0 0 60px; } /* 数量列 */
.table-cell:nth-child(7) { flex: 0 0 60px; } /* 操作前库存列 */
.table-cell:nth-child(8) { flex: 0 0 60px; } /* 操作后库存列 */
.table-cell:nth-child(9) { flex: 0 0 160px; } /* 创建时间列 */
.table-cell:nth-child(10) { flex: 0 0 60px; } /* 操作员列 */
.table-cell:nth-child(11) { flex: 0 0 80px; } /* 状态列 */

.operation-type {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.operation-type.inbound {
  background-color: #d4edda;
  color: #155724;
}

.operation-type.outbound {
  background-color: #f8d7da;
  color: #721c24;
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

.refresh-btn {
  background-color: #17a2b8;
  color: white;
}

.refresh-btn:hover {
  background-color: #138496;
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
  .stock-records-table {
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
  .table-cell:nth-child(1) { flex: 0 0 40px; }
  .table-cell:nth-child(2) { flex: 0 0 40px; }
  .table-cell:nth-child(3) { flex: 1; min-width: 100px; }
  .table-cell:nth-child(4) { flex: 0 0 50px; }
  .table-cell:nth-child(5) { flex: 0 0 70px; }
  .table-cell:nth-child(6) { flex: 0 0 50px; }
  .table-cell:nth-child(7) { flex: 0 0 50px; }
  .table-cell:nth-child(8) { flex: 0 0 50px; }
  .table-cell:nth-child(9) { flex: 0 0 120px; }
  .table-cell:nth-child(10) { flex: 0 0 80px; }
}
</style>

```
