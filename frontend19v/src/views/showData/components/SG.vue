<template>
  <div class="SG">
    <div class="S">
      <S :data="data" />
    </div>
    <div class="G">
      <Graph ref="g" />
    </div>
  </div>
</template>

<script>
import S from "../components/Search";
import Graph from "../components/Graph";

import d from "../data/data1.json";

export default {
  components: {
    S,
    Graph
  },
  data() {
    return {
      data: {}
    };
  },
  methods: {
    setG(data) {
      this.$refs.g.setData(data);
    },
    setS(data) {
      this.data = data;
    }
  },
  mounted() {
    this.setG(d);
  },
  created() {
    this.setS(d);
  }
};
</script>

<style>
</style>