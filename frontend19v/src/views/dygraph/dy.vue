<template>
  <div id="hi"></div>
</template>

<script>
import Dygraph from "dygraphs";
import data from "../showData/data/data.json";
export default {
  methods: {
    setData(data) {
      let sst = data.SST;
      let csv = "a,b\n";
      if (sst) {
        sst.map(i => {
          csv += i[0] + "," + i[1] + "\n";
        });
      }
      // console.log(csv);
      let width = document.documentElement.clientWidth;
      let height = document.documentElement.clientHeight;
      console.log(width);
      console.log(height);
      let g = new Dygraph(document.getElementById("hi"), csv, {
        valueRange: [-1, 1],
        drawAxesAtZero: true,
        height:height/2,
        width:width/2
      });
    }
  }
};
</script>

<style>
</style>