<template>
  <div class="Show">
    <div class="graph">
      <el-card class="box-card">
        <div slot="header" class="clearfix">
          <span>知识图谱</span>
        </div>
        <div class="systemOverview">
          <system/>
        </div>
      </el-card>

      <el-card class="box-card2">
        <div slot="header" class="clearfix">
          <span>说明</span>
        </div>
        <div>
          <chart :options="options" :init-options="initOptions" autoresize />
        </div>
      </el-card>
    </div>
    <div class="word">
      <el-card class="box-card3">
        <div slot="header" class="clearfix">
          <span>数据</span>
        </div>
        <div class="text item">
          <span>
          </span>
        </div>
      </el-card>
    </div>
    <div class="addCsv">
      <input type="file" ref="csv" />
      <el-button @click.prevent="onclick" icon="el-icon-search" circle></el-button>
    </div>
  </div>
</template>

<script>
import Papa from "papaparse";
import ECharts from "../../components/ECharts";
import "echarts/lib/chart/line";

import SystemOverview from '../KnowledgeGraph/SystemOverview'

export default {
  components: {
    chart: ECharts,
    system:SystemOverview,
  },
  data() {
    return {
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
    onclick() {
      let file = this.$refs.csv.files[0];

      if (file) {
        let reader = new FileReader();
        reader.readAsText(file, "UTF-8");
        reader.onload = env => {
          let result = env.target.result;
          let parsed = Papa.parse(result);
          parsed.data[1].map(i => {
            this.options.xAxis.data.push(this.timestampToTime(i));
          });
          this.options.series.data.push(...parsed.data[0]);
          // console.log(JSON.stringify(this.options));
        };
        reader.onerror = function() {};
      }
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



