<template>
  <div class="enter-info">
    <el-form ref="form" :model="form" label-width="80px">
      <el-form-item label="类型名称:">
        <el-input v-model="form.name" style="width:195px"></el-input>
      </el-form-item>

      <el-form-item label="本体文档:">
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
        <el-button @click="back">返回</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import axios from "axios";
import global from '../global'

const url = global.base_url;

export default {
  data() {
    return {
      form: {
        name: ""
      },
      files: []
    };
  },
  methods: {
    back(e) {
      this.$emit("back", e);
    },
    onSubmit(e) {
      if (
        this.form.type == "" ||
        this.form.name == "" ||
        this.files[0] == undefined
      ) {
        this.$message.error("信息填写不完整，请完善信息后提交");
      } else {
        let formData = new FormData();
        formData.append("name", this.form.name);
        formData.append("file", this.files[0]);
        axios.post(url + "/uploadTypeFile", formData).then(res => {
          console.log(res.data);
        });
        // for (let i = 0; i < this.files.length; i++) {
        //   formData.append("files[]", this.files[i]);
        // }
        this.$emit("submit", e);
      }
    },
    handleAdd(file, fileList) {
      this.files.push(file.raw);
    }
  }
};
</script>

<style>
</style>