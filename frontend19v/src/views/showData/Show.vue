<template>
  <div>
    <chart :options="options" :init-options="initOptions" autoresize />
    <div class="addCsv">
      <input type="file" ref="csv" />
      <el-button @click.prevent="onclick" icon="el-icon-search" circle></el-button>
    </div>
  </div>
  <!-- <div class="Show">
    <div class="graph">
      <el-card class="box-card">
        <div slot="header" class="clearfix">
          <span>知识图谱</span>
        </div>
        <div class="systemOverview"></div>
      </el-card>

      <el-card class="box-card2">
        <div slot="header" class="clearfix">
          <span>说明</span>
        </div>
        <div class="text item"></div>
      </el-card>
    </div>
    <div class="word">
      <el-card class="box-card3">
        <div slot="header" class="clearfix">
          <span>数据</span>
        </div>
        <div class="text item">
          <span>
            <chart :options="options" :init-options="initOptions" autoresize />
          </span>
        </div>
      </el-card>
    </div>
    <div class="addCsv">
      <input type="file" ref="csv" />
      <el-button @click.prevent="onclick" icon="el-icon-search" circle></el-button>
    </div>
  </div>-->
</template>

<script>
import Papa from "papaparse";
import ECharts from "../../components/ECharts";
import "echarts/lib/chart/line";

export default {
  components: {
    chart: ECharts
  },
  data() {
    return {
      initOptions: {
        renderer: "canvas"
      },
      options: {
        xAxis: {
          type: "time",
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
          // console.log(parsed.data)
          // console.log(parsed);
          this.options.xAxis.data.push(...parsed.data[1]);
          this.options.series.data.push(...parsed.data[0]);
          console.log(JSON.stringify(this.options)) 
        };
        reader.onerror = function() {};
      }
    }
  }
};
</script>


<style>
.graph {
  display: flex;
  flex-direction: row;
  width: 100%;
  height: 300px;
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



