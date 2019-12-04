<template>
  <el-table :data="options" stripe style="width: 100%">
    <el-table-column prop="begin" label="Begin" width="180"></el-table-column>
    <el-table-column prop="end" label="End" width="180"></el-table-column>
  </el-table>
</template>

<script>
import axios from "axios";
import global from "../global";

export default {
  data() {
    return {
      options: []
    };
  },
  methods: {
    setData(param) {
      let data = param.Alarm;
      this.options = [];
      data.map(i => {
        let tmp = {};
        tmp.begin = global.timestampToTime(i[0]);
        tmp.end = global.timestampToTime(i[1]);
        this.options.push(tmp);
      });
    }
  }
};
</script>

<style>
</style>