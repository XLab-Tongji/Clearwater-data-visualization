<template>
  <div class="SG">
    <div class="S">
      <S ref="s"/>
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
  created() {
    axios.get(global.url+"/getCorrelation").then(res => {
        this.setG(res.data);
        this.setS(res.data);
    });
  }
};
</script>

<style>
.SG {
  display: grid;
  grid-template-rows: 1fr, 1fr;
  grid-column-gap: 30px;
}
.S {
  grid-row: 1/2;
  justify-self: start;
  align-self: center;
}
.G {
  grid-row: 2/3;
  justify-self: center;
  align-self: center;
}
</style>