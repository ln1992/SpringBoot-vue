<!-- src/components/matter/MatterList.vue -->
<template>
  <div class="matter-list-container">
    <!-- 事项列表界面 -->
    <div v-if="!selectedMatter && !showAddForm">
      <div class="header">
        <h2>事项清单</h2>
        <div class="header-actions">
          <button class="refresh-btn" @click="fetchMatters">刷新</button>
          <button class="add-btn" @click="showAddForm = true">新增事项</button>
        </div>
      </div>

      <!-- 导出功能区 -->
      <div class="export-section">
        <div class="version-input">
          <label for="version">版本:</label>
          <input
            id="version"
            type="text"
            v-model="exportVersion"
            placeholder="请输入版本号"
          />
        </div>
        <div class="export-buttons">
          <button
            class="export-catalog-btn"
            @click="exportCatalog"
            :disabled="!exportVersion"
          >
            导出目录
          </button>
          <button
            class="export-document-btn"
            @click="exportDocument"
            :disabled="!exportVersion"
          >
            导出文档
          </button>
        </div>
      </div>

      <div class="loading" v-if="loading">
        <p>正在加载事项数据...</p>
      </div>

      <div class="error" v-else-if="error">
        <p>加载失败: {{ error }}</p>
        <button @click="fetchMatters">重试</button>
      </div>

      <div class="no-data" v-else-if="paginatedMatters.length === 0">
        <p>暂无事项数据</p>
        <button class="add-btn" @click="showAddForm = true">新增第一个事项</button>
      </div>

      <!-- 事项表格 -->
      <div class="matters-table" v-else>
        <div class="table-header">
          <div class="table-cell">ID</div>
          <div class="table-cell">主项名称</div>
          <div class="table-cell">子项名称</div>
          <div class="table-cell">孙项名称</div>
          <div class="table-cell">版本</div>
          <div class="table-cell">法定时限</div>
          <div class="table-cell">承诺时限</div>
          <div class="table-cell">审批层级</div>
          <div class="table-cell">省厅对口指导处室</div>
          <div class="table-cell">发布状态</div>
          <div class="table-cell">状态</div>
          <div class="table-cell">操作</div>
        </div>

        <div
          class="table-row"
          v-for="matter in paginatedMatters"
          :key="matter.id"
          @click="editMatter(matter)"
        >
          <div class="table-cell">{{ matter.id }}</div>
          <div class="table-cell matter-name">
            {{ formatMainItemName(matter.mainItemCode, matter.mainItemName) }}
          </div>
          <div class="table-cell">
            {{ formatSubItemName(matter.mainItemCode, matter.subItemCode, matter.subItemName) }}
          </div>
          <div class="table-cell">
            {{ formatGrandchildItemName(matter.mainItemCode, matter.subItemCode, matter.grandchildItemCode, matter.grandchildItemName) }}
          </div>
          <div class="table-cell">{{ matter.version || '-' }}</div>
          <div class="table-cell">{{ matter.legalTimeLimit || '-' }}天</div>
          <div class="table-cell">{{ matter.committedTimeLimit || '-' }}天</div>
          <div class="table-cell">{{ getApprovalLevelDescription(matter.approvalLevel) || matter.approvalLevel || '-' }}</div>
          <div class="table-cell">{{ getProvincialDepartmentOfficeDescription(matter.provincialDepartmentOffice) || matter.provincialDepartmentOffice || '-' }}</div>
          <div class="table-cell">
            <span :class="['status-badge', matter.publish ? 'status-active' : 'status-inactive']">
              {{ matter.publish ? '已发布' : '未发布' }}
            </span>
          </div>
          <div class="table-cell">
            <span :class="['status-badge', matter.valid ? 'status-active' : 'status-inactive']">
              {{ matter.valid ? '已上线' : '已下线' }}
            </span>
          </div>
          <div class="table-cell">
            <div class="action-buttons">
              <!-- 状态操作按钮 -->
              <template v-if="matter.valid">
                <button
                  class="offline-btn"
                  @click.stop="toggleMatterStatus(matter.id, false)"
                >
                  下线
                </button>
              </template>
              <template v-else>
                <button
                  class="online-btn"
                  @click.stop="toggleMatterStatus(matter.id, true)"
                >
                  上线
                </button>
                <button
                  class="delete-btn"
                  @click.stop="deleteMatter(matter.id)"
                >
                  删除
                </button>
              </template>

              <!-- 发布操作按钮 -->
              <template v-if="matter.valid && !matter.publish">
                <button
                  class="publish-btn"
                  @click.stop="togglePublishStatus(matter.id, true)"
                >
                  发布
                </button>
              </template>
              <template v-else-if="matter.valid && matter.publish">
                <button
                  class="unpublish-btn"
                  @click.stop="togglePublishStatus(matter.id, false)"
                >
                  取消发布
                </button>
              </template>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页控件 -->
      <div class="pagination" v-if="paginatedMatters.length > 0">
        <div class="pagination-controls">
          <button
            :disabled="currentPage === 1"
            @click="currentPage > 1 && (currentPage--)">
            上一页
          </button>
          <span>第 {{ currentPage }} 页，共 {{ totalPages }} 页 (总计 {{ matters.length }} 条)</span>
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

    <!-- 事项详情界面 -->
    <MatterDetail
      v-else-if="selectedMatter"
      :matter="selectedMatter"
      :materials-list="materialsList"
      :approval-process-diagrams="approvalProcessDiagrams"
      :business-process-diagrams="businessProcessDiagrams"
      @back="goBackToList"
      @matter-updated="handleMatterUpdated"
    />

    <!-- 新增事项界面 -->
    <MatterDetail
      v-else-if="showAddForm"
      :matter="newMatter"
      :materials-list="materialsList"
      :approval-process-diagrams="approvalProcessDiagrams"
      :business-process-diagrams="businessProcessDiagrams"
      @back="cancelAdd"
      @matter-updated="handleMatterAdded"
    />
  </div>
</template>

<script>
// 引入详情组件
import MatterDetail from './MatterDetail.vue';
import { matterService, materialService, processDiagramService } from '../../api';

export default {
  name: 'MatterList',
  components: {
    MatterDetail
  },
  data() {
    return {
      matters: [],
      materialsList: [], // 存储所有材料列表
      approvalProcessDiagrams: [], // 存储所有审批流程图
      businessProcessDiagrams: [], // 存储所有业务流程图
      loading: true,
      error: null,
      selectedMatter: null,
      showAddForm: false,
      newMatter: {
        id: null,
        version: null,
        mainItemCode: null,
        subItemCode: null,
        grandchildItemCode: null,
        mainItemName: '',
        subItemName: '',
        grandchildItemName: '',
        bases: [],
        materialIds: [],
        legalTimeLimit: null,
        committedTimeLimit: null,
        approvalLevel: '',
        provincialDepartmentOffice: '',
        isValid: true,
        isPublish: false,
        approvalProcessDiagramId: null,
        businessProcessDiagramId: null
      },
      // 审批层级枚举
      approvalLevels: [
        { name: 'PROVINCIAL', description: '省级' },
        { name: 'PROVINCIAL_MUNICIPAL', description: '省市两级' },
        { name: 'PROVINCIAL_MUNICIPAL_COUNTY', description: '省市县三级' },
        { name: 'MUNICIPAL', description: '设区的市' },
        { name: 'MUNICIPAL_COUNTY', description: '市县两级' },
        { name: 'COUNTY', description: '县级' }
      ],
      // 省厅对口指导处室枚举
      provincialDepartmentOffices: [
        { name: 'PROVINCIAL_DEPARTMENT_POLICY_REGULATIONS', description: '厅政策法规处' },
        { name: 'PROVINCIAL_DEPARTMENT_ADMINISTRATIVE_APPROVAL', description: '厅行政审批处' },
        { name: 'PROVINCIAL_DEPARTMENT_TRANSPORTATION_MANAGEMENT', description: '厅运输管理处' },
        { name: 'PROVINCIAL_PORT_CENTER_CONSTRUCTION', description: '省港航中心建设处' },
        { name: 'PROVINCIAL_PORT_CENTER_MANAGEMENT', description: '省港航中心管理处' },
        { name: 'PROVINCIAL_HIGHWAY_CENTER_CONSTRUCTION', description: '省公路中心建设处' },
        { name: 'PROVINCIAL_HIGHWAY_CENTER_MAINTENANCE', description: '省公路中心养护处' }
      ],
      currentPage: 1,
      pageSize: 10, // 每页显示10条记录
      exportVersion: '' // 导出版本号
    };
  },
  computed: {
    totalPages() {
      return Math.ceil(this.matters.length / this.pageSize);
    },
    paginatedMatters() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.matters.slice(start, end);
    }
  },
  async mounted() {
    await this.fetchAllData();
  },
  methods: {
    async fetchAllData() {
      this.loading = true;
      this.error = null;

      try {
        // 并行获取所有数据
        const [matters, materials, approvalDiagrams, businessDiagrams] = await Promise.all([
          matterService.getAllMatters(),
          materialService.getAllMaterials(),
          processDiagramService.getApprovalProcessDiagrams(),
          processDiagramService.getBusinessProcessDiagrams()
        ]);

        this.matters = matters;
        this.materialsList = materials;
        this.approvalProcessDiagrams = approvalDiagrams;
        this.businessProcessDiagrams = businessDiagrams;
      } catch (error) {
        this.error = error.message || '网络错误';
        console.error('获取数据出错:', error);
      } finally {
        this.loading = false;
      }
    },

    async fetchMatters() {
      await this.fetchAllData();
    },

    handlePageSizeChange() {
      // 当页面大小改变时，重置到第一页
      this.currentPage = 1;
    },

    // 获取审批层级描述
    getApprovalLevelDescription(level) {
      const levelItem = this.approvalLevels.find(item => item.name === level);
      return levelItem ? levelItem.description : level;
    },

    // 获取省厅对口指导处室描述
    getProvincialDepartmentOfficeDescription(office) {
      const officeItem = this.provincialDepartmentOffices.find(item => item.name === office);
      return officeItem ? officeItem.description : office;
    },

    // 格式化主项名称
    formatMainItemName(mainItemCode, mainItemName) {
      if (!mainItemCode || !mainItemName) return mainItemName || '-';
      return `${mainItemCode}.${mainItemName}`;
    },

    // 格式化子项名称
    formatSubItemName(mainItemCode, subItemCode, subItemName) {
      if (!mainItemCode || !subItemCode || !subItemName) return subItemName || '-';
      return `${mainItemCode}.${subItemCode}.${subItemName}`;
    },

    // 格式化孙项名称
    formatGrandchildItemName(mainItemCode, subItemCode, grandchildItemCode, grandchildItemName) {
      if (!mainItemCode || !subItemCode || !grandchildItemCode || !grandchildItemName)
        return grandchildItemName || '-';
      return `${mainItemCode}.${subItemCode}.${grandchildItemCode}.${grandchildItemName}`;
    },

    editMatter(matter) {
      this.selectedMatter = matter;
    },

    goBackToList() {
      this.selectedMatter = null;
    },

    cancelAdd() {
      this.showAddForm = false;
    },

    async toggleMatterStatus(matterId, valid) {
      try {
        if (valid) {
          await matterService.activateMatter(matterId);
        } else {
          await matterService.deactivateMatter(matterId);
        }
        await this.fetchMatters();
      } catch (error) {
        console.error('更新事项状态失败:', error);
        alert('更新事项状态失败: ' + (error.message || '未知错误'));
      }
    },

    async togglePublishStatus(matterId, publish) {
      try {
        if (publish) {
          await matterService.publishMatter(matterId);
        } else {
          await matterService.unpublishMatter(matterId);
        }
        await this.fetchMatters();
      } catch (error) {
        console.error('更新发布状态失败:', error);
        alert('更新发布状态失败: ' + (error.message || '未知错误'));
      }
    },

    async deleteMatter(matterId) {
      if (!confirm('确定要删除这个事项吗？')) {
        return;
      }

      try {
        await matterService.deleteMatter(matterId);
        await this.fetchMatters();
      } catch (error) {
        console.error('删除事项失败:', error);
        alert('删除事项失败: ' + (error.message || '未知错误'));
      }
    },

    async handleMatterUpdated() {
      this.selectedMatter = null;
      await this.fetchMatters();
    },

    async handleMatterAdded() {
      this.showAddForm = false;
      await this.fetchMatters();
    },

    // 添加显示指定事项详情的方法（供父组件调用）
    async showMatterDetail(matterId) {
      try {
        // 获取指定ID的事项详情
        const matter = await matterService.getMatterById(matterId);
        if (matter) {
          this.selectedMatter = matter;
        } else {
          alert('未找到指定的事项');
        }
      } catch (error) {
        console.error('获取事项详情失败:', error);
        alert('获取事项详情失败: ' + (error.message || '未知错误'));
      }
    },

    // 重置到列表视图的方法（供父组件调用）
    resetToListView() {
      this.selectedMatter = null;
      this.showAddForm = false;
    },

    // 导出目录
    async exportCatalog() {
      if (!this.exportVersion) {
        alert('请输入版本号');
        return;
      }

      try {
        const blob = await matterService.exportMattersCatalog(this.exportVersion);
        this.downloadBlob(blob, `事项目录_v${this.exportVersion}.docx`);
      } catch (error) {
        console.error('导出目录失败:', error);
        alert('导出目录失败: ' + (error.message || '未知错误'));
      }
    },

    // 导出文档
    async exportDocument() {
      if (!this.exportVersion) {
        alert('请输入版本号');
        return;
      }

      try {
        const blob = await matterService.exportMattersDocuments(this.exportVersion);
        this.downloadBlob(blob, `事项文档_v${this.exportVersion}.docx`);
      } catch (error) {
        console.error('导出文档失败:', error);
        alert('导出文档失败: ' + (error.message || '未知错误'));
      }
    },

    // 下载文件
    downloadBlob(blob, filename) {
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = filename;
      link.style.display = 'none';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    }
  }
};
</script>

<style scoped>
.matter-list-container {
  padding: 20px;
  position: relative;
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

.export-section {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.version-input {
  display: flex;
  align-items: center;
  gap: 8px;
}

.version-input label {
  font-weight: bold;
}

.version-input input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.export-buttons {
  display: flex;
  gap: 10px;
}

.export-catalog-btn,
.export-document-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.export-catalog-btn {
  background-color: #28a745;
  color: white;
}

.export-catalog-btn:hover:not(:disabled) {
  background-color: #218838;
}

.export-document-btn {
  background-color: #007bff;
  color: white;
}

.export-document-btn:hover:not(:disabled) {
  background-color: #0069d9;
}

.export-catalog-btn:disabled,
.export-document-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.matters-table {
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
  min-width: 0; /* 添加此属性以防止内容溢出 */
  word-wrap: break-word; /* 允许长单词换行 */
  word-break: break-word; /* 允许单词内换行 */
  white-space: normal; /* 允许正常换行 */
}

.table-cell:last-child {
  border-right: none;
}

.matter-name {
  color: #007bff;
  text-decoration: underline;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: normal;
  word-wrap: break-word;
  word-break: break-word;
}

/* 进一步调整列宽设置，使所有列更窄以便完整显示 */
.table-cell:nth-child(1) { flex: 0 0 30px; }   /* ID列 */
.table-cell:nth-child(2) { flex: 1; min-width: 80px; max-width: 120px; } /* 主项名称列 */
.table-cell:nth-child(3) { flex: 1; min-width: 80px; max-width: 120px; } /* 子项名称列 */
.table-cell:nth-child(4) { flex: 1; min-width: 80px; max-width: 120px; } /* 孙项名称列 */
.table-cell:nth-child(5) { flex: 0 0 40px; }  /* 版本列 */
.table-cell:nth-child(6) { flex: 0 0 50px; }  /* 法定时限列 */
.table-cell:nth-child(7) { flex: 0 0 50px; }  /* 承诺时限列 */
.table-cell:nth-child(8) { flex: 0 0 70px; } /* 审批层级列 */
.table-cell:nth-child(9) { flex: 1; min-width: 100px; max-width: 150px; } /* 省厅对口指导处室列 */
.table-cell:nth-child(10) { flex: 0 0 50px; } /* 发布状态列 */
.table-cell:nth-child(11) { flex: 0 0 50px; } /* 状态列 */
.table-cell:nth-child(12) { flex: 0 0 100px; }/* 操作列 */

/* 特殊处理需要换行的列 */
.table-cell:nth-child(2),
.table-cell:nth-child(3),
.table-cell:nth-child(4),
.table-cell:nth-child(8),
.table-cell:nth-child(9) {
  align-items: flex-start; /* 顶部对齐 */
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

.online-btn {
  background-color: #28a745;
  color: white;
}

.online-btn:hover {
  background-color: #218838;
}

.delete-btn {
  background-color: #dc3545;
  color: white;
}

.delete-btn:hover {
  background-color: #c82333;
}

.publish-btn {
  background-color: #007bff;
  color: white;
}

.publish-btn:hover {
  background-color: #0069d9;
}

.unpublish-btn {
  background-color: #6c757d;
  color: white;
}

.unpublish-btn:hover {
  background-color: #5a6268;
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
  z-index: 10; /* 添加 z-index 确保分页控件在最上层 */
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
  position: relative;
  z-index: 11; /* 确保下拉框在分页控件之上 */
}

.pagination button {
  padding: 8px 16px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  position: relative;
  z-index: 11; /* 确保按钮在分页控件之上 */
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
  .matters-table {
    font-size: 14px;
  }

  .table-cell {
    padding: 8px;
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

  .export-section {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
