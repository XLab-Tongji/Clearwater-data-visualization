<template>
  <div id="new-graph">
    <!-- 搜索和树 在 ../components/SearchTree 下 -->
    <search-tree></search-tree>
    <!-- 节点和关系图 -->
    <d3-network ref="net" :net-nodes="nodes" :net-links="links" :options="options" @node-click="clickNode"
      @link-click="clickLink" />
    <!-- 右下角的对节点进行操作的 button -->
    <div id="button-group">
      <el-radio-group v-model="radio">
        <el-radio-button label="1">
          <i class="el-icon-edit"></i>
        </el-radio-button>
        <el-radio-button label="2">
          <i class="el-icon-edit"></i>增加节点
        </el-radio-button>
        <el-radio-button label="3">
          <i class="el-icon-edit"></i>连线
        </el-radio-button>
        <el-radio-button label="4">
          <i class="el-icon-edit"></i>删除
        </el-radio-button>
      </el-radio-group>
    </div>
  </div>
</template>

<script>
  import D3Network from "vue-d3-network";
  import SearchTree from "../components/SearchTree.vue";

  Array.prototype.indexOf = function (val) {
    for (var i = 0; i < this.length; i++) {
      if (this[i] == val) return i;
    }
    return -1;
  };

  Array.prototype.remove = function (val) {
    var index = this.indexOf(val);
    if (index > -1) {
      this.splice(index, 1);
    }
  };

  var getCoordInDocument = function (e) {
    e = e || window.event;
    var x = e.pageX || (e.clientX +
      (document.documentElement.scrollLeft ||
        document.body.scrollLeft));
    var y = e.pageY || (e.clientY +
      (document.documentElement.scrollTop ||
        document.body.scrollTop));
    return {
      'x': x,
      'y': y
    };
  }

  export default {
    components: {
      D3Network,
      SearchTree
    },
    data() {
      return {
        id: 14,
        radio: "1",
        nodes: [{
            id: 1,
            name: "my awesome node 1"
          },
          {
            id: 2,
            name: "my node 2"
          },
          {
            id: 3,
            name: "我是有操作的节点 点我",
            _color: "orange"
          },
          {
            id: 4,
            _color: "#4466ff"
          },
          {
            id: 5
          },
          {
            id: 6
          },
          {
            id: 7
          },
          {
            id: 8
          },
          {
            id: 9
          },
          {
            id: 10
          },
          {
            id: 11
          },
          {
            id: 12
          },
          {
            id: 13
          },
          {
            id: 14
          }
        ],
        links: [{
            sid: 1,
            tid: 2,
            _color: "orange"
          },
          {
            sid: 2,
            tid: 8,
            _color: "orange"
          },
          {
            sid: 3,
            tid: 4,
            _svgAttrs: {
              "stroke-width": 8,
              opacity: 1
            },
            name: "custom link",
            _color: "darkcyan"
          },
          {
            sid: 4,
            tid: 5,
            _color: "orange"
          },
          {
            sid: 5,
            tid: 6,
            _color: "orange"
          },
          {
            sid: 7,
            tid: 8
          },
          {
            sid: 5,
            tid: 8
          },
          {
            sid: 3,
            tid: 8
          },
          {
            sid: 7,
            tid: 9
          }
        ],
        nodeSize: 35,
        canvas: false,
        nodeOperations: "<a href='#'>开机</a><br><a href='#'>关机</a>",
        notify: {},
        sourceNodeId: 0,
        targetNodeId: 0,
        offset_X: -80,
        offset_Y: 0,
        finCoor: 0,
        staCoor: 0
      };
    },
    methods: {
      clickNode(e, node) {
        // 普通点击
        if (this.radio === "1") {
          // 如果已经有弹出框则关掉
          if (this.notify.hasOwnProperty("message")) {
            this.notify.close();
          }
          // 有「操作」的结点弹出框
          if (node.id === 3) {
            // 弹出操作框 只能插入 html 片段 不能操作 dom
            // 暂时这么写吧 。。感觉之后都不能实现。。。
            this.notify = this.$notify({
              title: "请选择对结点的操作：",
              dangerouslyUseHTMLString: true,
              message: this.nodeOperations,
              offset: 50,
              duration: 0
            });
          }
        }
        // 增加节点
        if (this.radio === "2") {
          let newNode = {
            id: ++this.id,
            name: "new"
          };
          this.links.push({
            sid: node.id,
            tid: newNode.id,
            _color: "orange"
          });
          this.nodes.push(newNode);
        }
        // 增加边
        if (this.radio === "3") {
          // 节点的 id 必须唯一且从1开始
          if (this.sourceNodeId) {
            if (node.id !== this.sourceNodeId) {
              this.targetNodeId = node.id;
              this.links.push({
                sid: this.sourceNodeId,
                tid: this.targetNodeId,
                _color: "orange"
              });
              this.sourceNodeId = 0;
              this.targetNodeId = 0;
            }
          } else {
            this.sourceNodeId = node.id;
          }
        }
        if (this.radio === "4") {
          // 删除
          var removeList = []
          this.links.forEach(element => {
            if ((node.id == element.sid) || (node.id == element.tid)) {
              removeList.push(element)
            }
          });

          removeList.forEach(element => {
            this.links.remove(element)
          });

          this.nodes.forEach(element => {
            if (node.id == element.id) {
              this.nodes.remove(element)
            }
          });
        }

      },
      clickLink(e, link) {
        if (this.radio === "4") {
          // 删除
          this.links.forEach(element => {
            if (link.sid == element.sid && link.tid == element.tid) {
              this.links.remove(element);
            }
          });
        }
      },
      lcb(link) {
        link._svgAttrs = {
          "marker-end": "url(#m-end)",
          "marker-start": "url(#m-start)"
        };
        return link;
      }
    },
    computed: {
      options() {
        return {
          force: 3000,
          size: {
            h: 700
          },
          offset: {
            x: this.offset_X,
            y: this.offset_Y
          },
          nodeSize: this.nodeSize,
          nodeLabels: true,
          linkLabels: true,
          canvas: this.canvas
        };
      }
    },
    mounted() {
      var el = document.getElementsByClassName("net-svg")[0];
      el.onmousedown = (e) => {
        this.staCoor = getCoordInDocument(e)
      }
      el.onmouseup = (e) => {
        this.finCoor = getCoordInDocument(e)
        this.offset_X += this.finCoor.x - this.staCoor.x
        this.offset_Y += this.finCoor.y - this.staCoor.y
        this.staCoor = 0;
        this.finCoor = 0;
      }
      el.onmousemove = (e) => {
        if (this.staCoor) {
          this.finCoor = getCoordInDocument(e)
          this.offset_X += this.finCoor.x - this.staCoor.x
          this.offset_Y += this.finCoor.y - this.staCoor.y
          this.staCoor = this.finCoor
        }
      }
    },
    created() {}
  };
</script>

<style scoped>
  #button-group {
    position: fixed;
    right: 60px;
    bottom: 40px;
  }
</style>