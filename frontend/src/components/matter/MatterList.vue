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

      <!-- 添加搜索和筛选区域 -->
      <div class="filter-section">
        <div class="search-group">
          <input
            type="text"
            v-model="searchKeyword"
            placeholder="搜索事项名称..."
            @input="filterMatters"
          >
        </div>
        <div class="filter-group">
          <select v-model="filterVersion" @change="filterMatters">
            <option value="">所有版本</option>
            <option v-for="version in versions" :key="version" :value="version">
              {{ version }}
            </option>
          </select>
        </div>
        <div class="filter-group">
          <select v-model="filterStatus" @change="filterMatters">
            <option value="all">全部</option>
            <option value="active">已上线</option>
            <option value="inactive">已下线</option>
          </select>
        </div>
        <div class="filter-group">
          <select v-model="filterPublishStatus" @change="filterMatters">
            <option value="all">所有发布状态</option>
            <option value="published">已发布</option>
            <option value="unpublished">未发布</option>
          </select>
        </div>
        <div class="sort-group">
          <label>排序:</label>
          <select v-model="sortBy" @change="filterMatters">
            <option value="id">ID</option>
            <option value="mainItemName">主项名称</option>
            <option value="subItemName">子项名称</option>
            <option value="grandchildItemName">孙项名称</option>
            <option value="version">版本</option>
            <option value="legalTimeLimit">法定时限</option>
            <option value="committedTimeLimit">承诺时限</option>
            <option value="valid">状态</option>
            <option value="publish">发布状态</option>
          </select>
          <select v-model="sortDirection" @change="filterMatters">
            <option value="desc">降序</option>
            <option value="asc">升序</option>
          </select>
        </div>
      </div>

      <div class="loading" v-if="loading">
        <p>正在加载事项数据...</p>
      </div>

      <div class="error" v-else-if="error">
        <p>加载失败: {{ error }}</p>
        <button @click="fetchMatters">重试</button>
      </div>

      <div class="no-data" v-else-if="filteredMatters.length === 0">
        <p>暂无事项数据</p>
        <button class="add-btn" @click="showAddForm = true">新增第一个事项</button>
      </div>

      <!-- 事项表格 -->
      <div class="matters-table" v-else>
        <div class="table-header">
          <div class="table-cell sortable" @click="sort('id')">
            ID
            <span v-if="sortBy === 'id'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell sortable" @click="sort('mainItemName')">
            主项名称
            <span v-if="sortBy === 'mainItemName'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell sortable" @click="sort('subItemName')">
            子项名称
            <span v-if="sortBy === 'subItemName'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell sortable" @click="sort('grandchildItemName')">
            孙项名称
            <span v-if="sortBy === 'grandchildItemName'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell sortable" @click="sort('version')">
            版本
            <span v-if="sortBy === 'version'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell">法定时限</div>
          <div class="table-cell">承诺时限</div>
          <div class="table-cell sortable" @click="sort('approvalLevel')">
            审批层级
            <span v-if="sortBy === 'approvalLevel'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell">省厅对口指导处室</div>
          <div class="table-cell sortable" @click="sort('publish')">
            发布状态
            <span v-if="sortBy === 'publish'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell sortable" @click="sort('valid')">
            状态
            <span v-if="sortBy === 'valid'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell">操作</div>
        </div>

        <div
          class="table-row"
          v-for="matter in paginatedFilteredMatters"
          :key="matter.id"
          @click="viewMatter(matter)"
        >
          <div class="table-cell">{{ matter.id }}</div>
          <div class="table-cell">
            <div class="matter-name" @click.stop="viewMatter(matter)">
              {{ matter.mainItemName || '-' }}
            </div>
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
      <div class="pagination" v-if="paginatedFilteredMatters.length > 0">
        <div class="pagination-controls">
          <button
            :disabled="currentPage === 1"
            @click="currentPage > 1 && (currentPage--)">
            上一页
          </button>
          <span>第 {{ currentPage }} 页，共 {{ totalPages }} 页 (总计 {{ filteredMatters.length }} 条)</span>
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
      allMatters: [], // 保存所有事项，不进行筛选
      matters: [], // 筛选后的事项
      filteredMatters: [], // 搜索和筛选后的事项
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
      // 添加搜索和筛选相关数据
      searchKeyword: '',
      filterVersion: '',
      filterStatus: 'all',
      filterPublishStatus: 'all',
      // 排序相关数据
      sortBy: 'id',
      sortDirection: 'desc',
      // 版本数据
      versions: []
    };
  },
  computed: {
    totalPages() {
      return Math.ceil(this.filteredMatters.length / this.pageSize);
    },
    paginatedFilteredMatters() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.filteredMatters.slice(start, end);
    }
  },
  async mounted() {
    await this.fetchVersions();
    await this.fetchAllData();
  },
  methods: {
    async fetchAllData() {
      this.loading = true;
      this.error = null;
      this.currentPage = 1;

      try {
        // 并行获取所有数据
        const [matters, materials, approvalDiagrams, businessDiagrams] = await Promise.all([
          matterService.getAllMatters(),
          materialService.getAllMaterials(),
          processDiagramService.getApprovalProcessDiagrams(),
          processDiagramService.getBusinessProcessDiagrams()
        ]);

        this.allMatters = matters;
        this.materialsList = materials;
        this.approvalProcessDiagrams = approvalDiagrams;
        this.businessProcessDiagrams = businessDiagrams;
        
        // 初始化筛选后的数据
        this.filterMatters();
      } catch (error) {
        this.error = error.message || '网络错误';
        console.error('获取数据出错:', error);
      } finally {
        this.loading = false;
      }
    },
    
    async fetchVersions() {
      try {
        this.versions = await matterService.getAllVersions();
      } catch (error) {
        console.error('获取事项版本失败:', error);
      }
    },

    // 添加过滤方法
    filterMatters() {
      let result = [...this.allMatters];

      // 状态筛选
      if (this.filterStatus !== 'all') {
        const isValid = this.filterStatus === 'active';
        result = result.filter(matter => matter.valid === isValid);
      }

      // 发布状态筛选
      if (this.filterPublishStatus !== 'all') {
        const isPublished = this.filterPublishStatus === 'published';
        result = result.filter(matter => matter.publish === isPublished);
      }

      // 关键词搜索
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase();
        result = result.filter(matter =>
          (matter.mainItemName && matter.mainItemName.toLowerCase().includes(keyword)) ||
          (matter.subItemName && matter.subItemName.toLowerCase().includes(keyword)) ||
          (matter.grandchildItemName && matter.grandchildItemName.toLowerCase().includes(keyword)) ||
          (matter.__name__ && matter.__name__.toLowerCase().includes(keyword))
        );
      }

      // 版本筛选
      if (this.filterVersion !== '') {
        const version = parseInt(this.filterVersion);
        if (!isNaN(version)) {
          result = result.filter(matter => matter.version === version);
        }
      }

      // 应用排序
      this.sortMatters(result);
      
      this.filteredMatters = result;
      // 重置到第一页
      this.currentPage = 1;
    },

    handlePageSizeChange() {
      // 当页面大小改变时，重置到第一页
      this.currentPage = 1;
    },

    // 排序功能
    sortMatters(matters) {
      const field = this.sortBy;
      const direction = this.sortDirection;
      
      matters.sort((a, b) => {
        let valueA = a[field];
        let valueB = b[field];
        
        // 特殊处理名称字段
        if (field === 'mainItemName') {
          valueA = a.mainItemName || '';
          valueB = b.mainItemName || '';
        } else if (field === 'subItemName') {
          valueA = a.subItemName || '';
          valueB = b.subItemName || '';
        } else if (field === 'grandchildItemName') {
          valueA = a.grandchildItemName || '';
          valueB = b.grandchildItemName || '';
        }
        
        // 处理日期字段
        if (field === 'createdTime') {
          valueA = new Date(a.createdTime).getTime();
          valueB = new Date(b.createdTime).getTime();
        }
        
        // 处理布尔值字段
        if (field === 'valid' || field === 'publish') {
          valueA = a[field] ? 1 : 0;
          valueB = b[field] ? 1 : 0;
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
      this.filterMatters();
    },

    // 格式化主项名称
    formatMainItemName(mainItemCode, mainItemName) {
      if (mainItemCode && mainItemName) {
        return `${mainItemCode}.${mainItemName}`;
      }
      return mainItemName || '-';
    },

    // 格式化子项名称
    formatSubItemName(mainItemCode, subItemCode, subItemName) {
      if (mainItemCode && subItemCode && subItemName) {
        return `${mainItemCode}.${subItemCode}.${subItemName}`;
      }
      return subItemName || '-';
    },

    // 格式化孙项名称
    formatGrandchildItemName(mainItemCode, subItemCode, grandchildItemCode, grandchildItemName) {
      if (mainItemCode && subItemCode && grandchildItemCode && grandchildItemName) {
        return `${mainItemCode}.${subItemCode}.${grandchildItemCode}.${grandchildItemName}`;
      }
      return grandchildItemName || '-';
    },

    // 获取审批层级描述
    getApprovalLevelDescription(level) {
      const levelObj = this.approvalLevels.find(item => item.name === level);
      return levelObj ? levelObj.description : level;
    },

    // 获取省厅对口指导处室描述
    getProvincialDepartmentOfficeDescription(office) {
      const officeObj = this.provincialDepartmentOffices.find(item => item.name === office);
      return officeObj ? officeObj.description : office;
    },

    editMatter(matter) {
      this.selectedMatter = matter;
    },

    goBackToList() {
      this.selectedMatter = null;
    },

    cancelAdd() {
      this.showAddForm = false;
      this.newMatter = {
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
      };
    },

    async fetchMatters() {
      await this.fetchAllData();
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
      this.newMatter = {
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
      };
      await this.fetchMatters();
    },

    viewMatter(matter) {
      this.selectedMatter = matter;
    },

    async exportCatalog() {
      if (!this.exportVersion) {
        alert('请输入版本号');
        return;
      }

      try {
        const blob = await matterService.exportCatalog(this.exportVersion);
        // 创建下载链接
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `事项目录_v${this.exportVersion}.docx`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      } catch (error) {
        console.error('导出目录失败:', error);
        alert('导出目录失败: ' + (error.message || '未知错误'));
      }
    },

    async exportDocument() {
      if (!this.exportVersion) {
        alert('请输入版本号');
        return;
      }

      try {
        const blob = await matterService.exportDocument(this.exportVersion);
        // 创建下载链接
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `事项文档_v${this.exportVersion}.docx`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      } catch (error) {
        console.error('导出文档失败:', error);
        alert('导出文档失败: ' + (error.message || '未知错误'));
      }
    },

    // 添加resetToListView方法，用于从App.vue中调用返回列表视图
    resetToListView() {
      // 清空选中的事项
      this.selectedMatter = null;
      // 隐藏新增表单
      this.showAddForm = false;
      // 重置分页
      this.currentPage = 1;
      // 重新获取数据确保列表是最新的
      this.fetchMatters();
    },
    
    // 添加显示指定事项详情的方法
    async showMatterDetail(matterId) {
      try {
        // 获取事项详情
        const matter = await matterService.getMatterById(matterId);
        // 设置为选中的事项，触发详情视图
        this.selectedMatter = matter;
      } catch (error) {
        console.error('获取事项详情失败:', error);
        alert('获取事项详情失败: ' + (error.message || '未知错误'));
      }
    }
  },
  watch: {
    filterStatus() {
      this.filterMatters();
    },
    filterPublishStatus() {
      this.filterMatters();
    },
    searchKeyword() {
      this.filterMatters();
    },
    filterVersion() {
      this.filterMatters();
    }
  }
};
</script>

<style scoped>
.matter-list-container {
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

.matters-table {
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 20px;
  max-height: 600px;
  overflow-y: auto;
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
.table-cell:nth-child(1) { flex: 0 0 40px; }   /* ID列 */
.table-cell:nth-child(2) { flex: 1; min-width: 100px; } /* 主项名称列 */
.table-cell:nth-child(3) { flex: 1; min-width: 100px; } /* 子项名称列 */
.table-cell:nth-child(4) { flex: 1; min-width: 100px; } /* 孙项名称列 */
.table-cell:nth-child(5) { flex: 0 0 40px; }  /* 版本列 */
.table-cell:nth-child(6) { flex: 0 0 60px; } /* 法定时限列 */
.table-cell:nth-child(7) { flex: 0 0 60px; } /* 承诺时限列 */
.table-cell:nth-child(8) { flex: 0 0 80px; } /* 审批层级列 */
.table-cell:nth-child(9) { flex: 0 0 120px; } /* 省厅对口指导处室列 */
.table-cell:nth-child(10) { flex: 0 0 80px; } /* 发布状态列 */
.table-cell:nth-child(11) { flex: 0 0 80px; } /* 状态列 */
.table-cell:nth-child(12) { flex: 0 0 120px; } /* 操作列 */

/* 在小屏幕上调整列宽 */
.table-cell:nth-child(1) { flex: 0 0 30px; }
.table-cell:nth-child(2) { flex: 1; min-width: 80px; }
.table-cell:nth-child(3) { flex: 1; min-width: 80px; }
.table-cell:nth-child(4) { flex: 1; min-width: 80px; }
.table-cell:nth-child(5) { flex: 0 0 30px; }
.table-cell:nth-child(6) { flex: 0 0 50px; }
.table-cell:nth-child(7) { flex: 0 0 50px; }
.table-cell:nth-child(8) { flex: 0 0 70px; }
.table-cell:nth-child(9) { flex: 0 0 100px; }
.table-cell:nth-child(10) { flex: 0 0 70px; }
.table-cell:nth-child(11) { flex: 0 0 70px; }
.table-cell:nth-child(12) { flex: 0 0 100px; }
.matter-name {
  color: #007bff;
  text-decoration: underline;
  z-index: 2;
  position: relative;
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

.online-btn {
  background-color: #28a745;
  color: white;
}

.online-btn:hover {
  background-color: #218838;
}

.offline-btn {
  background-color: #ffc107;
  color: #212529;
}

.offline-btn:hover {
  background-color: #e0a800;
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
  .matters-table {
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
}
</style>
