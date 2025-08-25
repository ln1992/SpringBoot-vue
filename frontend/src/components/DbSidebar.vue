<!-- src/components/DbSidebar.vue -->
<template>
  <div class="sidebar-container">
    <div
      class="menu-item"
      :class="{ active: activeItem === 'dashboard' }"
      @click="selectItem('dashboard')"
    >
      {{ msg }}
    </div>
    <div
      class="menu-item"
      :class="{ active: activeItem === 'matter' }"
      @click="selectItem('matter')"
    >
      事项管理
    </div>
    <div
      class="menu-item"
      :class="{ active: activeItem === 'material' }"
      @click="selectItem('material')"
    >
      材料管理
    </div>
    <div
      class="menu-item"
      :class="{ active: activeItem === 'approval-diagram' }"
      @click="selectItem('approval-diagram')"
    >
      审批流程图
    </div>
    <div
      class="menu-item"
      :class="{ active: activeItem === 'business-diagram' }"
      @click="selectItem('business-diagram')"
    >
      业务流程图
    </div>
  </div>
</template>

<script>
export default {
  name: 'db-sidebar',
  data() {
    return {
      msg: '仪表盘',
      activeItem: 'dashboard'
    }
  },
  methods: {
    selectItem(item) {
      this.activeItem = item;
      // 发送事件到父组件，通知选择了哪个菜单项
      this.$emit('menu-selected', item);
    }
  }
}
</script>

<style scoped>
.sidebar-container {
  display: block;
  width: 200px;
  height: 100%;
  background-color: #f5f7fa;
  border-right: 1px solid #d1d5da;
}

.menu-item {
  padding-left: 20px;
  height: 50px;
  line-height: 50px;
  cursor: pointer;
  border-bottom: 1px solid #d1d5da;
  transition: all 0.3s;
}

.menu-item.active {
  background-color: #409eff;
  color: white;
  font-weight: bold;
}

.menu-item:hover {
  background-color: #ecf5ff;
}

@media (max-width: 768px) {
  .sidebar-container {
    width: 100%;
    height: auto;
  }

  .menu-item {
    display: inline-block;
    width: calc(20% - 1px); /* 5个菜单项 */
    text-align: center;
    padding-left: 0;
    border-right: 1px solid #d1d5da;
    font-size: 12px;
  }

  .menu-item:last-child {
    border-right: none;
  }
}
</style>
