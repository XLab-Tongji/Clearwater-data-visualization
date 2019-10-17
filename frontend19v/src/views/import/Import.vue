<template>
  <div>
    <!-- <EnvExist /> -->
    <Env :options="options" />
    <ImportEnv :types="types"/>
    <ImportType />
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
        },
      ]
    };
  }
};
</script>

<style>
</style>