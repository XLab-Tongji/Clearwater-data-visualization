<template>
  <div class="SG">
    <div class="S">
      <!-- <S ref="s"/> -->
    </div>
    <div class="G">
      <Graph ref="g" />
    </div>
  </div>
</template>

<script>
import S from "../components/Search";
import Graph from "../components/Graph";
import axios from "axios";
import global from "../global";

import d from "../data/data1.json";

export default {
  components: {
    S,
    Graph
  },
  data(){
      return{
          data:{},
      }
  },
  methods: {
    setG(data) {
      this.$refs.g.setData(data);
    },
    setS(data) {
      this.$refs.s.setData(data);
    }
  },
  mounted() {
    console.log("mounted")
  },
  created() {
    console.log("created")
    axios.get(global.url+"/getCorrelation").then(res => {
        this.setG(res.data);
    });
  }
};
</script>

<style>
</style>