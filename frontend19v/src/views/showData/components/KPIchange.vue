<template>
  <div>
    <div class="select">
      <el-select v-model="value" placeholder="选择想要查询的KPI" @change="change">
        <el-option v-for="item in kpis" :key="item.value" :label="item.label" :value="item.value"></el-option>
      </el-select>
    </div>
    <chart :options="options" :init-options="initOptions" autoresize />
  </div>
</template>

<script>
import Papa from "papaparse";
import ECharts from "@/components/ECharts";
import "echarts/lib/chart/line";
import axios from 'axios'

import global from '../global'

export default {
  components: {
    chart: ECharts
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
      value: "",
      initOptions: {
        renderer: "canvas"
      },
      options: {
        xAxis: {
          type: "category",
          data: []
        },
        yAxis: {
          type: "value"
        },
        series: {
          data: [],
          type: "line"
        }
      }
    };
  },
  methods: {
    change(){
      let formData = new FormData();
      formData.append("kpi", this.value);
      axios.post(global.url+"",).then(res=>{
        setData(res);
      })
    },
    setData(d) {
      let data = d.SST;
      let a = [];
      let b = [];
      data.map(i => {
        a.push(i[0]);
        b.push(i[1]);
      });
      let ans = [];
      ans.push(a);
      ans.push(b);

      ans[0].map(i => {
        this.options.xAxis.data.push(this.timestampToTime(i));
      });
      this.options.series.data.push(...ans[1]);
    },
    timestampToTime(timestamp) {
      var date = new Date(timestamp * 1000); //时间戳为10位需*1000，时间戳为13位的话不需乘1000
      var Y = date.getFullYear() + "-";
      var M =
        (date.getMonth() + 1 < 10
          ? "0" + (date.getMonth() + 1)
          : date.getMonth() + 1) + "-";
      var D =
        (date.getDate() < 10 ? "0" + date.getDate() : date.getDate()) + " ";
      var h =
        (date.getHours() < 10 ? "0" + date.getHours() : date.getHours()) + ":";
      var m =
        (date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes()) +
        ":";
      var s =
        date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds();
      return Y + M + D + h + m + s;
    }
  },
  mounted() {

  }
};
</script>


<style>
.graph {
  display: flex;
  flex-direction: row;
  width: 100%;
  height: 450px;
}
.text {
  font-size: 14px;
}

.item {
  margin-bottom: 18px;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both;
}
.box-card {
  width: 60%;
}
.box-card2 {
  width: 40%;
}
.box-card3 {
  height: 270px;
}
</style>



