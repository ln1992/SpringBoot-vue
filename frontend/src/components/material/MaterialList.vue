<!-- src/components/material/MaterialList.vue -->
<template>
  <div class="material-list-container">
    <div class="header">
      <h2>材料清单</h2>
      <button class="refresh-btn" @click="fetchMaterials">刷新</button>
    </div>

    <div class="loading" v-if="loading">
      <p>正在加载材料数据...</p>
    </div>

    <div class="error" v-else-if="error">
      <p>加载失败: {{ error }}</p>
      <button @click="fetchMaterials">重试</button>
    </div>

    <div class="no-data" v-else-if="materials.length === 0">
      <p>暂无材料数据</p>
    </div>

    <div class="materials-table" v-else>
      <div class="table-header">
        <div class="table-cell">ID</div>
        <div class="table-cell">材料明细</div>
        <div class="table-cell">审核点</div>
        <div class="table-cell">自动审批标准</div>
        <div class="table-cell">共享</div>
        <div class="table-cell">材料来源</div>
        <div class="table-cell">处理方式</div>
        <div class="table-cell">承诺资格</div>
      </div>

      <div
        class="table-row"
        v-for="material in materials"
        :key="material.id"
        @click="viewMaterialDetail(material)"
      >
        <div class="table-cell">{{ material.id }}</div>
        <div class="table-cell">{{ material.materialDetails || '-' }}</div>
        <div class="table-cell">{{ material.reviewPoints || '-' }}</div>
        <div class="table-cell">{{ material.autoApprovalCriteria || '-' }}</div>
        <div class="table-cell">{{ material.isShared ? '是' : '否' }}</div>
        <div class="table-cell">{{ material.materialSource || '-' }}</div>
        <div class="table-cell">{{ material.processingMethodAndInfoAccess || '-' }}</div>
        <div class="table-cell">{{ material.isEligibleForPromise ? '是' : '否' }}</div>
      </div>
    </div>

    <!-- 材料详情弹窗 -->
    <div class="modal" v-if="selectedMaterial" @click="closeModal">
      <div class="modal-content" @click.stop>
        <span class="close" @click="closeModal">&times;</span>
        <h3>材料详情</h3>
        <div class="material-detail">
          <p><strong>ID:</strong> {{ selectedMaterial.id }}</p>
          <p><strong>材料明细:</strong> {{ selectedMaterial.materialDetails || '无' }}</p>
          <p><strong>审核点:</strong> {{ selectedMaterial.reviewPoints || '无' }}</p>
          <p><strong>自动审批标准:</strong> {{ selectedMaterial.autoApprovalCriteria || '无' }}</p>
          <p><strong>是否共享:</strong> {{ selectedMaterial.isShared ? '是' : '否' }}</p>
          <p><strong>材料来源:</strong> {{ selectedMaterial.materialSource || '未指定' }}</p>
          <p><strong>处理方式:</strong> {{ selectedMaterial.processingMethodAndInfoAccess || '未指定' }}</p>
          <p><strong>承诺资格:</strong> {{ selectedMaterial.isEligibleForPromise ? '是' : '否' }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MaterialList',
  data() {
    return {
      materials: [],
      loading: true,
      error: null,
      selectedMaterial: null
    };
  },
  async mounted() {
    await this.fetchMaterials();
  },
  methods: {
    async fetchMaterials() {
      this.loading = true;
      this.error = null;

      try {
        // 使用完整的API路径
        const response = await fetch('http://localhost:8000/api/materials');
        // 或者如果是相对路径，确保正确
        // const response = await fetch('/api/materials');

        if (response.ok) {
          const contentType = response.headers.get('content-type');
          if (contentType && contentType.includes('application/json')) {
            this.materials = await response.json();
          } else {
            throw new Error('服务器返回的不是JSON格式数据');
          }
        } else {
          this.error = `HTTP Error: ${response.status} ${response.statusText}`;
        }
      } catch (error) {
        this.error = error.message || '网络错误';
        console.error('获取材料列表出错:', error);
      } finally {
        this.loading = false;
      }
    }
    ,

    viewMaterialDetail(material) {
      this.selectedMaterial = material;
    },

    closeModal() {
      this.selectedMaterial = null;
    }
  }
};
</script>

<style scoped>
.material-list-container {
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

.refresh-btn {
  background-color: #409eff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.refresh-btn:hover {
  background-color: #66b1ff;
}

.loading, .error, .no-data {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.error {
  color: #f56c6c;
}

.error button {
  margin-top: 10px;
  background-color: #f56c6c;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.materials-table {
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
  transition: background-color 0.2s;
  cursor: pointer;
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
}

.table-cell:first-child {
  flex: 0 0 60px;
}

.table-cell:nth-child(2) {
  flex: 2;
}

.table-cell:nth-child(3),
.table-cell:nth-child(4) {
  flex: 1.5;
}

/* 弹窗样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  padding: 20px;
  border-radius: 4px;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
  position: relative;
}

.close {
  position: absolute;
  top: 10px;
  right: 15px;
  font-size: 24px;
  cursor: pointer;
  color: #909399;
}

.close:hover {
  color: #303133;
}

.material-detail p {
  margin: 10px 0;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .materials-table {
    font-size: 12px;
  }

  .table-cell {
    padding: 8px 5px;
  }

  .header h2 {
    font-size: 18px;
  }
}
</style>
