<template>
  <div>
      <Graph/>
      <!-- <Ima/> -->
      <BE/>
      <KPI/>
  </div>
</template>

<script>
import KPI from './components/KPIchange';
import BE from './components/BeginEndTable'
import Ima from './components/Image'
import Graph from './components/Graph'

import d from './data/data.json'

export default {
    components:{
        KPI,
        BE,
        Ima,
        Graph
    },
}
</script>

<style>

</style>