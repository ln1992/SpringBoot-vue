<!-- src/App.vue -->
<template>
  <div class="wrapper">
    <db-header></db-header>
    <el-row class="container">
      <el-col :span="4" class="menu">
        <db-sidebar
          @menu-selected="handleMenuSelect"
          :key="sidebarKey"
        ></db-sidebar>
      </el-col>
      <el-col :span="20" class="content">
        <div v-if="selectedMenu === 'dashboard'">
          <db-filterinput></db-filterinput>
          <db-table></db-table>
        </div>
        <div v-else-if="selectedMenu === 'material'">
          <material-list ref="materialList"></material-list>
        </div>
        <!-- 添加事项管理视图 -->
        <div v-else-if="selectedMenu === 'matter'">
          <matter-list ref="matterList"></matter-list>
        </div>
        <!-- 添加审批流程图视图 -->
        <div v-else-if="selectedMenu === 'approval-diagram'">
          <approval-process-diagram-list ref="approvalDiagramList"></approval-process-diagram-list>
        </div>
        <!-- 添加业务流程图视图 -->
        <div v-else-if="selectedMenu === 'business-diagram'">
          <business-process-diagram-list ref="businessDiagramList"></business-process-diagram-list>
        </div>
        <!-- 添加更新记录视图 -->
        <div v-else-if="selectedMenu === 'update-record'">
          <update-record-list ref="updateRecordList"></update-record-list>
        </div>
        <!-- 添加版本对比视图 -->
        <div v-else-if="selectedMenu === 'matter-version-compare'">
          <matter-version-compare @navigate-to-matter-detail="handleNavigateToMatterDetail"></matter-version-compare>
        </div>
      </el-col>
    </el-row>

    <el-row>
      <el-col :span="24">
        <footer class="footer">
          <db-footer></db-footer>
        </footer>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import DbHeader from './components/DbHeader.vue'
import DbSidebar from './components/DbSidebar.vue'
import DbFilterinput from './components/DbFilterinput.vue'
import DbTable from './components/DbTable.vue'
import DbFooter from './components/DbFooter.vue'
import MaterialList from './components/material/MaterialList.vue'
// 导入事项管理组件
import MatterList from './components/matter/MatterList.vue'
// 导入流程图管理组件
import ApprovalProcessDiagramList from './components/process-diagram/ApprovalProcessDiagramList.vue'
import BusinessProcessDiagramList from './components/process-diagram/BusinessProcessDiagramList.vue'
// 导入更新记录组件
import UpdateRecordList from './components/update-record/UpdateRecordList.vue'
// 导入版本对比组件
import MatterVersionCompare from './components/version-compare/MatterVersionCompare.vue'
import ElRow from "element-ui/packages/row/src/row";

export default {
  name: 'app',
  components: {
    ElRow,
    DbHeader,
    DbSidebar,
    DbFilterinput,
    DbTable,
    DbFooter,
    MaterialList,
    // 注册事项管理组件
    MatterList,
    // 注册流程图管理组件
    ApprovalProcessDiagramList,
    BusinessProcessDiagramList,
    // 注册更新记录组件
    UpdateRecordList,
    // 注册版本对比组件
    MatterVersionCompare
  },
  data() {
    return {
      selectedMenu: 'dashboard', // 默认显示dashboard
      sidebarKey: 0 // 用于强制刷新sidebar组件
    }
  },
  mounted() {
    // 处理URL中的参数，支持直接打开事项详情页
    this.handleUrlParams();
  },
  methods: {
    handleUrlParams() {
      // 解析URL中的hash部分
      const hash = window.location.hash;
      if (hash.startsWith('#/matters/')) {
        const matterId = hash.substring(10); // 提取ID部分
        if (matterId && !isNaN(matterId)) {
          // 如果URL包含事项ID，则切换到事项管理视图
          this.selectedMenu = 'matter';
          
          // 在下一个DOM更新周期中调用事项列表组件的方法
          this.$nextTick(() => {
            // 确保事项列表组件已加载
            if (this.$refs.matterList) {
              // 调用组件方法显示指定ID的事项详情
              this.$refs.matterList.showMatterDetail(parseInt(matterId));
            }
          });
        }
      }
    },
    
    handleMenuSelect(menuItem) {
      // 如果点击的是当前已选中的菜单项，则强制刷新到列表视图
      if (this.selectedMenu === menuItem) {
        this.resetToListView(menuItem);
      }

      // 更新选中的菜单项
      this.selectedMenu = menuItem;
    },
    
    // 添加处理从版本对比组件导航到事项详情的事件
    handleNavigateToMatterDetail(matterId) {
      // 更新当前选中的菜单项
      this.selectedMenu = 'matter';
      
      // 在下一个DOM更新周期中调用事项列表组件的方法
      this.$nextTick(() => {
        // 确保事项列表组件已加载
        if (this.$refs.matterList) {
          // 调用组件方法显示指定ID的事项详情
          this.$refs.matterList.showMatterDetail(matterId);
        }
      });
    },

    resetToListView(menuItem) {
      // 通过ref调用对应组件的重置方法
      this.$nextTick(() => {
        try {
          switch (menuItem) {
            case 'material':
              if (this.$refs.materialList && typeof this.$refs.materialList.resetToListView === 'function') {
                this.$refs.materialList.resetToListView();
              }
              break;
            case 'matter':
              if (this.$refs.matterList && typeof this.$refs.matterList.resetToListView === 'function') {
                this.$refs.matterList.resetToListView();
              }
              break;
            case 'approval-diagram':
              if (this.$refs.approvalDiagramList && typeof this.$refs.approvalDiagramList.resetToListView === 'function') {
                this.$refs.approvalDiagramList.resetToListView();
              }
              break;
            case 'business-diagram':
              if (this.$refs.businessDiagramList && typeof this.$refs.businessDiagramList.resetToListView === 'function') {
                this.$refs.businessDiagramList.resetToListView();
              }
              break;
            case 'update-record':
              if (this.$refs.updateRecordList && typeof this.$refs.updateRecordList.resetToListView === 'function') {
                this.$refs.updateRecordList.resetToListView();
              }
              break;
            default:
              break;
          }
        } catch (error) {
          console.error('重置到列表视图时出错:', error);
        }
      });
    }
  }
}
</script>

<style>
element.style {
  background-color: rgb(10, 47, 88);
}

body {
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", Arial, sans-serif;
  margin: 0;
  display: flex;
  min-height: 100vh;
  flex-direction: column;
}

.el-menu, body, html {
  height: 100%;
}

.wrapper {
  position: relative;
}

footer, div {
  display: block;
}

.container {
  padding-top: 70px;
  flex: 1;
}

.container, .wrapper {
  height: 100%;
}

.menu {
  height: 100%;
  background-color: #eef1f6;
}

.content {
  padding-top: 25px;
  padding-right: 25px;
  padding-bottom: 125px;
  padding-left: 25px;
}

.footer {
  height: 120px;
  background-color: #324057;
  color: #a4aebd;
  width: 100%;
  z-index: 1000;
  margin-top: -120px;
  line-height: 1;
  font-size: 22px;
}
</style>