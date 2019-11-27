<template>
  <div>hello</div>
</template>

<script>
import axios from 'axios';

export default {
    mounted(){
        const url="http://10.60.38.173:10081/get_image";

        axios.get(url,{
            headers:{
                "Access-Control-Allow-Origin":"*"
            }
        }).then(res=>{
            console.log(res);
        })
    }
}
</script>

<style>
</style>