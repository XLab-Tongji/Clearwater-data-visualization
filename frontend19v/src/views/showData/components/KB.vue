<template>
  <div class="KB">
    <div class="select">
      <el-select v-model="value" placeholder="选择想要查询的KPI" @change="change">
        <el-option v-for="item in kpis" :key="item.value" :label="item.label" :value="item.value"></el-option>
      </el-select>
    </div>
    <div class="K">
      <K ref="k"/>
    </div>
    <div class="B">
      <B ref="b"/>
    </div>
  </div>
</template>

<script>
import K from "../components/KPIchange";
import B from "../components/BeginEndTable";

import d from '../data/data.json'

export default {
  components: {
    K,
    B
  },
  data() {
    return {
      kpis: [
        {
          value: "选项1",
          label: "CPU_usage"
        },
        {
          value: "选项2",
          label: "Network_Output_Packets"
        },
        {
          value: "选项3",
          label: "Network_Input_Packets"
        },
        {
          value: "选项4",
          label: "Network_Output_Bytes"
        },
        {
          value: "选项5",
          label: "Network_Input_Bytes"
        },
        {
          value: "选项6",
          label: "MEM_Usage"
        },
        {
          value: "选项7",
          label: "Latency"
        }
      ],
      value: ""
    };
  },
  methods: {
    change() {
        this.setK(d);
        this.setB(d);
    //   let formData = new FormData();
    //   formData.append("kpi", this.value);
    //   axios.post(global.url + "").then(res => {
    //     setData(res);
    //   });
    },
    setK(data){
        this.$refs.k.setData(data)
    },
    setB(data){
        this.$refs.b.setData(data)
    }
  }
};
</script>

<style>
</style>