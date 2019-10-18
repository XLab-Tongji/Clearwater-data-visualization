<template>
  <div>
    <button>111</button>
    <button>222</button>
    <button>333</button>
    <div class="container">
      <div class="env">
        <Env :options="options" />
      </div>
      <div class="importenv">
        <ImportEnv :types="types" />
      </div>
      <div class="importtype">
        <ImportType />
      </div>
    </div>
  </div>
</template>

<script>
// import EnvExist from "./EnvExist";
import Env from "./Env";
import ImportEnv from "./ImportEnv";
import ImportType from "./ImportType";

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
  }
};
</script>

<style scoped>
.container {
  /* position: fixed; */
  width: 300%;
  display: grid;
  grid-template-columns: 1fr, 1fr, 1fr;
  grid-column-gap: 30px;
}
.env {
  grid-column: 1/2;
}
.importenv {
  grid-column: 2/3;
}
.importtype {
  grid-column: 3/4;
}
</style>