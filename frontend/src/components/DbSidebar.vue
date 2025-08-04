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
      :class="{ active: activeItem === 'material' }"
      @click="selectItem('material')"
    >
      Material
    </div>
  </div>
</template>

<script>
export default {
  name: 'db-sidebar',
  data() {
    return {
      msg: 'Dashboard',
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

<style>
.sidebar-container {
  display: block;
}

.menu-item {
  padding-left: 20px;
  height: 50px;
  line-height: 50px;
  cursor: pointer;
  border-bottom: 1px solid #d1d5da;
}

.menu-item.active {
  background-color: #3a8ee6;
  color: white;
}

.menu-item:hover {
  background-color: #ecf5ff;
}
</style>
