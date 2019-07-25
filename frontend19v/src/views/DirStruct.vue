<template>
  <div class="dir-struct">
    <div v-html="structSvg"></div>
  </div>
</template>

<script> 
import flowSvg from '@/lib/flowSvg.js'

export default {
  name: 'dir-struct',
  data() {
    return {
      structSvg: flowSvg.structSvg
    }
  }
}
</script>

<style>
.dir-struct {
  margin: 5% 12%;
}
.dir-struct .node {
  stroke: none;
  stroke-width: 1px;
}
</style>
