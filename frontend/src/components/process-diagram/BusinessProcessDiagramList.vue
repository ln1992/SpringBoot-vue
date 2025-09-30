<!-- src/components/processdiagram/BusinessProcessDiagramList.vue -->
<template>
  <div class="process-diagram-container">
    <!-- 流程图列表界面 -->
    <div v-if="!selectedDiagram">
      <div class="header">
        <h2>业务流程图管理</h2>
        <div class="header-actions">
          <button class="refresh-btn" @click="fetchDiagrams">刷新</button>
          <button class="add-btn" @click="showAddForm">新增业务流程图</button>
        </div>
      </div>

      <!-- 添加搜索和筛选区域 -->
      <div class="filter-section">
        <div class="search-group">
          <input
            type="text"
            v-model="searchKeyword"
            placeholder="搜索名称..."
            @input="filterDiagrams"
          >
        </div>
        <div class="filter-group">
          <select v-model="filterVersion" @change="filterDiagrams">
            <option value="">所有版本</option>
            <option v-for="version in versions" :key="version" :value="version">
              {{ version }}
            </option>
          </select>
        </div>
        <div class="filter-group">
          <select v-model="filterType" @change="filterDiagrams">
            <option value="">类型</option>
            <option value="PNG">PNG</option>
            <option value="JPEG">JPEG</option>
            <option value="JPG">JPG</option>
            <option value="GIF">GIF</option>
          </select>
        </div>
        <div class="filter-group">
          <select v-model="filterStatus" @change="filterDiagrams">
            <option value="all">状态</option>
            <option value="active">已上线</option>
            <option value="inactive">已下线</option>
          </select>
        </div>
        <div class="sort-group">
          <label>排序:</label>
          <select v-model="sortBy" @change="sortDiagrams">
            <option value="id">ID</option>
            <option value="__name__">名称</option>
            <option value="version">版本</option>
            <option value="imageType">图片类型</option>
            <option value="valid">状态</option>
            <option value="createdTime">创建时间</option>
          </select>
          <select v-model="sortDirection" @change="sortDiagrams">
            <option value="desc">降序</option>
            <option value="asc">升序</option>
          </select>
        </div>
      </div>

      <div class="loading" v-if="loading">
        <p>正在加载业务流程图数据...</p>
      </div>

      <div class="error" v-else-if="error">
        <p>加载失败: {{ error }}</p>
        <button @click="fetchDiagrams">重试</button>
      </div>

      <div class="no-data" v-else-if="filteredDiagrams.length === 0">
        <div class="no-data-content">
          <h3>暂无业务流程图数据</h3>
          <p>点击下方按钮添加您的第一个业务流程图</p>
          <button class="add-btn" @click="showAddForm">新增业务流程图</button>
        </div>
      </div>

      <div class="diagrams-table" v-else>
        <div class="table-header">
          <div class="table-cell sortable" @click="sort('id')">
            ID
            <span v-if="sortBy === 'id'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell sortable" @click="sort('__name__')">
            名称
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
          <div class="table-cell sortable" @click="sort('imageType')">
            图片类型
            <span v-if="sortBy === 'imageType'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell">预览</div>
          <div class="table-cell sortable" @click="sort('valid')">
            状态
            <span v-if="sortBy === 'valid'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell sortable" @click="sort('createdTime')">
            创建时间
            <span v-if="sortBy === 'createdTime'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell">操作</div>
        </div>

        <div
          class="table-row"
          v-for="diagram in paginatedDiagrams"
          :key="diagram.id"
        >
          <div class="table-cell">{{ diagram.id }}</div>
          <div class="table-cell diagram-name" @click="viewDiagramDetail(diagram)">
            {{ diagram.__name__ || diagram.imageName }}
          </div>
          <div class="table-cell">{{ diagram.version }}</div>
          <div class="table-cell">{{ diagram.imageType }}</div>
          <div class="table-cell">
            <img
              v-if="diagram.imageDataUrl"
              :src="diagram.imageDataUrl"
              :alt="diagram.__name__"
              class="preview-image"
            >
            <span v-else>无预览</span>
          </div>
          <div class="table-cell">
            <span :class="['status-badge', diagram.valid ? 'status-valid' : 'status-invalid']">
              {{ diagram.valid ? '已上线' : '已下线' }}
            </span>
          </div>
          <div class="table-cell">{{ formatDate(diagram.createdTime) }}</div>
          <div class="table-cell">
            <div class="action-buttons">
              <button
                v-if="diagram.valid"
                class="deactivate-btn"
                @click.stop="deactivateDiagram(diagram.id)"
              >
                下线
              </button>
              <template v-else>
                <button
                  class="activate-btn"
                  @click.stop="activateDiagram(diagram.id)"
                >
                  上线
                </button>
                <button
                  class="delete-btn"
                  @click.stop="deleteDiagram(diagram.id)"
                  :title="diagram.valid ? '请先下线流程图再删除' : ''"
                >
                  删除
                </button>
              </template>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页控件 -->
      <div class="pagination" v-if="paginatedDiagrams.length > 0">
        <div class="pagination-controls">
          <button
            :disabled="currentPage === 1"
            @click="currentPage > 1 && (currentPage--)">
            上一页
          </button>
          <span>第 {{ currentPage }} 页，共 {{ totalPages }} 页 (总计 {{ filteredDiagrams.length }} 条)</span>
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

    <!-- 流程图详情界面 -->
    <BusinessProcessDiagramDetail
      v-else-if="selectedDiagram"
      :diagram="selectedDiagram"
      @back="selectedDiagram = null"
      @diagram-updated="handleDiagramUpdated"
    />
  </div>
</template>

<script>
import BusinessProcessDiagramDetail from './BusinessProcessDiagramDetail.vue';
import { processDiagramService } from '../../api';

export default {
  name: 'BusinessProcessDiagramList',
  components: {
    BusinessProcessDiagramDetail
  },
  data() {
    return {
      diagrams: [],
      filteredDiagrams: [],
      selectedDiagram: null,
      loading: true,
      error: null,
      currentPage: 1,
      pageSize: 10, // 每页显示10条记录
      // 添加搜索和筛选相关数据
      filterStatus: 'all',
      searchKeyword: '',
      filterVersion: '',
      filterType: '',
      // 排序相关数据
      sortBy: 'id',
      sortDirection: 'desc',
      // 版本数据
      versions: []
    };
  },
  computed: {
    totalPages() {
      return Math.ceil(this.filteredDiagrams.length / this.pageSize);
    },
    paginatedDiagrams() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.filteredDiagrams.slice(start, end);
    }
  },
  async mounted() {
    await this.fetchVersions();
    await this.fetchDiagrams();
  },
  methods: {
    async fetchDiagrams() {
      this.loading = true;
      this.error = null;
      this.currentPage = 1;

      try {
        this.diagrams = await processDiagramService.getBusinessProcessDiagrams();
        // 为每个流程图添加数据URL以便预览
        this.diagrams = this.diagrams.map(diagram => {
          if (diagram.imageData) {
            // 创建数据URL用于图片预览
            const imageData = new Uint8Array(diagram.imageData.data);
            const blob = new Blob([imageData], { type: this.getImageMimeType(diagram.imageType) });
            diagram.imageDataUrl = URL.createObjectURL(blob);
          }
          return diagram;
        });

        // 初始化过滤后的数据
        this.filterDiagrams();
      } catch (error) {
        this.error = error.message || '网络错误';
        console.error('获取业务流程图列表出错:', error);
      } finally {
        this.loading = false;
      }
    },

    async fetchVersions() {
      try {
        this.versions = await processDiagramService.getBusinessProcessDiagramVersions();
      } catch (error) {
        console.error('获取业务流程图版本失败:', error);
      }
    },

    // 添加过滤方法
    filterDiagrams() {
      let result = [...this.diagrams];

      // 状态筛选
      if (this.filterStatus !== 'all') {
        const isValid = this.filterStatus === 'active';
        result = result.filter(diagram => diagram.valid === isValid);
      }

      // 名称关键词搜索
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase();
        result = result.filter(diagram =>
          (diagram.__name__ && diagram.__name__.toLowerCase().includes(keyword)) ||
          (diagram.imageName && diagram.imageName.toLowerCase().includes(keyword))
        );
      }

      // 版本筛选
      if (this.filterVersion !== '') {
        const version = parseInt(this.filterVersion);
        if (!isNaN(version)) {
          result = result.filter(diagram => diagram.version === version);
        }
      }

      // 类型筛选
      if (this.filterType) {
        result = result.filter(diagram => diagram.imageType === this.filterType);
      }

      // 应用排序
      this.sortDiagrams(result);

      this.filteredDiagrams = result;
      // 重置到第一页
      this.currentPage = 1;
    },

    handlePageSizeChange() {
      // 当页面大小改变时，重置到第一页
      this.currentPage = 1;
    },

    // 根据图片类型获取MIME类型
    getImageMimeType(imageType) {
      const mimeTypes = {
        'PNG': 'image/png',
        'JPEG': 'image/jpeg',
        'JPG': 'image/jpeg',
        'GIF': 'image/gif'
      };
      return mimeTypes[imageType] || 'image/png';
    },

    showAddForm() {
      this.selectedDiagram = {};
    },

    viewDiagramDetail(diagram) {
      this.selectedDiagram = diagram;
    },

    async activateDiagram(diagramId) {
      try {
        await processDiagramService.activateBusinessProcessDiagram(diagramId);
        await this.fetchDiagrams();
      } catch (error) {
        console.error('激活业务流程图失败:', error);
        alert('激活业务流程图失败: ' + (error.message || '未知错误'));
      }
    },

    async deactivateDiagram(diagramId) {
      try {
        await processDiagramService.deactivateBusinessProcessDiagram(diagramId);
        await this.fetchDiagrams();
      } catch (error) {
        console.error('停用业务流程图失败:', error);
        alert('停用业务流程图失败: ' + (error.message || '未知错误'));
      }
    },

    async deleteDiagram(diagramId) {
      // 查找要删除的流程图
      const diagram = this.diagrams.find(d => d.id === diagramId);

      // 检查流程图是否已下线，只有已下线的流程图才能删除
      if (diagram && diagram.valid) {
        alert('只能删除已下线的业务流程图，请先下线该流程图再删除。');
        return;
      }

      if (!confirm('确定要删除这个业务流程图吗？')) {
        return;
      }

      try {
        await processDiagramService.deleteBusinessProcessDiagram(diagramId);
        await this.fetchDiagrams();
      } catch (error) {
        console.error('删除业务流程图失败:', error);
        alert('删除业务流程图失败: ' + (error.message || '未知错误'));
      }
    },

    async handleDiagramUpdated() {
      this.selectedDiagram = null;
      await this.fetchDiagrams();
    },

    // 添加resetToListView方法，用于从App.vue中调用返回列表视图
    resetToListView() {
      this.selectedDiagram = null;
    },

    // 增强显示指定流程图详情的方法
    async showDiagramDetail(diagramId) {
      if (!diagramId) return;

      try {
        // 获取流程图详情
        const diagram = await processDiagramService.getBusinessProcessDiagramById(diagramId);
        // 设置为选中的流程图，触发详情视图
        this.selectedDiagram = diagram;

        // 为流程图添加数据URL以便预览
        if (diagram.imageData) {
          // 创建数据URL用于图片预览
          const imageData = new Uint8Array(diagram.imageData.data);
          const blob = new Blob([imageData], { type: this.getImageMimeType(diagram.imageType) });
          diagram.imageDataUrl = URL.createObjectURL(blob);
        }
      } catch (error) {
        console.error('获取业务流程图详情失败:', error);
        alert('获取业务流程图详情失败: ' + (error.message || '未知错误'));
      }
    },

    // 排序功能
    sortDiagrams(diagrams) {
      const field = this.sortBy;
      const direction = this.sortDirection;

      diagrams.sort((a, b) => {
        let valueA = a[field];
        let valueB = b[field];

        // 特殊处理名称字段
        if (field === '__name__') {
          valueA = a.__name__ || a.imageName || '';
          valueB = b.__name__ || b.imageName || '';
        }

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
      this.filterDiagrams();
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
      this.filterDiagrams();
    },
    searchKeyword() {
      this.filterDiagrams();
    },
    filterVersion() {
      this.filterDiagrams();
    },
    filterType() {
      this.filterDiagrams();
    }
  }
};
</script>

<style scoped>
.process-diagram-container {
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

.no-data-content h3 {
  margin-top: 0;
}

.diagrams-table {
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
.table-cell:nth-child(2) { flex: 2; min-width: 200px; } /* 名称列 */
.table-cell:nth-child(3) { flex: 0 0 80px; }  /* 版本列 */
.table-cell:nth-child(4) { flex: 0 0 100px; } /* 图片类型列 */
.table-cell:nth-child(5) { flex: 0 0 120px; } /* 预览列 */
.table-cell:nth-child(6) { flex: 0 0 80px; } /* 状态列 */
.table-cell:nth-child(7) { flex: 0 0 160px; } /* 创建时间列 */
.table-cell:nth-child(8) { flex: 0 0 140px; } /* 操作列 */

.diagram-name {
  color: #007bff;
  text-decoration: underline;
  cursor: pointer;
  z-index: 2;
  position: relative;
}

.diagram-name:hover {
  color: #0056b3;
}

.preview-image {
  max-width: 80px;
  max-height: 40px;
  object-fit: contain;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.status-valid {
  background-color: #d4edda;
  color: #155724;
}

.status-invalid {
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

.activate-btn {
  background-color: #28a745;
  color: white;
}

.activate-btn:hover {
  background-color: #218838;
}

.deactivate-btn {
  background-color: #ffc107;
  color: #212529;
}

.deactivate-btn:hover {
  background-color: #e0a800;
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

.sortable {
  cursor: pointer;
  user-select: none;
}

.sortable:hover {
  background-color: #e6f7ff;
}

@media (max-width: 768px) {
  .diagrams-table {
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
  .table-cell:nth-child(2) { flex: 1; min-width: 100px; }
  .table-cell:nth-child(3) { flex: 0 0 60px; }
  .table-cell:nth-child(4) { flex: 0 0 80px; }
  .table-cell:nth-child(5) { flex: 0 0 100px; }
  .table-cell:nth-child(6) { flex: 0 0 80px; }
  .table-cell:nth-child(7) { flex: 0 0 120px; }
  .table-cell:nth-child(8) { flex: 0 0 140px; }
}
</style>
