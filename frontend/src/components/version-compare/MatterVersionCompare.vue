<!-- src/components/version-compare/MatterVersionCompare.vue -->
<template>
  <div class="version-compare-container">
    <div class="main-layout">
      <!-- 左侧版本对比面板（始终显示，占1/3宽度） -->
      <div class="left-panel">
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
              @click="showDetail(item)"
            >
              <div class="table-cell">
                <span :class="'status-badge ' + getStatusClass(item.status)">
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

      <!-- 右侧详细信息面板（始终显示，占2/3宽度） -->
      <div class="right-panel">
        <div class="detail-header" v-if="selectedItem">
          <h3>详细对比信息</h3>
          <button class="close-btn" @click="closeDetail">×</button>
        </div>

        <div class="detail-placeholder" v-if="!selectedItem">
          <p>请选择左侧列表中的事项查看详细对比信息</p>
        </div>

        <div class="detail-content" v-else>
          <div class="loading" v-if="isDetailLoading">
            <p>正在加载详细信息...</p>
          </div>

          <div v-else-if="detailData">
            <div class="detail-section old-section">
              <h4>旧版本 (
                <span v-if="getMatterName('old')">
                  事项:
                  <a
                    href="javascript:void(0)"
                    class="matter-link"
                    @click="goToMatterDetail(getMatterId('old'))"
                  >
                    {{ getMatterName('old') }}
                  </a>
                </span>
                <span v-else>
                  ID: {{ getMatterId('old') }}
                </span>
                )</h4>
              <div class="field-list">
                <div
                  v-for="(value, field) in getDetailData('old')"
                  :key="'old-' + field"
                  class="field-item"
                >
                  <span class="field-label">{{ getFieldLabel(field) }}:</span>
                  <span class="field-value" v-html="formatFieldValue(field, value)"></span>
                </div>
                <div v-if="isEmpty(getDetailData('old'))" class="no-diff">
                  无差异字段
                </div>
              </div>
            </div>

            <div class="detail-section new-section">
              <h4>新版本 (
                <span v-if="getMatterName('new')">
                  事项:
                  <a
                    href="javascript:void(0)"
                    class="matter-link"
                    @click="goToMatterDetail(getMatterId('new'))"
                  >
                    {{ getMatterName('new') }}
                  </a>
                </span>
                <span v-else>
                  ID: {{ getMatterId('new') }}
                </span>
                )</h4>
              <div class="field-list">
                <div
                  v-for="(value, field) in getDetailData('new')"
                  :key="'new-' + field"
                  class="field-item"
                >
                  <span class="field-label">{{ getFieldLabel(field) }}:</span>
                  <span class="field-value" v-html="formatFieldValue(field, value)"></span>
                </div>
                <div v-if="isEmpty(getDetailData('new'))" class="no-diff">
                  无差异字段
                </div>
              </div>
            </div>
          </div>

          <div v-else class="no-detail-data">
            <p>暂无详细数据</p>
          </div>
        </div>
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
      isDetailLoading: false,
      error: null,
      tableData: [],
      detailData: null,
      selectedItem: null
    };
  },
  computed: {
    totalCount() {
      return this.compareResult ? this.compareResult.length : 0;
    },

    addedCount() {
      if (!this.compareResult) return 0;
      return this.compareResult.filter(function(item) { return item.status === 'ADDED'; }).length;
    },

    deletedCount() {
      if (!this.compareResult) return 0;
      return this.compareResult.filter(function(item) { return item.status === 'DELETED'; }).length;
    },

    modifiedCount() {
      if (!this.compareResult) return 0;
      return this.compareResult.filter(function(item) { return item.status === 'MODIFIED'; }).length;
    },

    unchangedCount() {
      if (!this.compareResult) return 0;
      return this.compareResult.filter(function(item) { return item.status === 'UNCHANGED'; }).length;
    }
  },
  async mounted() {
    await this.loadAvailableVersions();
    this.autoSelectLatestVersions();

    // 添加全局方法，供innerHTML中的onclick使用
    window.goToMaterialDetail = this.goToMaterialDetail.bind(this);
    window.goToApprovalDiagramDetail = this.goToApprovalDiagramDetail.bind(this);
    window.goToBusinessDiagramDetail = this.goToBusinessDiagramDetail.bind(this);
  },
  beforeDestroy() {
    // 清理全局方法
    delete window.goToMaterialDetail;
    delete window.goToApprovalDiagramDetail;
    delete window.goToBusinessDiagramDetail;
  },

  methods: {
    getDetailData(type) {
      if (!this.detailData) return {};
      return this.detailData[type] || {};
    },

    getMatterId(type) {
      if (!this.selectedItem) return '无';
      if (type === 'old') {
        return this.selectedItem.oldMatterId || '无';
      } else {
        return this.selectedItem.newMatterId || '无';
      }
    },

    // 新增方法：获取事项名称
    getMatterName(type) {
      if (!this.detailData || !this.detailData[type]) return null;

      const matterData = this.detailData[type];
      return matterData.__name__ || null;
    },

    isEmpty(obj) {
      if (!obj) return true;
      return Object.keys(obj).length === 0;
    },

    async loadAvailableVersions() {
      try {
        // 使用专门的API获取版本列表，避免加载所有事项数据
        const versions = await MatterService.getAllVersions();

        // 确保版本号是数字类型并按降序排列
        var numericVersions = versions
          .map(function(v) { return typeof v === 'string' ? parseFloat(v) : v; })
          .filter(function(v) { return !isNaN(v); });

        numericVersions.sort(function(a, b) { return b - a; });

        this.availableVersions = numericVersions;
        console.log('加载到的版本列表:', this.availableVersions);
      } catch (error) {
        console.error('加载版本列表失败:', error);
        this.error = '加载版本列表失败: ' + (error.message || '未知错误');
      }
    },

    autoSelectLatestVersions() {
      if (this.availableVersions.length >= 2) {
        var numericVersions = this.availableVersions
          .map(function(v) { return parseFloat(v); })
          .filter(function(v) { return !isNaN(v); });

        var sortedVersions = numericVersions.sort(function(a, b) { return b - a; });

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
      this.detailData = null;
      this.selectedItem = null;
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
        this.detailData = null;

        console.log('开始对比版本 ' + this.oldVersion + ' 和 ' + this.newVersion);
        var startTime = performance.now();

        var result = await MatterToolsService.compareVersions(
          parseInt(this.oldVersion),
          parseInt(this.newVersion)
        );

        var endTime = performance.now();
        console.log('API调用耗时: ' + (endTime - startTime) + ' 毫秒');

        // 确保结果是数组格式
        this.compareResult = Array.isArray(result) ? result : [];

        // 性能监控：检查数据量
        console.log('获取到 ' + this.compareResult.length + ' 条对比结果');
        if (this.compareResult.length > 1000) {
          console.warn('数据量较大，可能影响页面性能');
        }

        // 设置表格数据
        this.tableData = [].concat(this.compareResult);

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

    async showDetail(item) {
      console.log('点击事项:', item);

      // 直接调用API获取详细信息，不再做额外判断
      // 后端已经根据事项状态处理了所有情况：
      // - 新增事项：{old: {}, new: {完整数据}}
      // - 删除事项：{old: {完整数据}, new: {}}
      // - 修改事项：{old: {变更旧数据}, new: {变更新数据}}
      // - 未变更事项：{old: {}, new: {}}
      try {
        this.isDetailLoading = true;
        this.selectedItem = item;
        this.detailData = null; // 重置详细数据

        console.log('获取详细对比信息: oldMatterId=' + item.oldMatterId + ', newMatterId=' + item.newMatterId);

        var detailResult = await MatterToolsService.compareMatters(
          item.oldMatterId,
          item.newMatterId
        );

        console.log('详细对比结果:', detailResult);
        this.detailData = detailResult;
      } catch (error) {
        console.error('获取详细对比信息失败:', error);
        // 显示错误信息
        this.detailData = {
          old: { error: '加载失败: ' + (error.message || '未知错误') },
          new: { error: '加载失败: ' + (error.message || '未知错误') }
        };
      } finally {
        this.isDetailLoading = false;
      }
    },

    // 新增方法：跳转到事项详情
    goToMatterDetail(matterId) {
      if (matterId && matterId !== '无') {
        // 在新窗口中打开事项详情页面
        var url = window.location.origin + window.location.pathname + '#/matters/' + matterId;
        window.open(url, '_blank');
      }
    },

    // 新增方法：跳转到材料详情
    goToMaterialDetail(materialId) {
      if (materialId) {
        // 在新窗口中打开材料详情页面
        var url = window.location.origin + window.location.pathname + '#/materials/' + materialId;
        window.open(url, '_blank');
      }
    },

    // 新增方法：跳转到审批流程图详情
    goToApprovalDiagramDetail(diagramId) {
      if (diagramId) {
        // 在新窗口中打开审批流程图详情页面
        var url = window.location.origin + window.location.pathname + '#/approval-diagrams/' + diagramId;
        window.open(url, '_blank');
      }
    },

    // 新增方法：跳转到业务流程图详情（修复URL路径）
    goToBusinessDiagramDetail(diagramId) {
      if (diagramId) {
        // 修复URL路径，从approval-diagrams改为business-diagrams
        var url = window.location.origin + window.location.pathname + '#/business-diagrams/' + diagramId;
        window.open(url, '_blank');
      }
    },

    closeDetail() {
      this.detailData = null;
      this.selectedItem = null;
      this.isDetailLoading = false;
    },

    getStatusText(status) {
      if (!status) return '未知';

      var statusMap = {
        'ADDED': '新增',
        'DELETED': '删除',
        'MODIFIED': '修改',
        'UNCHANGED': '未变更'
      };

      var upperStatus = String(status).toUpperCase();
      return statusMap[upperStatus] || status;
    },

    getStatusClass(status) {
      if (!status) return 'status-unknown';

      var classMap = {
        'ADDED': 'status-added',
        'DELETED': 'status-deleted',
        'MODIFIED': 'status-modified',
        'UNCHANGED': 'status-unchanged'
      };

      var upperStatus = String(status).toUpperCase();
      return classMap[upperStatus] || 'status-unknown';
    },

    getRowClass(item) {
      return 'table-row status-' + (item.status || 'unknown').toLowerCase();
    },

    getFieldLabel(field) {
      var fieldLabels = {
        'mainItemCode': '主项编码',
        'subItemCode': '子项编码',
        'grandchildItemCode': '孙项编码',
        'mainItemName': '主项名称',
        'subItemName': '子项名称',
        'grandchildItemName': '孙项名称',
        'bases': '设定依据',
        'materialIds': '材料ID',
        'legalTimeLimit': '法定时限',
        'committedTimeLimit': '承诺时限',
        'approvalLevel': '审批层级',
        'provincialDepartmentOffice': '省厅对口指导处室',
        'approvalProcessDiagramId': '审批流程图ID',
        'businessProcessDiagramId': '业务流程图ID',
        '__name__': '事项名称',
        'version': '版本',
        'valid': '是否有效',
        'publish': '是否发布'
      };
      return fieldLabels[field] || field;
    },

    formatFieldValue(field, value) {
      if (value === null || value === undefined) {
        return '无';
      }

      if (field === 'valid' || field === 'publish') {
        return value ? '是' : '否';
      }

      // 特殊处理审批层级字段
      if (field === 'approvalLevel') {
        var approvalLevels = {
          'PROVINCIAL': '省级',
          'PROVINCIAL_MUNICIPAL': '省市两级',
          'PROVINCIAL_MUNICIPAL_COUNTY': '省市县三级',
          'MUNICIPAL': '设区的市',
          'MUNICIPAL_COUNTY': '市县两级',
          'COUNTY': '县级'
        };
        return approvalLevels[value] || value;
      }

      // 特殊处理省厅对口指导处室字段
      if (field === 'provincialDepartmentOffice') {
        var provincialOffices = {
          'PROVINCIAL_DEPARTMENT_POLICY_REGULATIONS': '厅政策法规处',
          'PROVINCIAL_DEPARTMENT_ADMINISTRATIVE_APPROVAL': '厅行政审批处',
          'PROVINCIAL_DEPARTMENT_TRANSPORTATION_MANAGEMENT': '厅运输管理处',
          'PROVINCIAL_PORT_CENTER_CONSTRUCTION': '省港航中心建设处',
          'PROVINCIAL_PORT_CENTER_MANAGEMENT': '省港航中心管理处',
          'PROVINCIAL_HIGHWAY_CENTER_CONSTRUCTION': '省公路中心建设处',
          'PROVINCIAL_HIGHWAY_CENTER_MAINTENANCE': '省公路中心养护处'
        };
        return provincialOffices[value] || value;
      }

      // 特殊处理materialIds字段，添加跳转链接
      if (field === 'materialIds' && Array.isArray(value) && value.length > 0) {
        var materialLinks = value.map(function(id) {
          // 为每个材料ID创建跳转链接
          return '<a href="javascript:void(0)" onclick="window.goToMaterialDetail(' + id + ')">' + id + '</a>';
        });
        return materialLinks.join(', ');
      }

      // 特殊处理流程图ID字段
      if (field === 'approvalProcessDiagramId' && value) {
        return '<a href="javascript:void(0)" onclick="window.goToApprovalDiagramDetail(' + value + ')">' + value + '</a>';
      }

      if (field === 'businessProcessDiagramId' && value) {
        return '<a href="javascript:void(0)" onclick="window.goToBusinessDiagramDetail(' + value + ')">' + value + '</a>';
      }

      return String(value);
    }
  }
};
</script>

<style scoped>
.version-compare-container {
  padding: 20px;
  position: relative;
  z-index: 1;
  height: 100%;
  box-sizing: border-box;
}

.main-layout {
  display: flex;
  height: 100%;
  gap: 20px;
}

.left-panel {
  flex: 1; /* 占1/3宽度 */
  background-color: #fff;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  min-width: 300px;
}

.right-panel {
  flex: 2; /* 占2/3宽度 */
  background-color: #fff;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
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
  gap: 15px;
}

.version-select-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.version-select-group label {
  font-weight: bold;
  color: #495057;
  font-size: 13px;
}

.version-select-group select {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 13px;
  min-width: 90px;
}

.compare-btn {
  padding: 6px 12px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  height: 32px;
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
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.result-header h3 {
  margin: 0 0 15px 0;
  color: #333;
}

.status-summary {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #dee2e6;
  flex-wrap: wrap;
  font-size: 13px;
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
  flex: 1;
  display: flex;
  flex-direction: column;
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
  padding: 10px;
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

/* 调整列宽 */
.table-cell:nth-child(1) { flex: 0 0 60px; }
.table-cell:nth-child(2) { flex: 1; min-width: 150px; }

.status-badge {
  padding: 3px 6px;
  border-radius: 4px;
  font-size: 12px;
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

/* 右侧详细信息面板样式 */
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #dee2e6;
}

.detail-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #333;
}

.detail-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
  font-size: 16px;
}

.detail-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
}

.detail-section {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.detail-section h4 {
  margin: 0 0 15px 0;
  color: #333;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

.old-section h4 {
  color: #ff4d4f;
}

.new-section h4 {
  color: #1890ff;
}

.field-list {
  flex: 1;
  overflow-y: auto;
}

.field-item {
  display: flex;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.field-label {
  font-weight: bold;
  min-width: 120px;
  margin-right: 10px;
  color: #666;
}

.field-value {
  flex: 1;
  word-break: break-word;
}

.no-diff {
  text-align: center;
  color: #999;
  padding: 20px;
}

.no-detail-data {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

/* 添加事项链接样式 */
.matter-link {
  color: #007bff;
  text-decoration: none;
  border-bottom: 1px dashed #007bff;
  cursor: pointer;
}

.matter-link:hover {
  color: #0056b3;
  border-bottom-style: solid;
}

@media (max-width: 768px) {
  .main-layout {
    flex-direction: column;
  }

  .left-panel,
  .right-panel {
    min-width: auto;
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












