
import { constants } from 'fs';
import { constants } from 'fs';
<template>
  <div id="search-tree">
    <el-input prefix-icon="el-icon-search" placeholder="输入关键字进行过滤" v-model="filterText"></el-input>
    <el-tree
      class="filter-tree"
      :data="data"
      :props="dataProps"
      default-expand-all
      :filter-node-method="filterNode"
      ref="tree"
      @node-click="nodeClickHandler"
    ></el-tree>
  </div>
</template>

<script>
export default {
  watch: {
    filterText (val) {
      this.$refs.tree.filter(val)
    }
  },
  data() {
    return {
      filterText: '',
      nodes: [
        { id: 1, name: 'my awesome node 1', type: 'node'},
        { id: 2, name: 'my node 2', type: 'master node'},
        { id: 3, name: '我是有操作的节点 点我', _color: "orange" ,type: 'pod'},
        { id: 4, name: 'pod4', _color: '#4466ff' ,type: 'pod'},
        { id: 5, name: 'c5', type: 'container'},
        { id: 6, name: 'c6', type: 'container'},
        { id: 7, name: 's7', type: 'service'},
        { id: 8, name: 'n8', type: 'namespace'},
        { id: 9, name: 'n9', type: 'namespace'},
        { id: 10, name: 's10', type: 'service'},
        { id: 11, name: 's11', type: 'service'},
        { id: 12, name: 'n12', type: 'node'},
        { id: 13, name: 'n13', type: 'node'},
        { id: 14, name: 'n14', type: 'node'}
      ],
      dataProps: {
        label: 'name',
        children: 'data'
      },
      data: [{name: 'node', data: []}, {name: 'master node', data: []}, {name: 'pod', data: []},
      {name: 'container', data: []}, {name: 'service', data: []}, {name: 'namespace', data: []}],

    };
  },
  methods: {
    handleData () {
      let nodes = this.nodes
      nodes.forEach(node => {
        this.data.forEach(ele => {
          if (node.type === ele.name) {
            ele.data.push(node)
          }
        })
      })
    },
    filterNode (value, data) {
      if (!value) return true
      return data.name.indexOf(value) !== -1
    },
    nodeClickHandler (data, node) {
      this.$emit('focusNode', data)
    }
  },
  created () {
    this.handleData()
  }
};
</script>

<style scope>
#search-tree {
  position: absolute;
  width: 180px;
  float: right;
}
</style>

