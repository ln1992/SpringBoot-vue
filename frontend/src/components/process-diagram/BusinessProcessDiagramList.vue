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

      <div class="loading" v-if="loading">
        <p>正在加载业务流程图数据...</p>
      </div>

      <div class="error" v-else-if="error">
        <p>加载失败: {{ error }}</p>
        <button @click="fetchDiagrams">重试</button>
      </div>

      <div class="no-data" v-else-if="paginatedDiagrams.length === 0">
        <div class="no-data-content">
          <h3>暂无业务流程图数据</h3>
          <p>点击下方按钮添加您的第一个业务流程图</p>
          <button class="add-btn" @click="showAddForm">新增业务流程图</button>
        </div>
      </div>

      <div class="diagrams-table" v-else>
        <div class="table-header">
          <div class="table-cell">ID</div>
          <div class="table-cell">名称</div>
          <div class="table-cell">版本</div>
          <div class="table-cell">图片类型</div>
          <div class="table-cell">预览</div>
          <div class="table-cell">状态</div>
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
          <span>第 {{ currentPage }} 页，共 {{ totalPages }} 页 (总计 {{ diagrams.length }} 条)</span>
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
      selectedDiagram: null,
      loading: true,
      error: null,
      currentPage: 1,
      pageSize: 10 // 每页显示10条记录
    };
  },
  computed: {
    totalPages() {
      return Math.ceil(this.diagrams.length / this.pageSize);
    },
    paginatedDiagrams() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.diagrams.slice(start, end);
    }
  },
  async mounted() {
    await this.fetchDiagrams();
  },
  methods: {
    async fetchDiagrams() {
      this.loading = true;
      this.error = null;

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
      } catch (error) {
        this.error = error.message || '网络错误';
        console.error('获取业务流程图列表出错:', error);
      } finally {
        this.loading = false;
      }
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
    }
  }
};
</script>

<style scoped>
.process-diagram-container {
  padding: 20px;
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

.loading,
.error,
.no-data {
  text-align: center;
  padding: 40px 20px;
}

.loading p,
.error p {
  margin: 0 0 20px 0;
  font-size: 16px;
}

.no-data-content h3 {
  margin-top: 0;
  color: #666;
}

.no-data-content p {
  color: #999;
  margin-bottom: 20px;
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

.table-header .table-cell:nth-child(1),
.table-row .table-cell:nth-child(1) {
  flex: 0 0 60px;
}

.table-header .table-cell:nth-child(2),
.table-row .table-cell:nth-child(2) {
  flex: 1;
}

.table-header .table-cell:nth-child(3),
.table-row .table-cell:nth-child(3) {
  flex: 0 0 80px;
}

.table-header .table-cell:nth-child(4),
.table-row .table-cell:nth-child(4) {
  flex: 0 0 100px;
}

.table-header .table-cell:nth-child(5),
.table-row .table-cell:nth-child(5) {
  flex: 0 0 120px;
}

.table-header .table-cell:nth-child(6),
.table-row .table-cell:nth-child(6) {
  flex: 0 0 100px;
}

.table-header .table-cell:nth-child(7),
.table-row .table-cell:nth-child(7) {
  flex: 0 0 180px;
}

.table-row {
  display: flex;
  border-bottom: 1px solid #eee;
  transition: background-color 0.2s;
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

.diagram-name {
  color: #007bff;
  text-decoration: underline;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 特殊处理需要换行的列 */
.table-cell:nth-child(2),
.table-cell:nth-child(7) {
  align-items: flex-start; /* 顶部对齐 */
}

.preview-image {
  max-width: 100px;
  max-height: 50px;
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

.offline-btn {
  background-color: #ffc107;
  color: #212529;
}

.offline-btn:hover {
  background-color: #e0a800;
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

.delete-btn:hover:not(:disabled) {
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

@media (max-width: 768px) {
  .diagrams-table {
    font-size: 14px;
  }
  
  .table-cell {
    padding: 8px;
  }
  
  .pagination {
    flex-direction: column;
    gap: 15px;
  }
}
</style>