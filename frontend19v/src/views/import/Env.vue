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
        <el-button @click="addNewEnv">添加新环境</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import axios from 'axios';

const url = "http://localhost:8088/bbs/api";

export default {
  data() {
    return {
      form: {},
      value: []
    };
  },
  props: {
    options: Array,
    handle: Function,
  },
  methods: {
    onSubmit() {
      let formData = new FormData();
      formData.append("type", this.value[0]);
      formData.append("env", this.value[1]);
      // axios.post(url+"", formData).then(res => {
      //   console.log(res);
      // });
    },
    addNewEnv(e){
      this.$emit('func', e); // 将当前对象 evt 传递到父组件
    }
  }
};
</script>

<style>
</style>