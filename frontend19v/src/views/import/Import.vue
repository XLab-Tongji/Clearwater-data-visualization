<template>
  <div>
    <div class="container">
      <div class="env">
        <Env :options="options" @func="addnewenv" />
      </div>
      <div class="importenv">
        <ImportEnv :types="types" @func="addnewtype" @submit="returntoenv"/>
      </div>
      <div class="importtype">
        <ImportType @submit="returntonewenv"/>
      </div>
    </div>
  </div>
</template>

<script>
import Env from "./Env";
import ImportEnv from "./ImportEnv";
import ImportType from "./ImportType";

import axios from 'axios';

const url = "http://0.0.0.0:8088/bbs/api/uploadSystemFile";

export default {
  components: {
    // EnvExist,
    Env,
    ImportEnv,
    ImportType
  },
  data() {
    return {
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
      types: [
        {
          value: "k8s",
          label: "k8s"
        },
        {
          value: "5g",
          label: "5g"
        }
      ]
    };
  },
  methods: {
    addnewenv() {
      let displayProps = document.getElementsByClassName("container")[0];
      displayProps.style.left = "-100%";
    },
    addnewtype(){
      let displayProps = document.getElementsByClassName("container")[0];
      displayProps.style.left = "-200%";
    },
    returntonewenv(){
      let displayProps = document.getElementsByClassName("container")[0];
      displayProps.style.left = "-100%";
    },
    returntoenv(){
      let displayProps = document.getElementsByClassName("container")[0];
      displayProps.style.left = "0%";
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