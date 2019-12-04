<template>
  <div>
    <dy ref="dy"/>
    <!-- <chart :options="options" :init-options="initOptions" autoresize /> -->
  </div>
</template>

<script>
import Papa from "papaparse";
import ECharts from "@/components/ECharts";
import "echarts/lib/chart/line";
import axios from 'axios'
import dy from '../../dygraph/dy'

import global from '../global'

export default {
  components: {
    chart: ECharts,
    dy
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
    setData(d) {
      console.log(d)
      this.$refs.dy.setData(d)
      // let data = d.SST;
      // let a = [];
      // let b = [];
      // data.map(i => {
      //   a.push(i[0]);
      //   b.push(i[1]);
      // });
      // let ans = [];
      // ans.push(a);
      // ans.push(b);

      // ans[0].map(i => {
      //   this.options.xAxis.data.push(this.timestampToTime(i));
      // });
      // this.options.series.data.push(...ans[1]);
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
</style>



