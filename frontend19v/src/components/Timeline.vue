<template>
  <!-- 时间线面板 -->
  <div id="timeline-container">
    <div class="timeline" v-for="(timeStamp,index) in orderedTimeStamps" :key="timeStamp">
      <event-label :eventList="eventList" :timeStamp="timeStamp"></event-label>
      <div class="time">
        <div class="line">-------------------------</div>
        <!-- radio 有用 name 来区分多个表单，没有 name 默认是一个 -->
        <input type="radio" :value="index" v-model="pickedTimeStamp" class="timePoint" />
        <div class="timeText">{{timeStamp.slice(0,10)}}</div>
        <div class="timeText">{{timeStamp.slice(11)}}</div>
      </div>
    </div>
    <div class="timeline">
      <div class="time">
        <div class="line">-------------------------</div>
        <input type="radio" value="now" v-model="pickedTimeStamp" class="timePoint" />
        <div class="timeText">now</div>
        <div class="timeText">&emsp;</div>
      </div>
    </div>
  </div>
</template>

<script>
import EventLabel from "@/components/EventLabel";
export default {
  props: {
    allTimeStamps: Array,
    eventList: Array
  },
  components: {
    EventLabel
  },
  data() {
    return {
      pickedTimeStamp: "now" // pick 的时间戳的 index（now 除外）
    };
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
      let lastVal = 0;
      if (newVal) {
        // 当不是第一个时间戳时
        if (newVal === "now") {
          // 如果是 now
          lastVal = this.orderedTimeStamps.length - 1;
        } else {
          lastVal = newVal - 1;
        }
      } else {
        // 如果是第一个时间戳 对比的数据是相同的就行
        lastVal = newVal;
      }
      this.$emit(
        "click",
        newVal === "now" ? "now" : this.orderedTimeStamps[newVal],
        this.orderedTimeStamps[lastVal]
      );
    }
  },
  mounted() {
    // 让滚动条在最右边
    document.getElementById(
      "timeline-container"
    ).scrollLeft = document.getElementById("timeline-container").scrollWidth;
  }
};
</script>

<style scoped>
#timeline-container {
  position: fixed;
  /* bottom: -0px; */
  bottom: -210px;
  width: 70%;
  height: 200px;
  text-align: center;
  white-space: nowrap; /* scroll x effects */
  overflow: auto;
  border: 1px solid lightgray;
  background-color: rgb(255, 255, 255);
  border-radius: 10px;
  padding-bottom: 20px;
  transition: bottom 0.3s;
}
#timeline-container::-webkit-scrollbar {
  display: none;
}
#timeline-container:hover {
  bottom: 0px;
}

.timeline {
  display: inline-block;
  width: 130px;
}
.time {
  position: relative;
  top: 35px;
}
.timePoint {
  position: relative;
  cursor: pointer;
  outline: none;
  appearance: none;
  height: 25px;
  width: 25px;
  background: rgb(153, 153, 153);
  border: 4px lightgray solid;
  border-radius: 25px;
  z-index: 2;
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
  top: 25px;
  color: lightgray;
}

.timeText {
  font-size: 14px;
  text-align: center;
  color: gray;
  /* height: 19px; */
}
</style>
