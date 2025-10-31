<!-- src/App.vue -->
<template>
  <div class="wrapper">
    <db-header></db-header>
    <el-row class="container">
      <el-col :span="4" class="menu">
        <!-- 传递 selectedMenu 给 DbSidebar -->
        <db-sidebar
          :active-menu="selectedMenu"
          @menu-selected="handleMenuSelect"
          :key="sidebarKey"
        ></db-sidebar>
      </el-col>
      <el-col :span="20" class="content">
        <div v-if="selectedMenu === 'dashboard'">
          <db-filterinput></db-filterinput>
          <db-table></db-table>
        </div>
        <!-- 添加服装管理视图 -->
        <div v-else-if="selectedMenu === 'clothing'">
          <clothing-list ref="clothingList"></clothing-list>
        </div>
        <!-- 添加库存记录视图 -->
        <div v-else-if="selectedMenu === 'clothing-stock-record'">
          <clothing-stock-record-list ref="clothingStockRecordList"></clothing-stock-record-list>
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
        <!-- 添加报表中心视图 -->
        <div v-else-if="selectedMenu === 'clothing-reports'">
          <clothing-report ref="clothingReport"></clothing-report>
        </div>
        <!-- 添加版本对比视图 -->
        <div v-else-if="selectedMenu === 'matter-version-compare'">
          <matter-version-compare></matter-version-compare>
        </div>
        <!-- 添加管理员面板视图 -->
        <div v-else-if="selectedMenu === 'admin'">
          <admin-panel></admin-panel>
        </div>
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
// 导入服装管理组件
import ClothingList from './components/clothing/ClothingList.vue'
// 导入库存记录组件
import ClothingStockRecordList from './components/clothing-stock-record/ClothingStockRecordList.vue'
import MaterialList from './components/material/MaterialList.vue'
// 导入事项管理组件
import MatterList from './components/matter/MatterList.vue'
// 导入流程图管理组件
import ApprovalProcessDiagramList from './components/process-diagram/ApprovalProcessDiagramList.vue'
import BusinessProcessDiagramList from './components/process-diagram/BusinessProcessDiagramList.vue'
// 导入更新记录组件
import UpdateRecordList from './components/update-record/UpdateRecordList.vue'
// 导入报表中心组件
import ClothingReport from './components/clothing-report/ClothingReport.vue'
// 导入版本对比组件
import MatterVersionCompare from './components/version-compare/MatterVersionCompare.vue'
// 导入管理员面板组件
import AdminPanel from './components/AdminPanel.vue'
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
    // 注册服装管理组件
    ClothingList,
    // 注册库存记录组件
    ClothingStockRecordList,
    // 注册事项管理组件
    MatterList,
    MaterialList,
    // 注册流程图管理组件
    ApprovalProcessDiagramList,
    BusinessProcessDiagramList,
    // 注册更新记录组件
    UpdateRecordList,
    // 注册报表中心组件
    ClothingReport,
    // 注册版本对比组件
    MatterVersionCompare,
    // 注册管理员面板组件
    AdminPanel
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
    // 添加处理流程图详情显示的新方法
    async handleDiagramDetail(menu, diagramId) {
      const showDetail = (componentRef) => {
        if (componentRef && typeof componentRef.showDiagramDetail === 'function') {
          componentRef.showDiagramDetail(parseInt(diagramId));
          return true;
        }
        return false;
      };

      // 确保组件已加载
      let attempts = 0;
      const maxAttempts = 50; // 最多尝试5秒(50 * 100ms)

      while (attempts < maxAttempts) {
        switch (menu) {
          case 'approval-diagram':
            if (showDetail(this.$refs.approvalDiagramList)) return;
            break;
          case 'business-diagram':
            if (showDetail(this.$refs.businessDiagramList)) return;
            break;
        }

        await new Promise(resolve => setTimeout(resolve, 100));
        attempts++;
      }

      console.warn(`无法在${maxAttempts/10}秒内加载${menu}组件`);
    },

    // 修复 handleUrlParams 方法中的菜单选择逻辑
    handleUrlParams() {
      // 解析URL中的hash部分
      const hash = window.location.hash;

      if (hash.startsWith('#/matters/')) {
        const matterId = hash.substring(11); // 提取ID部分
        if (matterId && !isNaN(matterId)) {
          // 如果URL包含事项ID，则切换到事项管理视图
          this.selectedMenu = 'matter';  // 确保设置正确的菜单项

          // 使用轮询方式确保事项列表组件已加载后再调用其方法
          const checkMatterList = () => {
            if (this.$refs.matterList) {
              // 调用组件方法显示指定ID的事项详情
              this.$refs.matterList.showMatterDetail(parseInt(matterId));
            } else {
              // 如果组件还未加载完成，延迟100ms后再次检查
              setTimeout(checkMatterList, 100);
            }
          };

          // 开始检查
          this.$nextTick(checkMatterList);
        }
      } else if (hash.startsWith('#/materials/')) {
        const materialId = hash.substring(12); // 提取ID部分
        if (materialId && !isNaN(materialId)) {
          // 如果URL包含材料ID，则切换到材料管理视图
          this.selectedMenu = 'material';  // 确保设置正确的菜单项

          // 使用轮询方式确保材料列表组件已加载后再调用其方法
          const checkMaterialList = () => {
            if (this.$refs.materialList) {
              // 调用组件方法显示指定ID的材料详情
              this.$refs.materialList.showMaterialDetail(parseInt(materialId));
            } else {
              // 如果组件还未加载完成，延迟100ms后再次检查
              setTimeout(checkMaterialList, 100);
            }
          };

          // 开始检查
          this.$nextTick(checkMaterialList);
        }
      } else if (hash.startsWith('#/approval-diagrams/')) {
        const diagramId = hash.substring(21); // 提取ID部分
        if (diagramId && !isNaN(diagramId)) {
          // 如果URL包含审批流程图ID，则切换到审批流程图管理视图
          this.selectedMenu = 'approval-diagram';  // 关键：设置正确的菜单项
          console.log('切换到审批流程图管理视图，ID:', diagramId);

          // 使用轮询方式确保审批流程图列表组件已加载后再调用其方法
          const checkApprovalDiagramList = () => {
            if (this.$refs.approvalDiagramList) {
              console.log('审批流程图列表组件已加载，调用showDiagramDetail方法');
              // 调用组件方法显示指定ID的流程图详情
              this.$refs.approvalDiagramList.showDiagramDetail(parseInt(diagramId));
            } else {
              console.log('审批流程图列表组件未加载，100ms后重试');
              // 如果组件还未加载完成，延迟100ms后再次检查
              setTimeout(checkApprovalDiagramList, 100);
            }
          };

          // 开始检查
          this.$nextTick(checkApprovalDiagramList);
        }
      } else if (hash.startsWith('#/business-diagrams/')) {
        const diagramId = hash.substring(20); // 提取ID部分
        if (diagramId && !isNaN(diagramId)) {
          // 如果URL包含业务流程图ID，则切换到业务流程图管理视图
          this.selectedMenu = 'business-diagram';  // 关键：设置正确的菜单项

          // 使用轮询方式确保业务流程图列表组件已加载后再调用其方法
          const checkBusinessDiagramList = () => {
            if (this.$refs.businessDiagramList) {
              // 调用组件方法显示指定ID的流程图详情
              this.$refs.businessDiagramList.showDiagramDetail(parseInt(diagramId));
            } else {
              // 如果组件还未加载完成，延迟100ms后再次检查
              setTimeout(checkBusinessDiagramList, 100);
            }
          };

          // 开始检查
          this.$nextTick(checkBusinessDiagramList);
        }
      }
    },

    handleMenuSelect(menu) {
      this.selectedMenu = menu;

      // 特殊处理各菜单项，确保从详情界面返回时能正确显示列表
      switch (menu) {

        case 'clothing':
          this.$nextTick(() => {
            if (this.$refs.clothingList) {
              this.$refs.clothingList.resetToListView();
            }
          });
          break;

         case 'clothing-stock-record':
          this.$nextTick(() => {
            if (this.$refs.clothingStockRecordList) {
              this.$refs.clothingStockRecordList.resetToListView();
            }
          });
          break;

        case 'material':
          this.$nextTick(() => {
            if (this.$refs.materialList) {
              this.$refs.materialList.resetToListView();
            }
          });
          break;

        case 'matter':
          this.$nextTick(() => {
            if (this.$refs.matterList) {
              this.$refs.matterList.resetToListView();
            }
          });
          break;

        case 'update-record':
          this.$nextTick(() => {
            if (this.$refs.updateRecordList) {
              this.$refs.updateRecordList.resetToListView();
            }
          });
          break;

        case 'approval-diagram':
          this.$nextTick(() => {
            if (this.$refs.approvalDiagramList) {
              this.$refs.approvalDiagramList.resetToListView();
            }
          });
          break;

        case 'business-diagram':
          this.$nextTick(() => {
            if (this.$refs.businessDiagramList) {
              this.$refs.businessDiagramList.resetToListView();
            }
          });
          break;
          
        case 'reports':
          this.$nextTick(() => {
            if (this.$refs.clothingReport) {
              // 报表中心不需要特殊处理
            }
          });
          break;
      }

      // 当切换菜单时，重置对应组件的状态
      this.resetComponentState(menu);
    },

    // 重置组件状态
    resetComponentState(menu) {

      // 重置服装列表视图
      if (menu !== 'clothing' && this.$refs.clothingList) {
        this.$refs.clothingList.resetToListView();
      }

      // 重置库存记录列表视图
      if (menu !== 'clothing-stock-record' && this.$refs.clothingStockRecordList) {
        this.$refs.clothingStockRecordList.resetToListView();
      }

      // 重置材料列表视图
      if (menu !== 'material' && this.$refs.materialList) {
        this.$refs.materialList.resetToListView();
      }

      // 重置事项列表视图
      if (menu !== 'matter' && this.$refs.matterList) {
        this.$refs.matterList.resetToListView();
      }

      // 重置审批流程图列表视图
      if (menu !== 'approval-diagram' && this.$refs.approvalDiagramList) {
        this.$refs.approvalDiagramList.resetToListView();
      }

      // 重置业务流程图列表视图
      if (menu !== 'business-diagram' && this.$refs.businessDiagramList) {
        this.$refs.businessDiagramList.resetToListView();
      }

      // 重置更新记录列表视图
      if (menu !== 'update-record' && this.$refs.updateRecordList) {
        this.$refs.updateRecordList.resetToListView();
      }
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
            case 'clothing':
              if (this.$refs.clothingList && typeof this.$refs.clothingList.resetToListView === 'function') {
                this.$refs.clothingList.resetToListView();
              }
              break;
            case 'clothing-stock-record':
              if (this.$refs.clothingStockRecordList && typeof this.$refs.clothingStockRecordList.resetToListView === 'function') {
                this.$refs.clothingStockRecordList.resetToListView();
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
  padding-bottom: 25px;
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