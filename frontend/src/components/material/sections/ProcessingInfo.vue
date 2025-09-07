<!-- src/components/material/sections/ProcessingInfo.vue -->
<template>
  <div class="processing-info-section">
    <!-- 办理方式及材料信息获取方式说明和是否适用告知承诺放在同一行 -->
    <div class="form-row">
      <div class="form-group">
        <label>办理方式及材料信息获取方式说明:</label>
        <select
          :value="form.processingMethodAndInfoAccess"
          @change="$emit('update-field', 'processingMethodAndInfoAccess', $event.target.value)">
          <option value="ONLINE_PROCESSING">网上办理，线上提交材料</option>
          <option value="SYSTEM_AUTO_WITH_FALLBACK">系统自动获取，如数据不全则需申请者提交</option>
          <option value="PAPER_CERTIFICATE">纸质证书需申请者提交</option>
        </select>
      </div>

      <div class="form-group">
        <label>是否适用告知承诺:</label>
        <select
          :value="form.eligibleForPromise"
          @change="$emit('update-field', 'eligibleForPromise', $event.target.value === 'true')">
          <option :value="true">是</option>
          <option :value="false">否</option>
        </select>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProcessingInfo',
  props: {
    form: {
      type: Object,
      required: true
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
  margin-bottom: 20px;
}

.form-group {
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

@media (max-width: 768px) {
  .form-row {
    flex-direction: column;
    gap: 0;
  }
}
</style>
