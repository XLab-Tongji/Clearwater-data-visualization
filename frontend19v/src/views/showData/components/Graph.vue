<template>
  <div @mouseover="showLinkLabel">
    <D3Network
      :net-nodes="nodes"
      :net-links="links"
      :options="options"
      :selection="selection"
    />
  </div>
</template>

<script>
import D3Network from "@/components/vue-d3-network/src/d3-systemOverview.vue";

export default {
  components: {
    D3Network,
  },
  data() {
    return {
      force: 3000,
      offset_X: 0,
      offset_Y: 0,
      nodeSize: 30,
      fontSize: 14,
      canvas: false,
      selection: {
        links: {},
        nodes: {}
      },
      nodes: [],
      links: [],
    };
  },
  computed: {
    options() {
      return {
        force: this.force,
        size: {
          h: 500,
          w: 500
        },
        offset: {
          x: this.offset_X,
          y: this.offset_Y
        },
        nodeSize: this.nodeSize,
        fontSize: this.fontSize,
        nodeLabels: true,
        linkLabels: true,
        canvas: this.canvas,
        profileLinks: []
      };
    }
  },
  methods: {
    setData(data){
      this.nodes = data.nodes;
      this.links = data.links;
    },
    showLinkLabel(e) {
      // 功能：hover 上 link 后显示 label
      // 思路：监听鼠标的 mouseover 事件，当鼠标移动到 link 上时获取到 link 的 id，
      //      通过 id 搜索到 label，改变 label 的字体大小
      if (e.target.id.indexOf("link") != -1) {
        let linkid = e.target.id;
        let labels = document.querySelectorAll("[*|href]:not([href])");
        for (let label of labels) {
          if (label.href.animVal.indexOf(linkid) != -1) {
            label.setAttribute("style", "font-size:15px;");
            setTimeout(() => {
              label.setAttribute("style", "font-size:0px;");
            }, 1000);
            break;
          }
        }
      }
    }
  }
};
</script>

<style>
</style>