<!-- src/components/version-compare/MatterVersionCompare.vue -->
<template>
  <div class="version-compare-container">
    <div class="header">
      <h2>事项版本对比</h2>
    </div>

    <!-- 版本选择区 -->
    <div class="version-selection">
      <div class="version-select-group">
        <label>旧版本:</label>
        <el-select v-model="oldVersion" placeholder="请选择版本" clearable>
          <el-option
            v-for="version in availableVersions"
            :key="version"
            :label="version"
            :value="version">
          </el-option>
        </el-select>
      </div>

      <div class="version-select-group">
        <label>新版本:</label>
        <el-select v-model="newVersion" placeholder="请选择版本" clearable>
          <el-option
            v-for="version in availableVersions"
            :key="version"
            :label="version"
            :value="version">
          </el-option>
        </el-select>
      </div>

      <el-button
        type="primary"
        @click="compareVersions"
        :loading="isLoading"
        :disabled="!oldVersion || !newVersion || oldVersion === newVersion">
        对比
      </el-button>
    </div>

    <!-- 对比结果区域 -->
    <div class="compare-main" v-if="compareResult">
      <!-- 左侧：分类筛选 -->
      <div class="filter-section">
        <h3>筛选</h3>
        <div class="filter-options">
          <el-radio-group v-model="selectedCategory" class="filter-radio-group">
            <el-radio label="all" class="filter-option">全部</el-radio>
            <el-radio label="ADDED" class="filter-option status-added">
              新增 ({{ addedCount }})
            </el-radio>
            <el-radio label="DELETED" class="filter-option status-deleted">
              删除 ({{ deletedCount }})
            </el-radio>
            <el-radio label="MODIFIED" class="filter-option status-modified">
              修改 ({{ modifiedCount }})
            </el-radio>
            <el-radio label="UNCHANGED" class="filter-option status-unchanged">
              未变更 ({{ unchangedCount }})
            </el-radio>
          </el-radio-group>
        </div>
      </div>

      <!-- 中间：事项列表 -->
      <div class="list-section">
        <div class="list-header">
          <h3>事项列表</h3>
        </div>

        <el-table
          :data="displayItems"
          height="calc(100vh - 250px)"
          style="width: 100%"
          :row-class-name="tableRowClassName">
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <span :class="`status-${scope.row.status.toLowerCase()}`">
                {{ getStatusDisplayName(scope.row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="mainItemCode" label="主项编码" width="120"></el-table-column>
          <el-table-column prop="subItemCode" label="子项编码" width="120"></el-table-column>
          <el-table-column prop="grandchildItemCode" label="孙项编码" width="120"></el-table-column>
          <el-table-column prop="matterName" label="事项名称">
            <template #default="scope">
              {{ scope.row.matterName || '无数据' }}
            </template>
          </el-table-column>
        </el-table>

        <div v-if="displayItems.length === 0" class="no-items">
          没有符合条件的事项
        </div>
      </div>

      <!-- 右侧：统计信息 -->
      <div class="stats-section">
        <h3>统计信息</h3>
        <div class="stats-content">
          <div class="stat-item status-added">
            <span class="stat-label">新增事项:</span>
            <span class="stat-value">{{ addedCount }}</span>
          </div>
          <div class="stat-item status-deleted">
            <span class="stat-label">删除事项:</span>
            <span class="stat-value">{{ deletedCount }}</span>
          </div>
          <div class="stat-item status-modified">
            <span class="stat-label">修改事项:</span>
            <span class="stat-value">{{ modifiedCount }}</span>
          </div>
          <div class="stat-item status-unchanged">
            <span class="stat-label">未变更事项:</span>
            <span class="stat-value">{{ unchangedCount }}</span>
          </div>
          <div class="stat-item stat-total">
            <span class="stat-label">总事项数:</span>
            <span class="stat-value">{{ totalCount }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 无数据提示 -->
    <div class="no-data" v-else-if="!isLoading">
      <p>请选择要对比的版本</p>
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
      compareResult: null, // 现在是一个数组
      selectedCategory: 'all', // all, ADDED, DELETED, MODIFIED, UNCHANGED
      isLoading: false
    };
  },
  async mounted() {
    await this.loadAvailableVersions();
  },
  computed: {
    // 计算各类别的数量
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
    },

    totalCount() {
      return this.compareResult ? this.compareResult.length : 0;
    },

    displayItems() {
      if (!this.compareResult) return [];

      let filteredItems = [];

      if (this.selectedCategory === 'all') {
        filteredItems = [...this.compareResult];
      } else {
        filteredItems = this.compareResult.filter(item => item.status === this.selectedCategory);
      }

      // 转换数据格式用于显示
      return filteredItems.map(item => {
        let matterName = '未知事项';

        // 构建事项名称
        if (item.mainItemCode && item.subItemCode && item.grandchildItemCode) {
          matterName = `${item.mainItemCode}.${item.subItemCode}.${item.grandchildItemCode}`;
        } else if (item.mainItemCode && item.subItemCode) {
          matterName = `${item.mainItemCode}.${item.subItemCode}`;
        } else if (item.mainItemCode) {
          matterName = `${item.mainItemCode}`;
        }

        return {
          mainItemCode: item.mainItemCode,
          subItemCode: item.subItemCode,
          grandchildItemCode: item.grandchildItemCode,
          status: item.status,
          matterName: matterName
        };
      });
    }
  },
  methods: {
    async loadAvailableVersions() {
      try {
        const matters = await MatterService.getAllMatters();
        // 获取所有唯一版本号
        const versions = [];
        matters.forEach(matter => {
          if (matter.version !== null && !versions.includes(matter.version)) {
            versions.push(matter.version);
          }
        });
        // 按版本号降序排列
        versions.sort((a, b) => b - a);
        this.availableVersions = versions;
      } catch (error) {
        console.error('加载版本列表失败:', error);
        this.$message.error('加载版本列表失败: ' + (error.message || '未知错误'));
      }
    },

    async compareVersions() {
      console.log('compareVersions called', this.oldVersion, this.newVersion);
      if (!this.oldVersion || !this.newVersion || this.oldVersion === this.newVersion) {
        this.$message.warning('请选择两个不同的版本进行对比');
        return;
      }

      try {
        this.isLoading = true;
        this.compareResult = null; // 重置之前的结果

        // 使用新的服务调用
        this.compareResult = await MatterToolsService.compareVersions(
          parseInt(this.oldVersion),
          parseInt(this.newVersion)
        );

        this.selectedCategory = 'all'; // 重置筛选条件
      } catch (error) {
        console.error('版本对比失败:', error);
        this.$message.error('版本对比失败: ' + (error.message || '未知错误'));
        this.compareResult = null;
      } finally {
        this.isLoading = false;
      }
    },

    getStatusDisplayName(status) {
      const statusMap = {
        'ADDED': '新增',
        'DELETED': '删除',
        'MODIFIED': '修改',
        'UNCHANGED': '未变更'
      };
      return statusMap[status] || status;
    },

    tableRowClassName({ row }) {
      return 'status-' + row.status.toLowerCase();
    }
  }
};
</script>

<style scoped>
.version-compare-container {
  padding: 20px;
  max-width: 100%;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  color: #303133;
  margin: 0;
}

.version-selection {
  display: flex;
  gap: 20px;
  align-items: end;
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.version-select-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.version-select-group label {
  font-weight: bold;
}

.compare-main {
  display: flex;
  gap: 20px;
  height: calc(100vh - 200px);
}

/* 左侧：分类筛选 */
.filter-section {
  flex: 0 0 180px;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 15px;
}

.filter-section h3 {
  margin-top: 0;
  color: #303133;
}

.filter-radio-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.filter-option {
  margin: 0;
}

.status-added {
  color: #67c23a;
}

.status-deleted {
  color: #f56c6c;
}

.status-modified {
  color: #409eff;
}

.status-unchanged {
  color: #909399;
}

/* 中间：列表页 */
.list-section {
  flex: 1;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.list-header {
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
}

.list-header h3 {
  margin: 0;
  color: #303133;
}

/* 右侧：统计信息 */
.stats-section {
  flex: 0 0 200px;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 15px;
}

.stats-section h3 {
  margin-top: 0;
  color: #303133;
}

.stats-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #ebeef5;
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-label {
  font-weight: 500;
}

.stat-value {
  font-weight: bold;
}

.stat-total {
  font-weight: bold;
  border-top: 2px solid #ebeef5;
  margin-top: 5px;
}

.no-items {
  text-align: center;
  padding: 20px;
  color: #909399;
}

.no-data {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.no-data p {
  font-size: 16px;
  margin: 0;
}

/* 表格行样式 */
.status-added td {
  background-color: #f0f9ff !important;
}

.status-deleted td {
  background-color: #fef0f0 !important;
}

.status-modified td {
  background-color: #ecf5ff !important;
}

.status-unchanged td {
  background-color: #f5f7fa !important;
}

@media (max-width: 768px) {
  .version-selection {
    flex-direction: column;
    align-items: stretch;
  }

  .compare-main {
    flex-direction: column;
    height: auto;
  }

  .filter-section,
  .stats-section {
    flex: none;
  }
}
</style>
