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
          @change="handleChange"
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
      form:{},
      value: [],
      options: [
        {
          value: "k8s",
          label: "k8s",
          children: [
            {
              value: "k8s1",
              label: "k8s1"
            },
            {
              value: "k8s2",
              label: "k8s2"
            }
          ]
        },
        {
          value: "5g",
          label: "5g",
          children: [
            {
              value: "5g1",
              label: "5g1"
            },
            {
              value: "5g2",
              label: "5g2"
            },
            {
              value: "5g3",
              label: "5g3"
            }
          ]
        }
      ],
      type: ""
    };
  },
  methods: {
    handleChange(value) {},
    onSubmit(){}  
  }
};
</script>

<style>
</style>