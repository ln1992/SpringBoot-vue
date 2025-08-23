<!-- src/components/processdiagram/BusinessProcessDiagramList.vue -->
<template>
  <div class="process-diagram-container">
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

    <div class="no-data" v-else-if="diagrams.length === 0">
      <p>暂无业务流程图数据</p>
      <button class="add-btn" @click="showAddForm">新增第一个业务流程图</button>
    </div>

    <div class="diagrams-table" v-else>
      <div class="table-header">
        <div class="table-cell">ID</div>
        <div class="table-cell">名称</div>
        <div class="table-cell">预览</div>
        <div class="table-cell">操作</div>
      </div>

      <div
        class="table-row"
        v-for="diagram in diagrams"
        :key="diagram.id"
      >
        <div class="table-cell">{{ diagram.id }}</div>
        <div class="table-cell">{{ diagram.imageName || '未命名' }}</div>
        <div class="table-cell">
          <img
            v-if="diagram.imageDataUrl"
            :src="diagram.imageDataUrl"
            :alt="diagram.imageName"
            class="preview-image"
            @click="viewDiagramDetail(diagram)"
          />
          <span v-else>无预览</span>
        </div>
        <div class="table-cell">
          <div class="action-buttons">
            <button class="edit-btn" @click="editDiagram(diagram)">编辑</button>
            <button class="delete-btn" @click="deleteDiagram(diagram.id)">删除</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 流程图详情弹窗 -->
    <div class="modal" v-if="selectedDiagram" @click="closeDetailModal">
      <div class="modal-content detail-modal" @click.stop>
        <span class="close" @click="closeDetailModal">&times;</span>
        <h3>业务流程图详情</h3>
        <div class="diagram-detail">
          <p><strong>ID:</strong> {{ selectedDiagram.id }}</p>
          <p><strong>名称:</strong> {{ selectedDiagram.imageName || '未命名' }}</p>
          <div class="image-preview-container">
            <img
              v-if="selectedDiagram.imageDataUrl"
              :src="selectedDiagram.imageDataUrl"
              :alt="selectedDiagram.imageName"
              class="detail-image"
            />
            <p v-else>无图像数据</p>
          </div>
        </div>
        <div class="modal-actions">
          <button @click="editDiagram(selectedDiagram)">编辑</button>
          <button @click="closeDetailModal">关闭</button>
        </div>
      </div>
    </div>

    <!-- 新增/编辑流程图弹窗 -->
    <div class="modal" v-if="showDiagramForm" @click="closeForm">
      <div class="modal-content form-modal" @click.stop>
        <span class="close" @click="closeForm">&times;</span>
        <h3>{{ editingDiagram ? '编辑业务流程图' : '新增业务流程图' }}</h3>
        <form @submit.prevent="saveDiagram">
          <div class="form-group" v-if="editingDiagram">
            <label>ID:</label>
            <input type="text" v-model="form.id" disabled>
          </div>

          <div class="form-group">
            <label>名称 *</label>
            <input type="text" v-model="form.imageName" required>
          </div>

          <div class="form-group">
            <label>上传图像 *</label>
            <input
              type="file"
              accept="image/*"
              @change="onImageChange"
              :disabled="!!editingDiagram && !!editingDiagram.imageDataUrl"
            />
            <div v-if="form.imagePreview" class="image-preview">
              <img :src="form.imagePreview" alt="预览图像" />
            </div>
            <div v-else-if="editingDiagram && editingDiagram.imageDataUrl" class="image-preview">
              <img :src="editingDiagram.imageDataUrl" alt="当前图像" />
              <p>当前图像 (编辑时不可更改)</p>
            </div>
          </div>

          <div class="form-actions">
            <button type="button" @click="closeForm">取消</button>
            <button type="submit" class="save-btn">
              {{ editingDiagram ? '更新' : '创建' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
const API_BASE_URL = 'http://localhost:8000/api/process-diagrams/business'

export default {
  name: 'BusinessProcessDiagramList',
  data() {
    return {
      diagrams: [],
      loading: true,
      error: null,
      selectedDiagram: null,
      showDiagramForm: false,
      editingDiagram: null,
      form: {
        id: null,
        imageName: '',
        imageFile: null,
        imagePreview: null
      }
    }
  },
  async mounted() {
    await this.fetchDiagrams()
  },
  methods: {
    async fetchDiagrams() {
      this.loading = true
      this.error = null

      try {
        const response = await fetch(API_BASE_URL)
        if (response.ok) {
          this.diagrams = await response.json()
          // 为每个流程图添加图像数据URL
          this.diagrams.forEach(diagram => {
            if (diagram.imageData) {
              diagram.imageDataUrl = `data:${diagram.contentType};base64,${diagram.imageData}`
            }
          })
        } else {
          throw new Error(`获取业务流程图失败: ${response.status}`)
        }
      } catch (error) {
        this.error = error.message || '网络错误'
        console.error('获取业务流程图列表出错:', error)
      } finally {
        this.loading = false
      }
    },

    viewDiagramDetail(diagram) {
      this.selectedDiagram = diagram
    },

    closeDetailModal() {
      this.selectedDiagram = null
    },

    showAddForm() {
      this.editingDiagram = null
      this.resetForm()
      this.showDiagramForm = true
    },

    resetForm() {
      this.form = {
        id: null,
        imageName: '',
        imageFile: null,
        imagePreview: null
      }
    },

    closeForm() {
      this.showDiagramForm = false
      this.editingDiagram = null
    },

    onImageChange(event) {
      const file = event.target.files[0]
      if (file) {
        this.form.imageFile = file
        // 生成预览
        const reader = new FileReader()
        reader.onload = (e) => {
          this.form.imagePreview = e.target.result
        }
        reader.readAsDataURL(file)
      }
    },

    editDiagram(diagram) {
      this.editingDiagram = diagram
      this.form = {
        id: diagram.id,
        imageName: diagram.imageName || '',
        imageFile: null,
        imagePreview: null
      }
      this.showDiagramForm = true
    },

    async saveDiagram() {
      try {
        const formData = new FormData()
        formData.append('imageName', this.form.imageName)

        // 如果是新增或者编辑时重新上传了图片
        if (this.form.imageFile) {
          formData.append('imageFile', this.form.imageFile)
        }

        let response

        if (this.editingDiagram) {
          // 更新流程图
          response = await fetch(`${API_BASE_URL}/${this.form.id}`, {
            method: 'PUT',
            body: formData
          })
        } else {
          // 新增流程图
          response = await fetch(API_BASE_URL, {
            method: 'POST',
            body: formData
          })
        }

        if (response.ok) {
          await this.fetchDiagrams()
          this.closeForm()
          alert(this.editingDiagram ? '业务流程图更新成功' : '业务流程图创建成功')
        } else {
          const errorText = await response.text()
          console.error('Server error response:', errorText)
          alert((this.editingDiagram ? '更新' : '创建') + '失败: ' + response.status + ' - ' + errorText)
        }
      } catch (error) {
        console.error('保存业务流程图出错:', error)
        alert('保存失败: ' + error.message)
      }
    },

    async deleteDiagram(id) {
      if (!confirm('确定要删除这个业务流程图吗？')) {
        return
      }

      try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
          method: 'DELETE'
        })

        if (response.ok) {
          await this.fetchDiagrams()
          alert('业务流程图删除成功')
        } else {
          alert('删除失败: ' + response.status)
        }
      } catch (error) {
        console.error('删除业务流程图出错:', error)
        alert('删除失败: ' + error.message)
      }
    }
  }
}
</script>

<style scoped>
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

.preview-image {
  max-width: 100px;
  max-height: 50px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
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

.detail-modal {
  max-width: 600px;
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

.diagram-detail p {
  margin: 10px 0;
  line-height: 1.5;
}

.image-preview-container {
  text-align: center;
  margin: 15px 0;
}

.detail-image {
  max-width: 100%;
  max-height: 300px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.modal-actions {
  margin-top: 20px;
  text-align: right;
}

.modal-actions button {
  margin-left: 10px;
  background-color: #409eff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.modal-actions button:hover {
  background-color: #66b1ff;
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

.form-group input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group input:disabled {
  background-color: #f5f7fa;
  cursor: not-allowed;
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

.image-preview p {
  margin: 5px 0;
  font-size: 12px;
  color: #909399;
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

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.edit-btn,
.delete-btn {
  padding: 4px 8px;
  border-radius: 3px;
  cursor: pointer;
  font-size: 12px;
  border: none;
  white-space: nowrap;
}

.edit-btn {
  background-color: #409eff;
  color: white;
}

.edit-btn:hover {
  background-color: #66b1ff;
}

.delete-btn {
  background-color: #f56c6c;
  color: white;
}

.delete-btn:hover {
  background-color: #f78989;
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
