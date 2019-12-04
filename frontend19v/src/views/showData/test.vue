<template>
  <div>
    <el-card class="kpi">
      <KB />
    </el-card>
    <el-card class="graph">
      <SG />
    </el-card>
    <el-card class="img">
      <Ima />
    </el-card>
  </div>
</template>

<script>
import KB from "../showData/components/KB";
import SG from "../showData/components/SG";
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
</style>