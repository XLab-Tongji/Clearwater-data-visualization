<template>
  <div>
    <el-form ref="form" :model="form" label-width="80px">
      <el-form-item label="选择环境:">
        <el-cascader
          v-model="value"
          filterable
          clearable
          :show-all-levels="false"
          :options="options"
          :props="{ expandTrigger: 'hover' }"
        ></el-cascader>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="onSubmit">进入环境</el-button>
        <el-button>添加新环境</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  data() {
    return {
      form: {},
      value: [],
      type: ""
    };
  },
  props: {
    options: Array
  },
  methods: {
    onSubmit() {
      // console.log('type',this.value[0])
      // console.log('env',this.value[1])
      // console.log("转到对应环境的知识图谱");
    }
  }
};
</script>

<style>
</style>