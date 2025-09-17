<!-- src/components/AdminPanel.vue -->
<template>
  <div class="admin-panel-container">
    <h2>管理员面板</h2>
    
    <!-- 导出功能区 -->
    <div class="export-section">
      <h3>事项导出</h3>
      <div class="export-form">
        <div class="form-group">
          <label for="version">版本:</label>
          <div class="controls-row">
            <select
              id="version"
              v-model="exportVersion"
              :disabled="loadingVersions"
              class="version-select"
            >
              <option value="">请选择版本</option>
              <option
                v-for="version in versions"
                :key="version"
                :value="version"
              >
                {{ version }}
              </option>
            </select>
            <button
              class="refresh-btn"
              @click="loadVersions"
              :disabled="loadingVersions"
            >
              {{ loadingVersions ? '加载中...' : '刷新' }}
            </button>
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
      </div>
    </div>
  </div>
</template>

<script>
import { matterToolsService, matterService } from '../api';

export default {
  name: 'AdminPanel',
  data() {
    return {
      exportVersion: '', // 导出版本号
      versions: [], // 版本列表
      loadingVersions: false // 版本加载状态
    };
  },
  async mounted() {
    await this.loadVersions();
  },
  methods: {
    // 加载版本列表
    async loadVersions() {
      this.loadingVersions = true;
      try {
        const versions = await matterService.getAllVersions();
        this.versions = versions.sort((a, b) => b - a); // 按版本号降序排列
      } catch (error) {
        console.error('加载版本列表失败:', error);
        alert('加载版本列表失败: ' + (error.message || '未知错误'));
      } finally {
        this.loadingVersions = false;
      }
    },

    // 导出目录
    async exportCatalog() {
      if (!this.exportVersion) {
        alert('请选择版本号');
        return;
      }

      try {
        const blob = await matterToolsService.exportMattersCatalog(this.exportVersion);
        this.downloadBlob(blob, `事项目录_v${this.exportVersion}.docx`);
      } catch (error) {
        console.error('导出目录失败:', error);
        alert('导出目录失败: ' + (error.message || '未知错误'));
      }
    },

    // 导出文档
    async exportDocument() {
      if (!this.exportVersion) {
        alert('请选择版本号');
        return;
      }

      try {
        const blob = await matterToolsService.exportMattersDocuments(this.exportVersion);
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
.admin-panel-container {
  padding: 20px;
  position: relative;
}

.admin-panel-container h2 {
  margin: 0 0 20px 0;
  color: #333;
}

.export-section {
  background-color: #f8f9fa;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 20px;
}

.export-section h3 {
  margin-top: 0;
  color: #333;
}

.export-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.form-group label {
  font-weight: bold;
  min-width: 50px;
}

.controls-row {
  display: flex;
  gap: 8px;
  flex: 1;
  align-items: center;
}

.version-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  width: 150px; /* 固定宽度 */
}

.refresh-btn,
.export-catalog-btn,
.export-document-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
  white-space: nowrap;
}

.refresh-btn {
  border: 1px solid #ddd;
  background-color: #f5f7fa;
  color: #333;
}

.refresh-btn:hover:not(:disabled) {
  background-color: #e1e5f0;
}

.refresh-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
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

@media (max-width: 768px) {
  .export-form {
    align-items: flex-start;
  }
  
  .form-group {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .controls-row {
    flex-direction: column;
    align-items: stretch;
    width: 100%;
  }
  
  .version-select {
    width: 100%;
  }
}
</style>