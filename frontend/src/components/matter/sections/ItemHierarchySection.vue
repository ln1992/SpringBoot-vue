<!-- src/components/matter/sections/ItemHierarchySection.vue -->
<template>
  <div class="item-hierarchy-section">
    <div class="form-row item-info-row">
      <!-- 主项信息 -->
      <div class="form-group item-code-group">
        <label>主项编号 *</label>
        <input
          type="number"
          :value="form.mainItemCode"
          @input="updateForm('mainItemCode', $event.target.value)"
          required
          :class="{ 'error': errors.mainItemCode }"
        >
        <div class="error-message" v-if="errors.mainItemCode">{{ errors.mainItemCode }}</div>
      </div>
      <div class="form-group item-name-group">
        <label>主项名称 *</label>
        <input
          type="text"
          :value="form.mainItemName"
          @input="updateForm('mainItemName', $event.target.value)"
          required
          :class="{ 'error': errors.mainItemName }"
        >
        <div class="error-message" v-if="errors.mainItemName">{{ errors.mainItemName }}</div>
      </div>

      <!-- 子项信息 -->
      <div class="form-group item-code-group">
        <label>子项编号</label>
        <input
          type="number"
          :value="form.subItemCode"
          @input="updateForm('subItemCode', $event.target.value)">
      </div>
      <div class="form-group item-name-group">
        <label>子项名称</label>
        <input
          type="text"
          :value="form.subItemName"
          @input="updateForm('subItemName', $event.target.value)">
      </div>

      <!-- 孙项信息 -->
      <div class="form-group item-code-group">
        <label>孙项编号</label>
        <input
          type="number"
          :value="form.grandchildItemCode"
          @input="updateForm('grandchildItemCode', $event.target.value)">
      </div>
      <div class="form-group item-name-group">
        <label>孙项名称</label>
        <input
          type="text"
          :value="form.grandchildItemName"
          @input="updateForm('grandchildItemName', $event.target.value)">
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ItemHierarchySection',
  props: {
    form: {
      type: Object,
      required: true
    },
    errors: {
      type: Object,
      default: () => ({})
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
/* 主项、子项、孙项信息行样式 */
.item-info-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.item-code-group {
  flex: 0 0 80px; /* 编号字段窄一些 */
}

.item-name-group {
  flex: 1; /* 名称字段宽一些 */
  min-width: 200px;
}

.item-code-group label,
.item-name-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #303133;
}

.item-code-group input,
.item-name-group input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group {
  margin-bottom: 15px;
}

.error-message {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 5px;
}

.form-group input.error {
  border-color: #f56c6c;
}

@media (max-width: 768px) {
  /* 移动端主项、子项、孙项信息行样式 */
  .item-info-row {
    flex-direction: column;
    gap: 15px;
  }

  .item-code-group,
  .item-name-group {
    flex: 1;
    min-width: auto;
  }
}
</style>
