<template>
  <div class="enter">
    <el-row>
      <el-cascader
        v-model="value"
        filterable
        clearable
        :show-all-levels="false"
        :options="options"
        :props="{ expandTrigger: 'hover' }"
        @change="handleChange"
      ></el-cascader>
      <el-button icon="el-icon-document-add" round>添加新环境</el-button>
    </el-row>
    <el-row>
      <el-button type="primary">进入</el-button>
    </el-row>
  </div>
</template>

<script>
export default {
  data() {
    return {
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
    handleChange(value) {}
  }
};
</script>

<style>
</style>