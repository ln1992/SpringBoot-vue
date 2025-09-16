<!-- src/components/version-compare/MatterVersionCompare.vue -->
<template>
  <div class="version-compare-container">
    <!-- 版本对比界面 -->
    <div>
      <div class="header">
        <h2>事项版本对比</h2>
      </div>

      <div class="version-selection-panel">
        <div class="version-selection">
          <div class="version-select-group">
            <label>旧版本:</label>
            <select v-model="oldVersion" @change="onVersionChange">
              <option value="">请选择版本</option>
              <option
                v-for="version in availableVersions"
                :key="version"
                :value="version">
                {{ version }}
              </option>
            </select>
          </div>

          <div class="version-select-group">
            <label>新版本:</label>
            <select v-model="newVersion" @change="onVersionChange">
              <option value="">请选择版本</option>
              <option
                v-for="version in availableVersions"
                :key="version"
                :value="version">
                {{ version }}
              </option>
            </select>
          </div>

          <button
            class="compare-btn"
            @click="compareVersions"
            :disabled="!oldVersion || !newVersion || oldVersion === newVersion || isLoading">
            {{ isLoading ? '对比中...' : '对比' }}
          </button>
        </div>
      </div>

      <!-- 对比结果区域 -->
      <div class="compare-result" v-if="compareResult !== null">
        <div class="result-header">
          <h3>对比结果 (总计: {{ totalCount }})</h3>
        </div>

        <!-- 状态统计 -->
        <div class="status-summary">
          <span class="status-item status-added">新增: {{ addedCount }}</span>
          <span class="status-item status-deleted">删除: {{ deletedCount }}</span>
          <span class="status-item status-modified">修改: {{ modifiedCount }}</span>
          <span class="status-item status-unchanged">未变更: {{ unchangedCount }}</span>
        </div>

        <div class="loading" v-if="isLoading">
          <p>正在加载对比结果...</p>
        </div>

        <div class="error" v-else-if="error">
          <p>加载失败: {{ error }}</p>
          <button @click="compareVersions">重试</button>
        </div>

        <div class="no-data" v-else-if="tableData.length === 0">
          <p>没有找到对比结果</p>
        </div>

        <!-- 对比结果表格 -->
        <div class="compare-table" v-else>
          <div class="table-header">
            <div class="table-cell">状态</div>
            <div class="table-cell">事项名称</div>
          </div>

          <div
            class="table-row"
            v-for="(item, index) in tableData"
            :key="index"
            :class="getRowClass(item)"
          >
            <div class="table-cell">
              <span :class="`status-badge ${getStatusClass(item.status)}`">
                {{ getStatusText(item.status) }}
              </span>
            </div>
            <div class="table-cell">
              {{ item.__name__ || '无数据' }}
            </div>
          </div>
        </div>
      </div>

      <!-- 未选择版本提示 -->
      <div class="no-version-selected" v-else-if="!isLoading && compareResult === null">
        <p>请选择要对比的版本</p>
      </div>
    </div>
  </div>
</template>

<script>
import MatterToolsService from '@/api/matterToolsService';
import MatterService from '@/api/matterService';

export default {
  name: 'MatterVersionCompare',
  data() {
    return {
      availableVersions: [],
      oldVersion: '',
      newVersion: '',
      compareResult: null,
      isLoading: false,
      error: null,
      tableData: []
    };
  },
  computed: {
    totalCount() {
      return this.compareResult ? this.compareResult.length : 0;
    },

    addedCount() {
      if (!this.compareResult) return 0;
      return this.compareResult.filter(item => item.status === 'ADDED').length;
    },

    deletedCount() {
      if (!this.compareResult) return 0;
      return this.compareResult.filter(item => item.status === 'DELETED').length;
    },

    modifiedCount() {
      if (!this.compareResult) return 0;
      return this.compareResult.filter(item => item.status === 'MODIFIED').length;
    },

    unchangedCount() {
      if (!this.compareResult) return 0;
      return this.compareResult.filter(item => item.status === 'UNCHANGED').length;
    }
  },
  async mounted() {
    await this.loadAvailableVersions();
    this.autoSelectLatestVersions();
  },

  methods: {
    async loadAvailableVersions() {
      try {
        // 使用专门的API获取版本列表，避免加载所有事项数据
        const versions = await MatterService.getAllVersions();

        // 确保版本号是数字类型并按降序排列
        const numericVersions = versions
          .map(v => typeof v === 'string' ? parseFloat(v) : v)
          .filter(v => !isNaN(v));

        numericVersions.sort((a, b) => b - a);

        this.availableVersions = numericVersions;
        console.log('加载到的版本列表:', this.availableVersions);
      } catch (error) {
        console.error('加载版本列表失败:', error);
        this.error = '加载版本列表失败: ' + (error.message || '未知错误');
      }
    },

    autoSelectLatestVersions() {
      if (this.availableVersions.length >= 2) {
        const numericVersions = this.availableVersions
          .map(v => parseFloat(v))
          .filter(v => !isNaN(v));

        const sortedVersions = numericVersions.sort((a, b) => b - a);

        if (sortedVersions.length >= 2) {
          this.oldVersion = sortedVersions[1];
          this.newVersion = sortedVersions[0];
        }
      }
    },

    onVersionChange() {
      // 版本选择改变时清空之前的结果
      this.compareResult = null;
      this.tableData = [];
    },

    async compareVersions() {
      if (!this.oldVersion || !this.newVersion || this.oldVersion === this.newVersion) {
        this.error = '请选择两个不同的版本进行对比';
        return;
      }

      try {
        this.isLoading = true;
        this.error = null;
        this.compareResult = null;
        this.tableData = [];

        console.log(`开始对比版本 ${this.oldVersion} 和 ${this.newVersion}`);
        const startTime = performance.now();

        const result = await MatterToolsService.compareVersions(
          parseInt(this.oldVersion),
          parseInt(this.newVersion)
        );

        const endTime = performance.now();
        console.log(`API调用耗时: ${endTime - startTime} 毫秒`);

        // 确保结果是数组格式
        this.compareResult = Array.isArray(result) ? result : [];

        // 性能监控：检查数据量
        console.log(`获取到 ${this.compareResult.length} 条对比结果`);
        if (this.compareResult.length > 1000) {
          console.warn('数据量较大，可能影响页面性能');
        }

        // 设置表格数据
        this.tableData = [...this.compareResult];

        console.log('数据获取完成:', this.compareResult);
        console.log('表格数据:', this.tableData);

      } catch (error) {
        console.error('版本对比失败:', error);
        this.error = '版本对比失败: ' + (error.message || '未知错误');
        this.compareResult = null;
        this.tableData = [];
      } finally {
        this.isLoading = false;
      }
    },

    getStatusText(status) {
      if (!status) return '未知';

      const statusMap = {
        'ADDED': '新增',
        'DELETED': '删除',
        'MODIFIED': '修改',
        'UNCHANGED': '未变更'
      };

      const upperStatus = String(status).toUpperCase();
      return statusMap[upperStatus] || status;
    },

    getStatusClass(status) {
      if (!status) return 'status-unknown';

      const classMap = {
        'ADDED': 'status-added',
        'DELETED': 'status-deleted',
        'MODIFIED': 'status-modified',
        'UNCHANGED': 'status-unchanged'
      };

      const upperStatus = String(status).toUpperCase();
      return classMap[upperStatus] || 'status-unknown';
    },

    getRowClass(item) {
      return `table-row status-${(item.status || 'unknown').toLowerCase()}`;
    }
  }
};
</script>

<style scoped>
.version-compare-container {
  padding: 20px;
  position: relative;
  z-index: 1;
  width: 33.33%; /* 占据左边1/3 */
  float: left; /* 浮动到左侧 */
  box-sizing: border-box;
  border-right: 1px solid #dee2e6;
  height: 100%;
  overflow-y: auto;
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

.version-selection-panel {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
  border: 1px solid #dee2e6;
}

.version-selection {
  display: flex;
  align-items: flex-end;
  gap: 15px; /* 减小间距 */
}

.version-select-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.version-select-group label {
  font-weight: bold;
  color: #495057;
  font-size: 13px; /* 稍微减小字体 */
}

.version-select-group select {
  padding: 6px 10px; /* 减小内边距 */
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 13px; /* 减小字体 */
  min-width: 90px; /* 减小最小宽度 */
}

.compare-btn {
  padding: 6px 12px; /* 减小内边距 */
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px; /* 减小字体 */
  height: 32px; /* 减小高度 */
}

.compare-btn:hover:not(:disabled) {
  background-color: #0056b3;
}

.compare-btn:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.compare-result {
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.result-header h3 {
  margin: 0 0 15px 0;
  color: #333;
}

.status-summary {
  display: flex;
  gap: 15px; /* 减小间距 */
  margin-bottom: 20px;
  padding: 12px; /* 减小内边距 */
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #dee2e6;
  flex-wrap: wrap;
  font-size: 13px; /* 减小字体 */
}

.status-item {
  font-weight: bold;
}

.status-added {
  color: #1890ff;
}

.status-deleted {
  color: #ff4d4f;
}

.status-modified {
  color: #409eff;
}

.status-unchanged {
  color: #909399;
}

.compare-table {
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
  max-height: calc(100vh - 300px);
  overflow-y: auto;
}

.table-header {
  display: flex;
  background-color: #f8f9fa;
  font-weight: bold;
  border-bottom: 1px solid #ddd;
  position: sticky;
  top: 0;
  z-index: 10;
}

.table-row {
  display: flex;
  border-bottom: 1px solid #eee;
  transition: background-color 0.2s;
}

.table-row:hover {
  background-color: #f5f5f5;
}

/* 状态行样式 */
.table-row.status-added {
  background-color: #f0f9ff;
}

.table-row.status-deleted {
  background-color: #fef0f0;
}

.table-row.status-modified {
  background-color: #ecf5ff;
}

.table-row.status-unchanged {
  background-color: #f5f7fa;
}

.table-cell {
  padding: 10px; /* 稍微减小内边距 */
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

/* 调整列宽 - 进一步缩小状态列 */
.table-cell:nth-child(1) { flex: 0 0 60px; } /* 状态列 - 更窄 */
.table-cell:nth-child(2) { flex: 1; min-width: 150px; } /* 事项名称列 - 更宽 */

.status-badge {
  padding: 3px 6px; /* 减小内边距 */
  border-radius: 4px;
  font-size: 12px; /* 保持字体大小 */
  font-weight: bold;
}

.status-added {
  background-color: #f0f9ff;
  color: #1890ff;
  border: 1px solid #e3f2fd;
}

.status-deleted {
  background-color: #fef0f0;
  color: #ff4d4f;
  border: 1px solid #ffdde0;
}

.status-modified {
  background-color: #ecf5ff;
  color: #409eff;
  border: 1px solid #d9ecff;
}

.status-unchanged {
  background-color: #f5f7fa;
  color: #909399;
  border: 1px solid #ebeef5;
}

.status-unknown {
  background-color: #fafafa;
  color: #c0c4cc;
  border: 1px solid #e4e7ed;
}

.loading,
.error,
.no-data,
.no-version-selected {
  text-align: center;
  padding: 40px 20px;
}

.loading p,
.error p,
.no-data p,
.no-version-selected p {
  margin: 0 0 20px 0;
  font-size: 16px;
}

@media (max-width: 768px) {
  .version-compare-container {
    width: 100%;
    float: none;
    border-right: none;
  }

  .version-selection {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }

  .version-select-group select {
    min-width: auto;
  }

  .status-summary {
    flex-direction: column;
    gap: 10px;
  }

  /* 在小屏幕上调整列宽 */
  .table-cell:nth-child(1) { flex: 0 0 60px; }
  .table-cell:nth-child(2) { flex: 1; min-width: 100px; }
}
</style>
