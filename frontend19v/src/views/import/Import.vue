<template>
  <div>
    <div class="container">
      <div class="env">
        <Env :options="options" @func="addnewenv" />
      </div>
      <div class="importenv">
        <ImportEnv :types="types" @func="addnewtype" @submit="returntoenv" @back="back1" />
      </div>
      <div class="importtype">
        <ImportType @submit="returntonewenv" @back="back2" />
      </div>
    </div>
  </div>
</template>

<script>
import Env from "./Env";
import ImportEnv from "./ImportEnv";
import ImportType from "./ImportType";

import axios from "axios";

const url = "http://0.0.0.0:8088/bbs/api";

export default {
  components: {
    // EnvExist,
    Env,
    ImportEnv,
    ImportType
  },
  data() {
    return {
      options: [],
      types: []
    };
  },
  mounted() {
    axios.get(url + "/getSystemTypeAndNameFile").then(res => {
      this.options = res.data.options;
      this.types = res.data.types;
    });
  },
  methods: {
    back1() {
      let displayProps = document.getElementsByClassName("container")[0];
      displayProps.style.left = "0%";
    },
    back2() {
      let displayProps = document.getElementsByClassName("container")[0];
      displayProps.style.left = "-100%";
    },
    addnewenv() {
      let displayProps = document.getElementsByClassName("container")[0];
      displayProps.style.left = "-100%";
    },
    addnewtype() {
      let displayProps = document.getElementsByClassName("container")[0];
      displayProps.style.left = "-200%";
    },
    returntonewenv() {
      setTimeout(() => {
        axios.get(url + "/getSystemTypeAndNameFile").then(res => {
          // console.log(res.data)
          this.options = res.data.options;
          this.types = res.data.types;
          console.log(this.options[0].children);
          // console.log(this.options)
        });
        let displayProps = document.getElementsByClassName("container")[0];
        displayProps.style.left = "-100%";
      }, 500);
    },
    returntoenv() {
      setTimeout(() => {
        axios.get(url + "/getSystemTypeAndNameFile").then(res => {
          this.options = res.data.options;
          this.types = res.data.types;
        });
        let displayProps = document.getElementsByClassName("container")[0];
        displayProps.style.left = "0%";
      }, 500);
    }
  }
};
</script>

<style scoped>
.container {
  position: fixed;
  width: 300%;
  display: grid;
  grid-template-columns: 1fr, 1fr, 1fr;
  grid-column-gap: 30px;
}
.env {
  grid-column: 1/2;
  justify-self: center;
  align-self: center;
}
.importenv {
  grid-column: 2/3;
  justify-self: center;
  align-self: center;
}
.importtype {
  grid-column: 3/4;
  justify-self: center;
  align-self: center;
}
</style>