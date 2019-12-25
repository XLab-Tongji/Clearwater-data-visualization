<template>
  <div>
    <el-form label-width="80px">
      <el-form-item label="选择KPI:">
        <el-select v-model="kpi" placeholder="选择想要查询的KPI">
          <el-option v-for="item in kpis" :key="item.value" :label="item.label" :value="item.value"></el-option>
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="onclick">确认</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" style="width: 100%">
      <el-table-column prop="event" label="事件" width="180"></el-table-column>
      <el-table-column prop="relate" label="关联度" width="180"></el-table-column>
    </el-table>
  </div>
</template>

<script>
import global from "../global";
import axios from "axios";

export default {
  data() {
    return {
      kpis: global.kpis,
      kpi: "",
      tableData: [{ event: "", relate: "" }]
    };
  },
  methods: {
    onclick() {
    //   let formData = new FormData();
    //   formData.append('kpi',this.kpi);
    //   axios.post(global.url+"",formData).then(res=>{
    //       this.tableData = res.data.result;
    //   })
    }
  }
};
</script>

<style>
</style>