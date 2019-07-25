<template>
  <div class="pattern">
    <p v-for="item in items" :key="item.label">
      <el-tag size="mini" :type="item.type" effect="dark" class="circle"></el-tag>
      <span class="text">{{item.label}}</span>
    </p>
  </div>
</template>

<script>
export default {
  name: "diff-pattern",
  data() {
    return {
      items: [
        { type: "", label: "新增的节点" },
        { type: "danger", label: "删除的节点" }
      ]
    };
  }
};
</script>

<style scoped>
.pattern {
  border: 1px solid lightgray;
  position: absolute;
  right: 30px;
  top: 10px;
  padding: 0 20px;
  border-radius: 10px;
  background: white;
}
.text {
  margin-left: 10px;
}
.circle {
  padding-top: 15px;
  border-radius: 30px;
  width: 21px;
}
</style>
