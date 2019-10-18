<template>
  <div class="enter-info">
    <el-form ref="form" :model="form" label-width="80px">
      <el-form-item label="环境类型:">
        <el-select v-model="form.type" placeholder="请选择">
          <el-option
            v-for="item in types"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          ></el-option>
          <el-option value="hello" class="addnewtype">
            <el-row>
              <i class="el-icon-plus"></i>添加新类型
            </el-row>
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="环境名称:">
        <el-input v-model="form.name"></el-input>
      </el-form-item>

      <el-form-item label="配置文件:">
        <el-upload
          action
          :auto-upload="false"
          drag
          multiple
          accept=".json"
          :on-change="handleAdd"
          :file-list="files"
        >
          <i class="el-icon-upload"></i>
          <div class="el-upload__text">
            将文件拖到此处，或
            <em>点击上传</em>
          </div>
          <div class="el-upload__tip" slot="tip">只能上传json文件，且不超过500kb</div>
        </el-upload>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="onSubmit">立即创建</el-button>
        <el-button>取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  data() {
    return {
      form: {
        type: "",
        name: ""
      },
      value: "",
      files: []
    };
  },
  props: {
    types: Array
  },
  methods: {
    onSubmit() {
      // let formData = new FormData();
      // formData.append("type", this.form.type);
      // formData.append("name", this.form.name);
      // for (let i = 0; i < this.files.length; i++) {
      //   formData.append("files[]", this.files[i]);
      // }
    },
    handleAdd(file, fileList) {
      this.files.push(file);
    }
  }
};
</script>

<style scoped>
.addnewtype {
  background-color: #d4e4f8;
}
</style>