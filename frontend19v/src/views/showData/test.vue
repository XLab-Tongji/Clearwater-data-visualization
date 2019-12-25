<template>
  <div class="algo">
    <div class="kpi">
      <KB />
    </div>
    <div class="graph">
      <SG/>
    </div>
    <div class="img">
      <Ima />
    </div>
  </div>
</template>

<script>
import KB from "../showData/components/KB";
// import SG from "../showData/components/SG";
import SG from '../showData/components/new'
import Ima from "../showData/components/Image";

import d from "./data/data.json";

export default {
  components: {
    KB,
    SG,
    Ima
  }
};
</script>

<style>
.algo {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: calc(100vh) auto;
  grid-column-gap: 30px;
}
.kpi {
  grid-column: 1/2;
  grid-row: 1/2;
  justify-self: center;
  align-self: center;
}
.graph {
  grid-column: 2/3;
  grid-row: 1/2;
  justify-self: center;
  align-self: center;
}
.img {
  grid-row: 2/3;
  justify-self: center;
  align-self: center;
}
</style>