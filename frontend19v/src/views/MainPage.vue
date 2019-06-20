<template>
  <el-container>
    <!-- <el-header height="0px">
           
    </el-header>-->

    <el-main>
      <el-row type="flex" align="middle">
        <el-col>
          <i class="el-icon-menu" @click="handleCollapse"></i>
        </el-col>
      </el-row>
      <el-row type="flex">
        <el-col :style="{width:this.sideBarWidth()}" class="nav">
          <el-menu
            default-active="1-4-1"
            class="el-menu-vertical-demo"
            @open="handleOpen"
            @close="handleClose"
            @select="handleSelect"
            :collapse="isCollapse"
          >
            <el-menu-item index="ERChara">
              <i class="el-icon-view"></i>
              <span slot="title">Entity/Relation Characteristic</span>
            </el-menu-item>
            <el-menu-item index="CauseRel">
              <i class="el-icon-share"></i>
              <span slot="title">因果关系图</span>
            </el-menu-item>

            <el-submenu index="ClassGraph">
              <template slot="title">
                <i class="el-icon-wallet"></i>
                <span slot="title">类图</span>
              </template>
              <el-menu-item-group>
                <span slot="title">class diagram</span>
                <el-menu-item index="ClassGraph">类图</el-menu-item>
                <el-menu-item index="DirStruct">层级图</el-menu-item>
              </el-menu-item-group>
            </el-submenu>

            <el-submenu index="ModiDataMenu">
              <template slot="title">
                <i class="el-icon-edit"></i>
                <span slot="title">Modify Dataset</span>
              </template>
              <el-menu-item-group>
                <span slot="title">Entering Information</span>
                <el-menu-item index="ModiData">KnowledgeGraph Maniplation</el-menu-item>
                <el-menu-item index="EnterRel">Entering Relationship</el-menu-item>
              </el-menu-item-group>
            </el-submenu>

            <el-submenu index="DecisionRep">
              <template slot="title">
                <i class="el-icon-data-line"></i>
                <span slot="title">Decision Report</span>
              </template>
              <el-menu-item-group>
                <span slot="title">Time Series Diagram</span>
                <el-menu-item index="series">Dashboard</el-menu-item>
              </el-menu-item-group>
              <el-menu-item-group title="Casual Loop Diagram">
                <el-menu-item index="CausaView">Dashboard</el-menu-item>
              </el-menu-item-group>
            </el-submenu>
          </el-menu>
        </el-col>

        <el-col style="width:100%;height:100%">
          <!-- <el-button>test</el-button> -->
          <keep-alive>
            <router-view></router-view>
          </keep-alive>
        </el-col>
      </el-row>
    </el-main>
  </el-container>
</template>


<style scoped>
svg {
  left: 50%;
}

.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 250px;
  min-height: 700px;
}

.el-main {
  padding: 5px;
}

.el-header {
  box-shadow: 0px 1px 10px lightgray;
  /* opacity: 60; */
}
.nav {
  margin-top: 40px;
}
.el-icon-menu {
  color: white;
  position: fixed;
  z-index: 10;
  width: 16px;
  height: 16px;
  border-radius: 20px;
  padding: 10px;
  background-color: rgba(95, 95, 95, 0.705);
  cursor: pointer;
  top: 7px;
  left: 18px;

}
</style>

<script>
export default {
  name: "mainpage",
  data() {
    return {
      sidebar: 5,
      isCollapse: true
    };
  },
  methods: {
    sideBarWidth() {
      if (!this.isCollapse) {
        return "350px";
      } else {
        return "75px";
      }
    },
    handleSelect(key, keyPath) {
      console.log(key, keyPath);
      this.$router.push({
        name: key
      });
    },
    handleOpen(key, keyPath) {
      console.log(key, keyPath);
    },
    handleClose(key, keyPath) {
      console.log(key, keyPath);
    },
    handleCollapse() {
      this.isCollapse = !this.isCollapse;
    }
  }
};
</script>