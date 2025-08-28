<!-- src/components/material/MaterialDetail.vue -->
<template>
  <div class="material-detail-container">
    <div class="header">
      <h2>编辑材料</h2>
      <!-- 移除了返回列表按钮 -->
    </div>

    <div class="material-detail-content">
      <form @submit.prevent="handleSubmit">
        <!-- 将ID和材料明细放在同一行 -->
        <div class="form-row">
          <div class="form-group">
            <label>ID:</label>
            <input type="text" v-model="form.id" disabled>
          </div>

          <div class="form-group">
            <label>材料明细 *</label>
            <input type="text" v-model="form.materialDetail" required>
          </div>
        </div>

        <div class="form-group">
          <label>审核点:</label>
          <textarea v-model="form.reviewPoint"></textarea>
        </div>

        <div class="form-group">
          <label>自动审批标准:</label>
          <textarea v-model="form.autoApprovalCriteria"></textarea>
        </div>

        <!-- 将是否共享和材料来源放在同一行，各占一半 -->
        <div class="form-row">
          <div class="form-group half-width">
            <label>是否共享:</label>
            <select v-model="form.isShared">
              <option :value="true">是</option>
              <option :value="false">否</option>
            </select>
          </div>

          <div class="form-group half-width">
            <label>材料来源:</label>
            <select v-model="form.materialSource">
              <option value="PERSONAL_SUBMISSION">个人提交网上办理</option>
              <option value="SYSTEM_AUTO_SHARED">系统自动获取</option>
            </select>
          </div>
        </div>

        <!-- 将处理方式和承诺资格放在同一行，各占一半 -->
        <div class="form-row">
          <div class="form-group half-width">
            <label>处理方式:</label>
            <input type="text" v-model="form.processingMethodAndInfoAccess">
          </div>

          <div class="form-group half-width">
            <label>承诺资格:</label>
            <select v-model="form.isEligibleForPromise">
              <option :value="true">是</option>
              <option :value="false">否</option>
            </select>
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
          <button type="button" @click="goBack" class="back-btn-form">返回</button>
          <button type="submit" class="save-btn">保存</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
const API_BASE_URL = 'http://localhost:8000/api/materials';
const API_UPDATE = (id) => `${API_BASE_URL}/${id}`;

export default {
  name: 'MaterialDetail',
  props: {
    material: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      form: {
        id: null,
        materialDetail: '',
        reviewPoint: '',
        autoApprovalCriteria: '',
        isShared: false,
        materialSource: 'PERSONAL_SUBMISSION',
        processingMethodAndInfoAccess: '',
        isEligibleForPromise: false,
        isValid: true
      }
    };
  },
  created() {
    // 初始化表单数据
    this.resetForm();
  },
  methods: {
    resetForm() {
      this.form = {
        id: this.material.id,
        materialDetail: this.material.materialDetail,
        reviewPoint: this.material.reviewPoint,
        autoApprovalCriteria: this.material.autoApprovalCriteria,
        isShared: this.material.isShared,
        materialSource: this.material.materialSource,
        processingMethodAndInfoAccess: this.material.processingMethodAndInfoAccess,
        isEligibleForPromise: this.material.isEligibleForPromise,
        isValid: this.material.isValid
      };
    },

    // 返回列表
    goBack() {
      console.log('MaterialDetail: 返回按钮被点击');
      this.$emit('back');
    },

    // 表单提交处理
    async handleSubmit() {
      console.log('MaterialDetail: 保存按钮被点击，表单数据:', this.form);
      try {
        const response = await fetch(API_UPDATE(this.form.id), {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(this.form)
        });

        console.log('MaterialDetail: 收到响应:', response.status);
        if (response.ok) {
          const updatedMaterial = await response.json();
          console.log('MaterialDetail: 更新成功，返回数据:', updatedMaterial);
          this.$emit('material-updated', updatedMaterial);
          alert('材料更新成功');
        } else {
          const errorText = await response.text();
          console.error('MaterialDetail: 更新失败:', response.status, errorText);
          alert('更新失败: ' + response.status + ' - ' + errorText);
        }
      } catch (error) {
        console.error('MaterialDetail: 保存材料出错:', error);
        alert('保存失败: ' + error.message);
      }
    }
  }
};
</script>

<style scoped>
.material-detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1001;
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

.material-detail-content {
  background-color: white;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1001;
}

/* 表单样式 */
.form-group {
  margin-bottom: 15px;
}

.form-row {
  display: flex;
  gap: 15px;
}

.form-row .form-group {
  flex: 1;
  margin-bottom: 15px;
}

/* 调整ID字段的宽度 */
.form-row .form-group:first-child {
  flex: 0 0 120px; /* ID字段更窄 */
}

/* 半宽字段样式 */
.form-row .form-group.half-width {
  flex: 1;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #303133;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group textarea {
  min-height: 60px;
  resize: vertical;
}

.form-actions {
  margin-top: 20px;
  text-align: right;
  position: relative;
  z-index: 1002;
}

.form-actions button {
  margin-left: 10px;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  position: relative;
  z-index: 1003;
}

.form-actions button[type="button"] {
  background-color: #909399;
  color: white;
  border: none;
}

.form-actions button[type="button"]:hover {
  background-color: #a6a9ad;
}

.back-btn-form {
  background-color: #909399 !important;
  color: white !important;
  border: none !important;
}

.back-btn-form:hover {
  background-color: #a6a9ad !important;
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
  .material-detail-container {
    padding: 10px;
  }

  .form-row {
    flex-direction: column;
    gap: 0;
  }

  /* 在移动端恢复ID字段的默认宽度 */
  .form-row .form-group:first-child {
    flex: 1;
  }

  .form-actions {
    display: flex;
    justify-content: space-between;
  }

  .form-actions button {
    flex: 1;
    margin: 0 5px;
  }
}
</style>
