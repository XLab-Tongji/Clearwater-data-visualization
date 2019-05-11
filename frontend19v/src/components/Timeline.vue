<template>
  <div id="timelineContainer">
    <div id="timeline" v-for="timeStamp in orderedTimeStamps" :key="timeStamp">
      <div class="time">
        <div class="line">----------------</div>
        <!-- radio 有用 name 来区分多个表单，没有 name 默认是一个 -->
        <input type="radio" :value="timeStamp" v-model="pickedTimeStamp" class="timePoint"> 
        <div class="timeText">{{timeStamp.slice(0,10)}}</div>
        <div class="timeText">{{timeStamp.slice(11)}}</div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    allTimeStamps: Array
  },
  data() {
    return {
      pickedTimeStamp: ''
    }
  },
  computed: {
    orderedTimeStamps() {
      return this.allTimeStamps.sort((before, after) => {
        if (before < after) {
          return -1;
        } else if (before > after) {
          return 1;
        } else {
          return 0;
        }
      });
    }
  },
  watch: {
    pickedTimeStamp(newVal) {
      this.$emit('click', newVal)
    }
  },
};
</script>

<style scoped>
#timelineContainer {
  position: absolute;
  bottom: 50px;
  width: 70%;
  /* height: 100px; */
  text-align: center;
  overflow: auto;
}

#timeline {
  display: inline-block;
}

.timePoint {
  cursor: pointer;
  outline: none;
  appearance: none;
  height: 25px;
  width: 25px;
  background: rgb(153, 153, 153);
  border: 4px lightgray solid;
  border-radius: 25px
}
.timePoint:hover {
  border-color: darkcyan;
  background-color: rgb(161, 216, 216);
}

.timePoint:checked {
  border-color: darkcyan;
  background-color: rgb(161, 216, 216);
  /* border-color: darkcyan; */
}

.line {
  position: relative;
  z-index: -1;
  top: 25px;
  color: lightgray
}


.timeText {
  font-size: 14px;
  text-align: center;
  color: gray;
}
</style>
