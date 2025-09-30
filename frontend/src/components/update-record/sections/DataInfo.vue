<template>
  <div class="data-info-section">
    <h3>数据变更信息</h3>
    <div class="data-comparison">
      <div class="data-column">
        <h4>变更前数据</h4>
        <div class="form-value">
          <pre v-if="formattedBeforeData" v-html="formattedBeforeData"></pre>
          <span v-else>-</span>
        </div>
      </div>
      
      <div class="data-column">
        <h4>变更后数据</h4>
        <div class="form-value">
          <pre v-if="formattedAfterData" v-html="formattedAfterData"></pre>
          <span v-else>-</span>
        </div>
      </div>
    </div>
    
  </div>
</template>

<script>
export default {
  name: 'DataInfo',
  props: {
    record: {
      type: Object,
      required: true
    }
  },
  computed: {
    
    formattedBeforeData() {
      if (!this.record.beforeData) return null;
      
      try {
        // 如果是字符串，尝试解析为JSON
        if (typeof this.record.beforeData === 'string') {
          const parsed = JSON.parse(this.record.beforeData);
          this.addLinksToData(parsed);
          return JSON.stringify(parsed, null, 2);
        }
        // 如果已经是对象，直接格式化
        this.addLinksToData(this.record.beforeData);
        return JSON.stringify(this.record.beforeData, null, 2);
      } catch (e) {
        // 如果解析失败，返回原始数据
        return this.record.beforeData;
      }
    },
    
    formattedAfterData() {
      if (!this.record.afterData) return null;
      
      try {
        // 如果是字符串，尝试解析为JSON
        if (typeof this.record.afterData === 'string') {
          const parsed = JSON.parse(this.record.afterData);
          this.addLinksToData(parsed);
          return JSON.stringify(parsed, null, 2);
        }
        // 如果已经是对象，直接格式化
        this.addLinksToData(this.record.afterData);
        return JSON.stringify(this.record.afterData, null, 2);
      } catch (e) {
        // 如果解析失败，返回原始数据
        return this.record.afterData;
      }
    }
  },
  methods: {
    addLinksToData(data) {
      if (!data || typeof data !== 'object') return;
      
      // 为材料ID添加跳转链接
      if (data.materialIds && Array.isArray(data.materialIds)) {
        data.materialIds = data.materialIds.map(id => {
          return `<a href="javascript:void(0)" onclick="window.handleNavigateToMaterialDetail(${id})">${id}</a>`;
        });
      }
      
      // 为审批流程图ID添加跳转链接
      if (data.approvalProcessDiagramId) {
        const id = data.approvalProcessDiagramId;
        data.approvalProcessDiagramId = `<a href="javascript:void(0)" onclick="window.handleNavigateToApprovalDiagramDetail(${id})">${id}</a>`;
      }
      
      // 为业务流程图ID添加跳转链接
      if (data.businessProcessDiagramId) {
        const id = data.businessProcessDiagramId;
        data.businessProcessDiagramId = `<a href="javascript:void(0)" onclick="window.handleNavigateToBusinessDiagramDetail(${id})">${id}</a>`;
      }
      
      // 处理同时包含id字段和entityType的情况（这是最常见的）
      if (data.id && this.record.entityType) {
        // 如果entityType是流程图类型，则将id字段转换为可点击的链接
        if (this.record.entityType === 'ApprovalProcessDiagram') {
          data.id = `<a href="javascript:void(0)" onclick="window.handleNavigateToApprovalDiagramDetail(${data.id})">${data.id}</a>`;
        } else if (this.record.entityType === 'BusinessProcessDiagram') {
          data.id = `<a href="javascript:void(0)" onclick="window.handleNavigateToBusinessDiagramDetail(${data.id})">${data.id}</a>`;
        }
      }
    }
  },
  mounted() {
    // 将跳转方法暴露到全局作用域，以便在innerHTML中使用
    window.handleNavigateToMaterialDetail = (materialId) => {
      // 通过自定义事件通知父组件
      this.$emit('navigate-to-material-detail', materialId);
      // 同时更新浏览器URL
      window.location.hash = `#/materials/${materialId}`;
    };
    
    window.handleNavigateToApprovalDiagramDetail = (diagramId) => {
      // 通过自定义事件通知父组件
      this.$emit('navigate-to-approval-diagram-detail', diagramId);
      // 同时更新浏览器URL
      window.location.hash = `#/approval-diagrams/${diagramId}`;
    };
    
    window.handleNavigateToBusinessDiagramDetail = (diagramId) => {
      // 通过自定义事件通知父组件
      this.$emit('navigate-to-business-diagram-detail', diagramId);
      // 同时更新浏览器URL
      window.location.hash = `#/business-diagrams/${diagramId}`;
    };
    
    // 通用流程图跳转处理
    window.handleNavigateToProcessDiagramDetail = (diagramId, entityType) => {
      // 根据entityType决定跳转事件
      if (entityType === 'ApprovalProcessDiagram') {
        this.$emit('navigate-to-approval-diagram-detail', diagramId);
        window.location.hash = `#/approval-diagrams/${diagramId}`;
      } else if (entityType === 'BusinessProcessDiagram') {
        this.$emit('navigate-to-business-diagram-detail', diagramId);
        window.location.hash = `#/business-diagrams/${diagramId}`;
      } else {
        // 对于其他类型，使用通用路由
        window.location.hash = `#/process-diagrams/${diagramId}`;
      }
    };
  },
  beforeDestroy() {
    // 清理全局函数引用
    delete window.handleNavigateToMaterialDetail;
    delete window.handleNavigateToApprovalDiagramDetail;
    delete window.handleNavigateToBusinessDiagramDetail;
    delete window.handleNavigateToProcessDiagramDetail;
  }
}
</script>

<style scoped>
.data-info-section {
  margin-bottom: 20px;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #fafafa;
}

.data-info-section h3 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #303133;
  border-left: 4px solid #007bff;
  padding-left: 10px;
}

.data-comparison {
  display: flex;
  gap: 20px;
}

.data-column {
  flex: 1;
}

.data-column h4 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #606266;
  font-size: 14px;
}

.form-value {
  flex: 1;
  color: #303133;
}

.form-value pre {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
  margin: 0;
  color: #606266;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.test-data-section {
  margin-top: 20px;
  padding: 10px;
  background-color: #e8f4f8;
  border-left: 4px solid #007bff;
  border-radius: 4px;
}

@media (max-width: 768px) {
  .data-comparison {
    flex-direction: column;
  }
}
</style>