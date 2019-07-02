<template>
  <div class="event-label bubble bottom" v-if="itsTime" :style="{border: '1px solid ' + color}">
    <div class="scroll-container">
      {{eventName}}
      <br />
      {{startOrEnd}}
    </div>
  </div>
</template>

<script>
import { getColor } from "@/lib/colors.js";
export default {
  name: "event-label",
  props: {
    eventList: Array,
    timeStamp: String
  },
  data() {
    return {
      eventName: "",
      startOrEnd: "",
      color: ""
    };
  },
  computed: {
    itsTime() {
      let flag = false;
      this.eventList.forEach(event => {
        if (event.starts_at === this.timeStamp) {
          this.eventName = event.name;
          this.startOrEnd = "started";
          this.color = getColor(event.id);
          flag = true;
        }
        // 某一个时间又是开始时间又是结束时间？不太可能吧
        else if (event.ends_at === this.timeStamp) {
          this.eventName = event.name;
          this.startOrEnd = "ended";
          this.color = getColor(event.id);
          flag = true;
        }
      });
      return flag;
    }
  },
  methods: {}
};
</script>

<style lang="less" scoped>
.bubble {
  background: #fff;
  border: 1px solid #d0cfd2;
  padding: 5px;
  box-shadow: 0px 0px 2px #000;
  border-radius: 4px;
  height: 50px;
  width: 110px;
  position: relative;
  top: 30px;

  &:after {
    content: "";
    position: absolute;
    border: 1px solid rgba(51, 51, 51, 0.19);
  }

  &:before {
    content: "";
    position: absolute;
    border: 1px solid #333;
  }
  /*bottom*/
  &.bottom:after {
    border-color: white transparent;
    border-width: 12px 13px 0 13px;
    bottom: -12px;
    left: 45%;
  }
  &.bottom:before {
    border-color: #999 transparent;
    border-width: 11px 13px 0 13px;
    bottom: -13px;
    left: 45%;
  }
}

.scroll-container {
  overflow: auto;
  &::-webkit-scrollbar {
    display: none;
  }
}
</style>

