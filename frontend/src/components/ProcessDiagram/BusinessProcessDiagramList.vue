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

      <div class="no-data" v-else-if="diagrams && diagrams.length === 0">
        <div class="no-data-content">
          <h3>暂无业务流程图数据</h3>
          <p>点击下方按钮添加您的第一个业务流程图</p>
          <button class="add-btn" @click="showAddForm">新增业务流程图</button>
        </div>
      </div>

      <div class="diagrams-table" v-else-if="diagrams && diagrams.length > 0">
        <div class="table-header">
          <div class="table-cell">ID</div>
          <div class="table-cell">名称</div>
          <div class="table-cell">图片类型</div>
          <div class="table-cell">预览</div>
          <div class="table-cell">状态</div>
          <div class="table-cell">操作</div>
        </div>

        <div
          class="table-row"
          v-for="diagram in diagrams"
          :key="diagram.id"
          @click="viewDiagramDetail(diagram)"
        >
          <div class="table-cell">{{ diagram.id }}</div>
          <div class="table-cell diagram-name">
            {{ diagram.imageName || '未命名' }}
          </div>
          <div class="table-cell">
            {{ diagram.imageType || '未知' }}
          </div>
          <div class="table-cell">
            <img
              v-if="diagram.imageDataUrl"
              :src="diagram.imageDataUrl"
              :alt="diagram.imageName"
              class="preview-image"
            />
            <span v-else>无预览</span>
          </div>
          <div class="table-cell">
            <span :class="['status-badge', diagram.isValid ? 'status-active' : 'status-inactive']">
              {{ diagram.isValid ? '已上线' : '已下线' }}
            </span>
          </div>
          <div class="table-cell">
            <div class="action-buttons">
              <button
                v-if="diagram.isValid"
                class="offline-btn"
                @click.stop="toggleDiagramStatus(diagram.id, false)"
              >
                下线
              </button>
              <template v-else>
                <button
                  class="online-btn"
                  @click.stop="toggleDiagramStatus(diagram.id, true)"
                >
                  上线
                </button>
                <button
                  class="delete-btn"
                  @click.stop="deleteDiagram(diagram.id)"
                >
                  删除
                </button>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 流程图详情界面 -->
    <BusinessProcessDiagramDetail
      v-else
      :diagram="selectedDiagram"
      @back="goBackToList"
      @diagram-updated="handleDiagramUpdated"
    />

    <!-- 新增流程图弹窗 -->
    <div class="modal" v-if="showDiagramForm" @click="closeForm">
      <div class="modal-content form-modal" @click.stop>
        <span class="close" @click="closeForm">&times;</span>
        <h3>新增业务流程图</h3>
        <form @submit.prevent="saveDiagram">
          <div class="form-group">
            <label>名称 *</label>
            <input type="text" v-model="form.imageName" required>
          </div>

          <div class="form-group">
            <label>上传图像</label>
            <input
              type="file"
              accept="image/*"
              @change="onImageChange"
            />
            <div v-if="form.imagePreview" class="image-preview">
              <img :src="form.imagePreview" alt="预览图像" />
            </div>
          </div>

          <div class="form-group">
            <label>状态:</label>
            <select v-model="form.isValid">
              <option :value="true">已上线</option>
              <option :value="false">已下线</option>
            </select>
          </div>

          <div class="form-actions">
            <button
              type="button"
              @click="closeForm"
            >
              取消
            </button>
            <button
              type="submit"
              class="save-btn"
            >
              创建
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
// 引入详情组件
import BusinessProcessDiagramDetail from './BusinessProcessDiagramDetail.vue';

// API端点常量
const API_BASE_URL = 'http://localhost:8000/api/process-diagrams/business';
const API_GET_ALL = `${API_BASE_URL}`;
const API_CREATE = `${API_BASE_URL}`;
const API_GET_BY_ID = (id) => `${API_BASE_URL}/${id}`;
const API_UPDATE = (id) => `${API_BASE_URL}/${id}`;
const API_DELETE = (id) => `${API_BASE_URL}/${id}`;
const API_ACTIVATE = (id) => `${API_BASE_URL}/${id}/activate`;
const API_DEACTIVATE = (id) => `${API_BASE_URL}/${id}/deactivate`;

export default {
  name: 'BusinessProcessDiagramList',
  components: {
    BusinessProcessDiagramDetail
  },
  data() {
    return {
      diagrams: [],
      loading: true,
      error: null,
      selectedDiagram: null,
      showDiagramForm: false,
      form: {
        imageName: '',
        imageFile: null,
        imagePreview: null,
        isValid: true
      }
    };
  },
  async mounted() {
    await this.fetchDiagrams();
  },
  methods: {
    async fetchDiagrams() {
      this.loading = true;
      this.error = null;
      this.selectedDiagram = null;

      try {
        const response = await fetch(API_GET_ALL, {
          headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
          }
        });

        if (response.ok) {
          const contentType = response.headers.get('content-type');
          if (contentType && contentType.includes('application/json')) {
            const data = await response.json();
            this.diagrams = Array.isArray(data) ? data : [];

            // 为每个流程图添加图像数据URL
            this.diagrams.forEach(diagram => {
              // 使用后端提供的getImageDataUrl方法
              if (diagram.imageDataUrl) {
                diagram.imageDataUrl = diagram.imageDataUrl;
              } else if (diagram.imageData && diagram.imageType) {
                // 根据imageType生成Base64 URL
                const contentType = this.getContentTypeByImageType(diagram.imageType);
                diagram.imageDataUrl = `data:${contentType};base64,${diagram.imageData}`;
              } else if (diagram.imageData && diagram.contentType) {
                // 后备方案：使用contentType字段（兼容旧数据）
                diagram.imageDataUrl = `data:${diagram.contentType};base64,${diagram.imageData}`;
              }

              // 确保有状态字段
              if (diagram.isValid === undefined) {
                diagram.isValid = true;
              }

              // 确保imageName存在
              if (diagram.imageName === undefined || diagram.imageName === null) {
                diagram.imageName = '';
              }
            });
          } else {
            throw new Error('服务器返回的不是JSON格式数据');
          }
        } else {
          this.error = `HTTP Error: ${response.status} ${response.statusText}`;
        }
      } catch (error) {
        this.error = error.message || '网络错误';
        console.error('获取业务流程图列表出错:', error);
      } finally {
        this.loading = false;
      }
    },

    viewDiagramDetail(diagram) {
      this.selectedDiagram = diagram;
    },

    goBackToList() {
      this.selectedDiagram = null;
    },

    handleDiagramUpdated(updatedDiagram) {
      // 更新列表中的流程图
      const index = this.diagrams.findIndex(d => d.id === updatedDiagram.id);
      if (index !== -1) {
        this.diagrams.splice(index, 1, updatedDiagram);
      }
      // 更新选中的流程图
      this.selectedDiagram = updatedDiagram;
    },

    showAddForm() {
      this.resetForm();
      this.showDiagramForm = true;
    },

    resetForm() {
      this.form = {
        imageName: '',
        imageFile: null,
        imagePreview: null,
        isValid: true
      };
    },

    closeForm() {
      this.showDiagramForm = false;
    },

    onImageChange(event) {
      const file = event.target.files[0];
      if (file) {
        this.form.imageFile = file;
        // 生成预览
        const reader = new FileReader();
        reader.onload = (e) => {
          this.form.imagePreview = e.target.result;
        };
        reader.readAsDataURL(file);
      }
    },

    async saveDiagram() {
      try {
        const formData = new FormData();
        formData.append('imageName', this.form.imageName);
        formData.append('isValid', this.form.isValid);

        // 如果是新增或者编辑时重新上传了图片
        if (this.form.imageFile) {
          formData.append('imageFile', this.form.imageFile);
        }

        // 新增流程图
        const response = await fetch(API_CREATE, {
          method: 'POST',
          body: formData
        });

        if (response.ok) {
          await this.fetchDiagrams();
          this.closeForm();
          alert('业务流程图创建成功');
        } else {
          const errorText = await response.text();
          alert('创建失败: ' + response.status + ' - ' + errorText);
        }
      } catch (error) {
        console.error('保存业务流程图出错:', error);
        alert('保存失败: ' + error.message);
      }
    },

    async toggleDiagramStatus(id, isValid) {
      try {
        let response;
        let action = isValid ? '启用' : '禁用';

        if (isValid) {
          // 启用流程图
          response = await fetch(API_ACTIVATE(id), {
            method: 'PUT'
          });
        } else {
          // 禁用流程图
          response = await fetch(API_DEACTIVATE(id), {
            method: 'PUT'
          });
        }

        if (response.ok) {
          await this.fetchDiagrams();
          alert(`流程图已${action}`);
        } else if (response.status === 404) {
          alert('流程图不存在');
        } else {
          alert(`${action}失败: ${response.status}`);
        }
      } catch (error) {
        console.error(`更新流程图状态出错:`, error);
        alert(`${action}失败: ${error.message}`);
      }
    },

    async deleteDiagram(id) {
      if (!confirm('确定要删除这个业务流程图吗？')) {
        return;
      }

      try {
        const response = await fetch(API_DELETE(id), {
          method: 'DELETE'
        });

        if (response.ok) {
          await this.fetchDiagrams();
          // 如果正在查看被删除的流程图，则返回列表
          if (this.selectedDiagram && this.selectedDiagram.id === id) {
            this.selectedDiagram = null;
          }
          alert('业务流程图删除成功');
        } else {
          alert('删除失败: ' + response.status);
        }
      } catch (error) {
        console.error('删除业务流程图出错:', error);
        alert('删除失败: ' + error.message);
      }
    },

    /**
     * 根据imageType获取对应的contentType
     * @param {string} imageType - 图像类型，如'JPEG', 'PNG'等
     * @returns {string} - 对应的MIME类型
     */
    getContentTypeByImageType(imageType) {
      const contentTypes = {
        'JPEG': 'image/jpeg',
        'PNG': 'image/png',
        'GIF': 'image/gif',
        'BMP': 'image/bmp',
        'SVG': 'image/svg+xml'
      };
      return contentTypes[imageType] || 'image/jpeg';
    },

    resetToListView() {
      this.selectedDiagram = null;
      this.showDiagramForm = false;
    }
  }
};
</script>

<style scoped>
/* 保留原有样式，只需修改部分选择器 */
.process-diagram-container {
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

.refresh-btn,
.add-btn {
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

.loading,
.error,
.no-data {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.error {
  color: #f56c6c;
}

.error button,
.no-data button {
  margin-top: 10px;
  background-color: #409eff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.no-data-content {
  text-align: center;
}

.no-data-content h3 {
  color: #303133;
  margin-bottom: 10px;
}

.no-data-content p {
  color: #909399;
  margin-bottom: 20px;
}

.diagrams-table {
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

.table-cell:nth-child(2) {
  flex: 2;
  color: #409eff;
  font-weight: 500;
}

.preview-image {
  max-width: 100px;
  max-height: 50px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
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

.offline-btn, .online-btn, .delete-btn {
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

.form-modal {
  max-width: 600px;
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

/* 表单样式 */
.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #303133;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.image-preview {
  margin-top: 10px;
}

.image-preview img {
  max-width: 100%;
  max-height: 200px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.form-actions {
  margin-top: 20px;
  text-align: right;
}

.form-actions button {
  margin-left: 10px;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.form-actions button[type="button"] {
  background-color: #909399;
  color: white;
  border: none;
}

.form-actions button[type="button"]:hover {
  background-color: #a6a9ad;
}

.save-btn {
  background-color: #67c23a;
  color: white;
  border: none;
}

.save-btn:hover {
  background-color: #85ce61;
}

@media (max-width: 768px) {
  .diagrams-table {
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

  .preview-image {
    max-width: 60px;
    max-height: 30px;
  }
}
</style>
