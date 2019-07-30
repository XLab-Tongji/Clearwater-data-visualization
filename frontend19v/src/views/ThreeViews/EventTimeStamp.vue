<template>
  <div>
    <Hello :message="message"></Hello>
    <input type="text" v-model="message">
    <div>{{message}}</div>
  </div>
</template>

<script>
import Hello from "../../components/Hello"
export default {
  components:{
    Hello
  },
  data(){
    return{
      message:''
    }
  }
}
</script>

<style>

</style>
