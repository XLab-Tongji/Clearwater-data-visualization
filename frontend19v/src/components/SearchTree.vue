
import { constants } from 'fs';
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
  props:['nodes'],
  computed: {
    data () {
      let data = [{name: 'node', data: []}, {name: 'masterNode', data: []}, {name: 'pod', data: []},
                  {name: 'container', data: []}, {name: 'service', data: []}, {name: 'namespace', data: []}, {name: 'environment', data: []}]
      let nodes = this.nodes
      nodes.forEach(node => {
        data.forEach(ele => {
          if (node.type === ele.name) {
            ele.data.push(node)
          }
        })
      })
      return data
    }
  },
  data() {
    return {
      filterText: '',
      dataProps: {
        label: 'name',
        children: 'data'
      }

    };
  },
  methods: {
    filterNode (value, data) {
      if (!value) return true
      return data.name.indexOf(value) !== -1
    },
    nodeClickHandler (data, node) {
      this.$emit('focusNode', data)
    }
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

