<template>
  <div class="content">
    <el-form v-model="form" label-width="80px">
      <el-form-item label="选择KPI:">
        <el-select v-model="form.kpi" placeholder="选择想要查询的KPI">
          <el-option v-for="item in options.kpi" :key="item" :label="item" :value="item"></el-option>
        </el-select>
      </el-form-item>
      <!-- <el-form-item label="选择事件:">
        <el-select v-model="form.event" placeholder="选择想要查询的Event">
          <el-option v-for="item in options.event" :key="item" :label="item" :value="item"></el-option>
        </el-select>
      </el-form-item> -->
      <el-form-item>
        <el-button type="primary" @click="onclick">确认</el-button>
      </el-form-item>
      <!-- <span>关联度：{{this.vaule}} </span> -->
    </el-form>
  </div>
</template>

<script>
export default {
  data() {
    return {
      form: {
        kpi: "",
        event: ""
      },
      options: {
        kpi:[],
        event: []
      },
      vaule:'',
      data:{},
    };
  },
  methods: {
    onclick() {
        this.data.links.map(i=>{
            if(i.tid==this.form.event && i.sid==this.form.kpi){
                this.vaule = i.value;
            }
        })
    },
    setData(data) {
      this.options.kpi = data.kpis;
      this.options.event = data.events;
      this.data = data;
    }
  },
};
</script>

<style>
</style>