<template>
  <div class="KB">
    <div class="select">
      <el-select v-model="value" placeholder="选择想要查询的KPI" @change="change">
        <el-option v-for="item in kpi" :key="item.value" :label="item.label" :value="item.value"></el-option>
      </el-select>
    </div>
    <div class="K">
      <K ref="k" />
    </div>
    <el-collapse class="B">
      <el-collapse-item title="点击查看时间表" name="1">
        <B ref="b" />
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script>
import K from "../components/KPIchange";
import B from "../components/BeginEndTable";
import axios from "axios";
import global from "../global";

export default {
  components: {
    K,
    B
  },
  data() {
    return {
      kpi: [
        {
          value: "Network_Output_Packets",
          label: "Network_Output_Packets"
        },
        {
          value: "Network_Input_Packets",
          label: "Network_Input_Packets"
        },
        {
          value: "Network_Output_Bytes",
          label: "Network_Output_Bytes"
        },
        {
          value: "Network_Input_Bytes",
          label: "Network_Input_Bytes"
        }
      ],
      value: ""
    };
  },
  methods: {
    change() {
      let formData = new FormData();
      formData.append("kpi", this.value);
      console.log(this.value);
      axios.post(global.url + "/getClusterCSV", formData).then(res => {
        this.setK(res.data);
        this.setB(res.data);
      });
    },
    setK(data) {
      this.$refs.k.setData(data);
    },
    setB(data) {
      this.$refs.b.setData(data);
    }
  }
};
</script>

<style>

</style>