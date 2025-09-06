<!-- src/components/material/MaterialDetail.vue -->
<template>
  <div class="material-detail-container">
    <div class="header">
      <h2>{{ isEditMode ? '编辑材料' : '新增材料' }}</h2>
    </div>

    <div class="material-detail-content">
      <form @submit.prevent="handleSubmit">
        <!-- ID和材料明细放在同一行 -->
        <div class="form-row" v-if="isEditMode">
          <div class="form-group">
            <label>ID:</label>
            <input type="text" v-model="form.id" disabled>
          </div>

          <div class="form-group">
            <label>材料明细 *</label>
            <input
              type="text"
              v-model="form.materialDetail"
              required
              :class="{ 'error': errors.materialDetail }"
            >
            <div class="error-message" v-if="errors.materialDetail">
              {{ errors.materialDetail }}
            </div>
          </div>
        </div>

        <!-- 新增模式下材料明细单独一行 -->
        <div class="form-group" v-else>
          <label>材料明细 *</label>
          <input
            type="text"
            v-model="form.materialDetail"
            required
            :class="{ 'error': errors.materialDetail }"
          >
          <div class="error-message" v-if="errors.materialDetail">
            {{ errors.materialDetail }}
          </div>
        </div>

        <!-- 版本和显示名称放在同一行 -->
        <div class="form-row">
          <div class="form-group">
            <label>版本:</label>
            <input
              type="number"
              v-model.number="form.version"
              min="1"
            >
          </div>

          <div class="form-group">
            <label>显示名称:</label>
            <input type="text" v-model="form.__name__" disabled>
          </div>
        </div>

        <!-- 审核要点 -->
        <div class="form-group">
          <label>审核要点:</label>
          <textarea v-model="form.reviewPoint"></textarea>
        </div>

        <!-- "智能秒批"判断标准 -->
        <div class="form-group">
          <label>"智能秒批"判断标准:</label>
          <textarea v-model="form.autoApprovalCriteria"></textarea>
        </div>

        <!-- 是否共享和材料来源放在同一行 -->
        <div class="form-row">
          <div class="form-group">
            <label>是否共享:</label>
            <select v-model="form.shared">
              <option :value="true">是</option>
              <option :value="false">否</option>
            </select>
          </div>

          <div class="form-group">
            <label>材料来源:</label>
            <select v-model="form.materialSource">
              <option value="PERSONAL_SUBMISSION">申请人自备</option>
              <option value="SYSTEM_AUTO_SHARED">系统自动获取</option>
              <option value="WANG_SHAN_PROCESSING">网上办理</option>
            </select>
          </div>
        </div>

        <!-- 办理方式及材料信息获取方式说明和是否适用告知承诺放在同一行 -->
        <div class="form-row">
          <div class="form-group">
            <label>办理方式及材料信息获取方式说明:</label>
            <select v-model="form.processingMethodAndInfoAccess">
              <option value="ONLINE_PROCESSING">网上办理，线上提交材料</option>
              <option value="SYSTEM_AUTO_WITH_FALLBACK">系统自动获取，如数据不全则需申请者提交</option>
              <option value="PAPER_CERTIFICATE">纸质证书需申请者提交</option>
            </select>
          </div>

          <div class="form-group">
            <label>是否适用告知承诺:</label>
            <select v-model="form.eligibleForPromise">
              <option :value="true">是</option>
              <option :value="false">否</option>
            </select>
          </div>
        </div>

        <!-- 是否有效 -->
        <div class="form-group">
          <label>是否有效:</label>
          <select v-model="form.valid">
            <option :value="true">已上线</option>
            <option :value="false">已下线</option>
          </select>
        </div>

        <div class="form-actions">
          <button type="button" @click="goBack" class="back-btn-form">返回</button>
          <button type="submit" class="save-btn" :disabled="submitting">{{ submitting ? '保存中...' : (isEditMode ? '保存' : '创建') }}</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
const API_BASE_URL = 'http://localhost:8000/api/materials';
const API_UPDATE = (id) => `${API_BASE_URL}/${id}`;
const API_CREATE = API_BASE_URL;

export default {
  name: 'MaterialDetail',
  props: {
    material: {
      type: Object,
      default: () => ({
        id: null,
        materialDetail: '',
        reviewPoint: '',
        autoApprovalCriteria: '',
        shared: false,
        materialSource: 'PERSONAL_SUBMISSION',
        processingMethodAndInfoAccess: 'ONLINE_PROCESSING',
        eligibleForPromise: false,
        valid: true,
        version: 1,
        __name__: ''
      })
    }
  },
  data() {
    return {
      form: { ...this.material },
      errors: {},
      submitting: false
    };
  },
  computed: {
    isEditMode() {
      return !!this.material.id;
    }
  },
  watch: {
    material: {
      handler(newVal) {
        this.form = { ...newVal };
      },
      deep: true
    }
  },
  methods: {
    goBack() {
      this.$emit('back');
    },

    validateForm() {
      this.errors = {};

      if (!this.form.materialDetail || this.form.materialDetail.trim() === '') {
        this.errors.materialDetail = '材料明细不能为空';
        return false;
      }

      // 验证版本号
      if (this.form.version && (isNaN(this.form.version) || this.form.version < 1)) {
        this.errors.version = '版本号必须是大于0的数字';
        return false;
      }

      return true;
    },

    async handleSubmit() {
      // 防止重复提交
      if (this.submitting) {
        return;
      }

      if (!this.validateForm()) {
        return;
      }

      this.submitting = true;

      try {
        // 确保版本号存在且为正整数
        if (!this.form.version || this.form.version < 1) {
          this.form.version = 1;
        }

        let response;

        if (this.isEditMode) {
          response = await fetch(API_UPDATE(this.form.id), {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify(this.form)
          });
        } else {
          response = await fetch(API_CREATE, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify(this.form)
          });
        }

        if (response.ok) {
          const updatedMaterial = await response.json();
          this.$emit('material-updated', updatedMaterial);

          // 直接显示成功提示，不使用setTimeout
          alert(this.isEditMode ? '材料更新成功' : '材料创建成功');

          // 更新表单数据为返回的数据，保持在当前界面
          this.form = { ...updatedMaterial };
        } else {
          const errorText = await response.text();
          alert((this.isEditMode ? '更新' : '创建') + '失败: ' + errorText);
        }
      } catch (error) {
        console.error('保存材料出错:', error);
        alert('保存失败: ' + error.message);
      } finally {
        this.submitting = false;
      }
    }
  }
};
</script>

<style scoped>
.material-detail-container {
  padding: 20px;
  max-width: 800px;
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

.form-group {
  margin-bottom: 20px;
}

.form-row {
  display: flex;
  gap: 15px;
}

.form-row .form-group {
  flex: 1;
  margin-bottom: 20px;
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
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group input.error {
  border-color: #f56c6c;
}

.form-group textarea {
  min-height: 80px;
  resize: vertical;
}

.error-message {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 5px;
}

.form-group input:disabled {
  background-color: #f5f7fa;
  cursor: not-allowed;
}

.form-actions {
  margin-top: 30px;
  text-align: right;
  position: relative;
  z-index: 1002;
}

.form-actions button {
  margin-left: 10px;
  padding: 10px 20px;
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

.form-actions button:disabled {
  background-color: #a0a0a0;
  cursor: not-allowed;
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

.save-btn:hover:not(:disabled) {
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
