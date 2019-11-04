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
    <!-- <el-button :plain="true" @click="open4">错误</el-button> -->
  </div>
</template>

<script>
import axios from "axios";

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
    handle: Function
  },
  methods: {
    onSubmit() {
      // let formData = new FormData();
      // formData.append("type", this.value[0]);
      // formData.append("env", this.value[1]);
      if (this.value[0]==undefined || this.value[1]==undefined) {
        this.$message.error("错了哦，您没有选择任何环境");
      } else {
        this.$router.push({
          path: "main/overview",
          query: { type: this.value[0], env: this.value[1] }
        });
      }
    },
    addNewEnv(e) {
      this.$emit("func", e); // 将当前对象 evt 传递到父组件
    },
    // open4() {
    //   this.$message.error("错了哦，这是一条错误消息");
    // }
  }
};
</script>

<style>
</style>