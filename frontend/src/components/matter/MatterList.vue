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

      <div class="loading" v-if="loading">
        <p>正在加载事项数据...</p>
      </div>

      <div class="error" v-else-if="error">
        <p>加载失败: {{ error }}</p>
        <button @click="fetchMatters">重试</button>
      </div>

      <div class="no-data" v-else-if="matters.length === 0">
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
          <div class="table-cell">法定时限</div>
          <div class="table-cell">承诺时限</div>
          <div class="table-cell">审批层级</div>
          <div class="table-cell">省厅对口指导处室</div>
          <div class="table-cell">版本</div>
          <div class="table-cell">发布状态</div>
          <div class="table-cell">状态</div>
          <div class="table-cell">操作</div>
        </div>

        <div
          class="table-row"
          v-for="matter in matters"
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
          <div class="table-cell">{{ matter.legalTimeLimit || '-' }}天</div>
          <div class="table-cell">{{ matter.committedTimeLimit || '-' }}天</div>
          <div class="table-cell">{{ getApprovalLevelDescription(matter.approvalLevel) || matter.approvalLevel || '-' }}</div>
          <div class="table-cell">{{ getProvincialDepartmentOfficeDescription(matter.provincialDepartmentOffice) || matter.provincialDepartmentOffice || '-' }}</div>
          <div class="table-cell">{{ matter.version || '-' }}</div>
          <div class="table-cell">
            <span :class="['status-badge', matter.isPublish ? 'status-active' : 'status-inactive']">
              {{ matter.isPublish ? '已发布' : '未发布' }}
            </span>
          </div>
          <div class="table-cell">
            <span :class="['status-badge', matter.isValid ? 'status-active' : 'status-inactive']">
              {{ matter.isValid ? '已上线' : '已下线' }}
            </span>
          </div>
          <div class="table-cell">
            <div class="action-buttons">
              <!-- 状态操作按钮 -->
              <template v-if="matter.isValid">
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
              <template v-if="matter.isValid && !matter.isPublish">
                <button
                  class="publish-btn"
                  @click.stop="togglePublishStatus(matter.id, true)"
                >
                  发布
                </button>
              </template>
              <template v-else-if="matter.isValid && matter.isPublish">
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

// API端点常量
const API_BASE_URL = 'http://localhost:8000/api/matters'
const API_MATERIALS_URL = 'http://localhost:8000/api/materials'
const API_GET_ALL = API_BASE_URL
const API_CREATE = API_BASE_URL
const API_UPDATE = (id) => `${API_BASE_URL}/${id}`
const API_DELETE = (id) => `${API_BASE_URL}/${id}`
const API_ACTIVATE = (id) => `${API_BASE_URL}/${id}/activate`
const API_DEACTIVATE = (id) => `${API_BASE_URL}/${id}/deactivate`
const API_PUBLISH = (id) => `${API_BASE_URL}/${id}/publish`
const API_UNPUBLISH = (id) => `${API_BASE_URL}/${id}/unpublish`

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
      ]
    }
  },
  async mounted() {
    await this.fetchMatters()
    await this.fetchMaterials() // 获取材料列表
    await this.fetchProcessDiagrams() // 获取流程图列表
  },
  methods: {
    async fetchMatters() {
      this.loading = true
      this.error = null
      this.selectedMatter = null

      try {
        const response = await fetch(API_GET_ALL)

        if (response.ok) {
          const contentType = response.headers.get('content-type')
          if (contentType && contentType.includes('application/json')) {
            this.matters = await response.json()
          } else {
            throw new Error('服务器返回的不是JSON格式数据')
          }
        } else {
          this.error = `HTTP Error: ${response.status} ${response.statusText}`
        }
      } catch (error) {
        this.error = error.message || '网络错误'
        console.error('获取事项列表出错:', error)
      } finally {
        this.loading = false
      }
    },

    // 获取所有有效材料列表
    async fetchMaterials() {
      try {
        const response = await fetch(`${API_MATERIALS_URL}/search/valid?isValid=true`);
        if (response.ok) {
          this.materialsList = await response.json();
        }
      } catch (error) {
        console.error('获取材料列表出错:', error);
      }
    },

    // 获取所有有效流程图列表
    async fetchProcessDiagrams() {
      try {
        // 获取审批流程图
        const approvalResponse = await fetch('http://localhost:8000/api/process-diagrams/approval/search/valid?isValid=true');
        if (approvalResponse.ok) {
          this.approvalProcessDiagrams = await approvalResponse.json();
        }

        // 获取业务流程图
        const businessResponse = await fetch('http://localhost:8000/api/process-diagrams/business/search/valid?isValid=true');
        if (businessResponse.ok) {
          this.businessProcessDiagrams = await businessResponse.json();
        }
      } catch (error) {
        console.error('获取流程图列表出错:', error);
      }
    },

    // 获取审批层级描述
    getApprovalLevelDescription(approvalLevel) {
      if (!approvalLevel) return ''
      const level = this.approvalLevels.find(l => l.name === approvalLevel)
      return level ? level.description : ''
    },

    // 获取省厅对口指导处室描述
    getProvincialDepartmentOfficeDescription(office) {
      if (!office) return ''
      const dept = this.provincialDepartmentOffices.find(o => o.name === office)
      return dept ? dept.description : ''
    },

    // 格式化显示主项名称（编号 + 名称）
    formatMainItemName(code, name) {
      if (!code && !name) return '-';
      if (!code) return name;
      if (!name) return `${code}`;
      return `${code}.${name}`;
    },

    // 格式化显示子项名称（主项编号.子项编号.子项名称）
    formatSubItemName(mainCode, subCode, name) {
      if (!mainCode && !subCode && !name) return '-';
      if (!mainCode && !subCode) return name || '-';
      if (!name) return `${mainCode || ''}.${subCode || ''}`;
      return `${mainCode || ''}.${subCode || ''}.${name}`;
    },

    // 格式化显示孙项名称（主项编号.子项编号.孙项编号.孙项名称）
    formatGrandchildItemName(mainCode, subCode, grandchildCode, name) {
      if (!mainCode && !subCode && !grandchildCode && !name) return '-';
      if (!mainCode && !subCode && !grandchildCode) return name || '-';
      if (!name) return `${mainCode || ''}.${subCode || ''}.${grandchildCode || ''}`;
      return `${mainCode || ''}.${subCode || ''}.${grandchildCode || ''}.${name}`;
    },

    // 显示材料详情
    editMatter(matter) {
      this.selectedMatter = matter;
    },

    // 返回列表页
    goBackToList() {
      this.selectedMatter = null;
    },

    // 取消新增
    cancelAdd() {
      this.showAddForm = false;
    },

    // 处理事项更新事件
    handleMatterUpdated(updatedMatter) {
      // 更新列表中的事项
      const index = this.matters.findIndex(m => m.id === updatedMatter.id);
      if (index !== -1) {
        this.matters.splice(index, 1, updatedMatter);
      }
      // 更新选中的事项
      this.selectedMatter = updatedMatter;
    },

    // 处理新增事项
    async handleMatterAdded(newMatter) {
      // 添加到事项列表
      this.matters.push(newMatter);
      // 返回列表页
      this.showAddForm = false;
      // 刷新列表
      await this.fetchMatters();
    },

    async toggleMatterStatus(id, isValid) {
      try {
        let response;
        let action = isValid ? '上线' : '下线';

        if (isValid) {
          // 上线事项
          response = await fetch(API_ACTIVATE(id), {
            method: 'PUT'
          });
        } else {
          // 下线事项
          response = await fetch(API_DEACTIVATE(id), {
            method: 'PUT'
          });
        }

        if (response.ok) {
          await this.fetchMatters();
          alert(`事项已${action}`);
        } else if (response.status === 404) {
          alert('事项不存在');
        } else {
          alert(`${action}失败: ${response.status}`);
        }
      } catch (error) {
        console.error(`更新事项状态出错:`, error);
        alert(`${action}失败: ${error.message}`);
      }
    },

    async togglePublishStatus(id, isPublish) {
      try {
        let response;
        let action = isPublish ? '发布' : '取消发布';

        if (isPublish) {
          // 发布事项
          response = await fetch(API_PUBLISH(id), {
            method: 'PUT'
          });
        } else {
          // 取消发布事项
          response = await fetch(API_UNPUBLISH(id), {
            method: 'PUT'
          });
        }

        if (response.ok) {
          await this.fetchMatters();
          alert(`事项已${action}`);
        } else if (response.status === 404) {
          alert('事项不存在');
        } else {
          alert(`${action}失败: ${response.status}`);
        }
      } catch (error) {
        console.error(`更新事项发布状态出错:`, error);
        alert(`${action}失败: ${error.message}`);
      }
    },

    async deleteMatter(id) {
      if (!confirm('确定要删除这个事项吗？')) {
        return
      }

      try {
        const response = await fetch(API_DELETE(id), {
          method: 'DELETE'
        })

        if (response.ok) {
          await this.fetchMatters()
          // 如果正在查看被删除的事项，则返回列表
          if (this.selectedMatter && this.selectedMatter.id === id) {
            this.selectedMatter = null
          }
          alert('事项删除成功')
        } else {
          alert('删除失败: ' + response.status)
        }
      } catch (error) {
        console.error('删除事项出错:', error)
        alert('删除失败: ' + error.message)
      }
    }
  }
}
</script>

<style scoped>
.matter-list-container {
  padding: 20px;
  max-width: 1200px;
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

.header-actions {
  display: flex;
  gap: 10px;
}

.refresh-btn, .add-btn {
  background-color: #409eff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.add-btn {
  background-color: #67c23a;
}

.refresh-btn:hover {
  background-color: #66b1ff;
}

.add-btn:hover {
  background-color: #85ce61;
}

.loading, .error, .no-data {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.error {
  color: #f56c6c;
}

.error button, .no-data button {
  margin-top: 10px;
  background-color: #409eff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.matters-table {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.table-header {
  display: flex;
  background-color: #f5f7fa;
  font-weight: bold;
  border-bottom: 2px solid #dcdfe6;
}

.table-row {
  display: flex;
  border-bottom: 1px solid #dcdfe6;
  cursor: pointer;
  transition: background-color 0.2s;
}

.table-row:hover {
  background-color: #f5f7fa;
}

.table-row:last-child {
  border-bottom: none;
}

.table-cell {
  flex: 1;
  padding: 12px 10px;
  word-break: break-word;
  font-size: 14px;
  color: #606266;
  min-width: 0;
  display: flex;
  align-items: center;
}

.table-cell:first-child {
  flex: 0 0 60px;
}

/* 主项名称可点击样式 */
.matter-name {
  font-weight: 500;
  color: #409eff;
}

.matter-name:hover {
  color: #66b1ff;
  text-decoration: underline;
}

/* 状态标签样式 */
.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.status-active {
  background-color: #f0f9eb;
  color: #67c23a;
  border: 1px solid #c2e7b0;
}

.status-inactive {
  background-color: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fbc4c4;
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.offline-btn, .online-btn, .delete-btn, .publish-btn, .unpublish-btn {
  padding: 4px 8px;
  border-radius: 3px;
  cursor: pointer;
  font-size: 12px;
  border: none;
  white-space: nowrap;
}

.offline-btn {
  background-color: #e6a23c;
  color: white;
}

.offline-btn:hover {
  background-color: #ebb563;
}

.online-btn {
  background-color: #67c23a;
  color: white;
}

.online-btn:hover {
  background-color: #85ce61;
}

.delete-btn {
  background-color: #f56c6c;
  color: white;
}

.delete-btn:hover {
  background-color: #f78989;
}

.publish-btn {
  background-color: #409eff;
  color: white;
}

.publish-btn:hover {
  background-color: #66b1ff;
}

.unpublish-btn {
  background-color: #909399;
  color: white;
}

.unpublish-btn:hover {
  background-color: #a6a9ad;
}

@media (max-width: 768px) {
  .matters-table {
    font-size: 12px;
  }

  .table-cell {
    padding: 8px 5px;
  }

  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .header-actions {
    width: 100%;
    justify-content: space-between;
  }

  .action-buttons {
    flex-direction: column;
    gap: 3px;
  }
}
</style>
