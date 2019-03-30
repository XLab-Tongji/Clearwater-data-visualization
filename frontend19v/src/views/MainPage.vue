<template>
    <el-container>
        <el-header>
            <el-row type="flex" align="middle" style="width:100%;height:100%;">
                <el-col>
                    <i class="el-icon-menu" @click="handleCollapse"></i>
                </el-col>
            </el-row>
        </el-header>

        <el-main>
            <el-row type="flex">
                <el-col :style="{width:this.sideBarWidth()}">
                    <el-menu default-active="1-4-1" class="el-menu-vertical-demo" @open="handleOpen" @close="handleClose"
                        @select="handleSelect" :collapse="isCollapse">
                        <el-menu-item index="ERChara">
                            <i class="el-icon-view"></i>
                            <span slot="title">Entity/Relation Characteristic</span>
                        </el-menu-item>
                        <!-- <el-menu-item index="ModiData">
                            <i class="el-icon-edit"></i>
                            <span slot="title">Modify Dataset</span>
                        </el-menu-item> -->
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
                                <i class="el-icon-document"></i>
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
                    <router-view></router-view>
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
        padding-left: 0px;
    }

    .el-header {
        box-shadow: 0px 3px 4px rgba(0, 0, 0, .5);
        /* 向右和向下 */

    }
</style>

<script>
    export default {
        name: 'mainpage',
        data() {
            return {
                sidebar: 5,
                isCollapse: true
            };
        },
        methods: {
            sideBarWidth() {
                if (!this.isCollapse) {
                    return '350px';
                } else {
                    return '100px';
                }
            },
            handleSelect(key, keyPath) {
                console.log(key, keyPath);
                this.$router.push({
                    name: key
                })
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
    }
</script>