<!-- src/components/matter/sections/TimeAndApprovalSection.vue -->
<template>
  <div class="time-approval-section">
    <!-- 时限、审批层级和省厅对口指导处室放在同一行 -->
    <div class="form-row time-approval-row">
      <div class="form-group">
        <label>法定时限 (天) *</label>
        <input
          type="number"
          :value="form.legalTimeLimit"
          @input="updateForm('legalTimeLimit', $event.target.value)"
          required
          :class="{ 'error': errors.legalTimeLimit }"
        >
        <div class="error-message" v-if="errors.legalTimeLimit">{{ errors.legalTimeLimit }}</div>
      </div>
      <div class="form-group">
        <label>承诺时限 (天) *</label>
        <input
          type="number"
          :value="form.committedTimeLimit"
          @input="updateForm('committedTimeLimit', $event.target.value)"
          required
          :class="{ 'error': errors.committedTimeLimit }"
        >
        <div class="error-message" v-if="errors.committedTimeLimit">{{ errors.committedTimeLimit }}</div>
      </div>
      <div class="form-group">
        <label>审批层级 *</label>
        <select
          :value="form.approvalLevel"
          @change="updateForm('approvalLevel', $event.target.value)"
          required
          :class="{ 'error': errors.approvalLevel }"
        >
          <option value="">请选择审批层级</option>
          <option
            v-for="level in approvalLevels"
            :key="level.name"
            :value="level.name"
          >
            {{ level.description }}
          </option>
        </select>
        <div class="error-message" v-if="errors.approvalLevel">{{ errors.approvalLevel }}</div>
      </div>

      <div class="form-group">
        <label>省厅对口指导处室 *</label>
        <select
          :value="form.provincialDepartmentOffice"
          @change="updateForm('provincialDepartmentOffice', $event.target.value)"
          required
          :class="{ 'error': errors.provincialDepartmentOffice }"
        >
          <option value="">请选择省厅对口指导处室</option>
          <option
            v-for="office in provincialOffices"
            :key="office.name"
            :value="office.name"
          >
            {{ office.description }}
          </option>
        </select>
        <div class="error-message" v-if="errors.provincialDepartmentOffice">{{ errors.provincialDepartmentOffice }}</div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TimeAndApprovalSection',
  props: {
    form: {
      type: Object,
      required: true
    },
    errors: {
      type: Object,
      default: () => ({})
    },
    approvalLevels: {
      type: Array,
      default: () => []
    },
    provincialOffices: {
      type: Array,
      default: () => []
    }
  },
  methods: {
    updateForm(field, value) {
      this.$emit('update:form', field, value)
    }
  }
}
</script>

<style scoped>
.form-row {
  display: flex;
  gap: 15px;
}

.form-row .form-group {
  flex: 1;
  margin-bottom: 15px;
}

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

.form-group input:disabled {
  background-color: #f5f7fa;
  cursor: not-allowed;
}

.error-message {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 5px;
}

@media (max-width: 768px) {
  .form-row {
    flex-direction: column;
    gap: 0;
  }
}
</style>
