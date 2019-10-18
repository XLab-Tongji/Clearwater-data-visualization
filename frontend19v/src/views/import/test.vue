<template>
  <div class="container">
    <div class="large-12 medium-12 small-12 cell">
      <label>
        File
        <input type="file" id="file" ref="file" v-on:change="handleFileUpload()" />
      </label>
      <button v-on:click="submitFile()">Submit</button>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      file: ""
    };
  },
  methods: {
    handleFileUpload() {
      this.file = this.$refs.file.files[0];
      console.log(this.file);
    },
    submitFile() {
      // Initialize the form data
      let formData = new FormData();
      // Add the form data we need to submit
      formData.append("file", this.file);
      // Make the request to the POST /single-file URL
    //   axios
    //     .post("/single-file", formData, {
    //       headers: {
    //         "Content-Type": "multipart/form-data"
    //       }
    //     })
    //     .then(function() {
    //       console.log("SUCCESS!!");
    //     })
    //     .catch(function() {
    //       console.log("FAILURE!!");
    //     });
    }
  }
};
</script>

<style>
</style>