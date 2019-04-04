<template>
  <div id="new-graph">
    <!-- 搜索和树 在 ../components/SearchTree 下 -->
    <search-tree v-on:focusNode="focusNode" :nodes="nodes"></search-tree>
    <!-- 箭头 -->
    <svg height=0>
      <defs>
        <!-- 普通箭头 -->
        <marker id="m-end" markerWidth="10" markerHeight="10" :refX="nodeSize / 8 + 4" refY="2" orient="auto"
          markerUnits="strokeWidth">
          <path d="M0,0 L0,4 L4,2 z"></path>
        </marker>
        <!-- 高亮箭头 -->
        <marker id="m-end-selected" markerWidth="10" markerHeight="10" :refX="nodeSize / 8 + 4" refY="2" orient="auto"
          markerUnits="strokeWidth">
          <path d="M0,0 L0,4 L4,2 z"></path>
        </marker>
      </defs>
    </svg>
    <!-- 节点和关系图 -->
    <d3-network ref="net" :net-nodes="nodes" :net-links="links" :options="options" :selection="selection"
      @node-click="clickNode" @link-click="clickLink" class="noselect" :link-cb="lcb" :node-cb="ncb" />
    <!-- 右下角的对节点进行操作的 button -->
    <div id="button-group">
      <el-radio-group v-model="radio">
        <el-radio-button label="1">
          <!-- 普通点击 -->
          <i class="el-icon-view"></i>
        </el-radio-button>
        <el-radio-button label="2">
          <!-- 添加节点 -->
          <i class="el-icon-plus"></i>
        </el-radio-button>
        <el-radio-button label="3">
          <!-- 添加关系 -->
          <i class="el-icon-share"></i>
        </el-radio-button>
        <el-radio-button label="4">
          <!-- 删除 -->
          <i class="el-icon-delete"></i>
        </el-radio-button>
        <el-radio-button label="5">
          <!-- 修改 -->
          <i class="el-icon-edit"></i>
        </el-radio-button>
      </el-radio-group>
    </div>
  </div>
</template>

<script>
  import D3Network from "../components/vue-d3-network/src/vue-d3-network.vue";
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
    var x =
      e.pageX ||
      e.clientX +
      (document.documentElement.scrollLeft || document.body.scrollLeft);
    var y =
      e.pageY ||
      e.clientY + (document.documentElement.scrollTop || document.body.scrollTop);
    return {
      x: x,
      y: y
    };
  };

  function addEvent(obj, xEvent, fn) {
    if (obj.attachEvent) {
      obj.attachEvent("on" + xEvent, fn);
    } else {
      obj.addEventListener(xEvent, fn, false);
    }
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
            name: "node 1",
            type: "node"
          },
          {
            id: 2,
            name: "node 2",
            type: "master node"
          },
          {
            id: 3,
            name: "operation",
            type: "pod"
          },
          {
            id: 4,
            name: "pod4",
            type: "pod"
          },
          {
            id: 5,
            name: "c5",
            type: "container"
          },
          {
            id: 6,
            name: "c6",
            type: "container"
          },
          {
            id: 7,
            name: "s7",
            type: "service"
          },
          {
            id: 8,
            name: "n8",
            type: "namespace"
          },
          {
            id: 9,
            name: "n9",
            type: "namespace"
          },
          {
            id: 10,
            name: "s10",
            type: "service"
          },
          {
            id: 11,
            name: "s11",
            type: "service"
          },
          {
            id: 12,
            name: "n12",
            type: "node"
          },
          {
            id: 13,
            name: "n13",
            type: "node"
          },
          {
            id: 14,
            name: "n14",
            type: "node"
          }
        ],
        links: [{
            id: 1,
            sid: 1,
            tid: 2,
            name: "contains",
            type: "contains"
          },
          {
            sid: 2,
            tid: 8,
            name: "manage",
            type: "manage"
          },
          {
            sid: 3,
            tid: 4,
            _svgAttrs: {
              "stroke-width": 8,
              opacity: 1
            },
            name: "deployed-in",
            type: "deployed-in"
          },
          {
            sid: 4,
            tid: 5,
            name: "provides",
            type: "provides"
          },
          {
            sid: 5,
            tid: 6,
            name: "contains",
            type: "contains"
          },
          {
            sid: 7,
            tid: 8,
            name: "provides",
            type: "provides"
          },
          {
            sid: 5,
            tid: 8,
            name: "manage",
            type: "manage"
          },
          {
            sid: 3,
            tid: 8,
            name: "manage",
            type: "manage"
          },
          {
            sid: 7,
            tid: 9,
            name: "manage",
            type: "manage"
          }
        ],
        selection: {
          links: {},
          nodes: {}
        },
        nodeSize: 30,
        fontSize: 14,
        linkWidth: 4,
        canvas: false,
        nodeOperations: '<input type="button" value="开机">\t<input type="button" value="关机">',
        notify: {},
        sourceNodeId: 0,
        targetNodeId: 0,
        offset_X: 0,
        offset_Y: 0,
        finCoor: 0,
        staCoor: 0,
        force: 6000,
        moveable: false,
        focusedNode: {},
        allNodeType: ["master node", "node", "pod", "container", "service", "namespace"],
        allLinkType: ["manage", "deployed-in", "provides", "contains"],
        styleList: ["nodesMasterNode", "nodesNode", "nodesPod", "nodesContainer", "nodesService", "nodesNamespace"],
        linkStyleList: ["linkManage", "linkDeployed", "linkProvides", "linkContains"]
      };
    },
    methods: {
      clickNode(e, node) {
        this.moveable = false;
        if (e.preventDefault) {
          /*FF 和 Chrome*/
          e.preventDefault(); // 阻止默认事件
        }
        // 普通点击
        if (this.radio === "1") {
          console.log(document.getElementsByClassName("nodesNamespace"))
          // 显示连接的节点
          this.displayNodeRelation(node);
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
          this.displayOneNode(node);
          let newNode = {
            id: ++this.id,
            name: "new",
            type: "node"
          };
          this.links.push({
            sid: node.id,
            tid: newNode.id
          });
          this.nodes.push(newNode);
        }
        // 增加边
        if (this.radio === "3") {
          this.displayOneNode(node);
          // 节点的 id 必须唯一且从1开始
          if (this.sourceNodeId) {
            if (node.id !== this.sourceNodeId) {
              this.targetNodeId = node.id;
              this.links.push({
                sid: this.sourceNodeId,
                tid: this.targetNodeId
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
          var removeList = [];
          this.links.forEach(element => {
            if (node.id == element.sid || node.id == element.tid) {
              removeList.push(element);
            }
          });

          removeList.forEach(element => {
            this.links.remove(element);
          });

          this.nodes.forEach(element => {
            if (node.id == element.id) {
              this.nodes.remove(element);
            }
          });
        }
        // 修改节点
        if (this.radio === "5") {
          this.displayOneNode(node);
          this.$prompt("请输入新的节点名", "修改", {
              confirmButtonText: "确定",
              cancelButtonText: "取消"
            })
            .then(({
              value
            }) => {
              if (value) {
                this.$message({
                  type: "success",
                  message: "修改成功"
                });
                node.name = value;
              }
            })
            .catch(() => {
              this.$message({
                type: "info",
                message: "取消输入"
              });
            });
        }
      },
      clickLink(e, link) {
        this.displayOneLink(link);
        if (this.radio === "5") {
          this.$prompt("请输入新的关系名", "修改", {
              confirmButtonText: "确定",
              // inputPattern: /[\w!#$%&'/,
              // inputErrorMessage: '邮箱格式不正确',
              cancelButtonText: "取消"
            })
            .then(({
              value
            }) => {
              // value 不为空
              if (value) {
                this.$message({
                  type: "success",
                  message: "修改成功"
                });
                link.name = value;
              }
            })
            .catch(() => {
              this.$message({
                type: "info",
                message: "取消输入"
              });
            });
        }
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
        link._color = "lightgray";
        link._svgAttrs = {
          "stroke-width": this.linkWidth,
          opacity: 1,
          "marker-end": "url(#m-end)",
        };
        this.allLinkType.forEach((element, index, array) => {
          if (link.type == element) {
            link._linkLabelClass = this.linkStyleList[index];
            console.log(link)
          }
        });
        return link;
      },
      ncb(node) {
        this.allNodeType.forEach((element, index, array) => {
          if (node.type == element) {
            node._cssClass = this.styleList[index];
            node._linkLabelClass = this.linkStyleList[index];
            console.log(node)
          }
        });

        // node._cssClass = "nodesInit";
        return node;
      },
      focusNode(node) {
        this.focusedNode = node;
        // console.log(this.focusedNode);
        this.displayOneNode(node);
      },
      displayNodeRelation(node) {
        this.selection = {
          nodes: {},
          links: {}
        };
        this.selection.nodes[node.id] = node;
        for (let link of this.links) {
          if (link.sid === node.id) {
            this.selection.links[link.id] = link;
            for (let node of this.nodes) {
              if (node.id === link.tid) {
                this.selection.nodes[node.id] = node;
              }
            }
          }
          if (link.tid === node.id) {
            this.selection.links[link.id] = link;
            for (let node of this.nodes) {
              if (node.id === link.sid) {
                this.selection.nodes[node.id] = node;
              }
            }
          }
        }
      },
      displayOneNode(node) {
        this.selection = {
          nodes: {},
          links: {}
        };
        this.selection.nodes[node.id] = node;
      },
      displayOneLink(link) {
        this.selection = {
          nodes: {},
          links: {}
        };
        this.selection.links[link.id] = link;
      }
    },
    computed: {
      options() {
        return {
          force: this.force,
          size: {
            h: 700
          },
          offset: {
            x: this.offset_X,
            y: this.offset_Y
          },
          nodeSize: this.nodeSize,
          fontSize: this.fontSize,
          nodeLabels: true,
          linkLabels: true,
          canvas: this.canvas
        };
      }
    },
    mounted() {
      var el = document.getElementsByClassName("net-svg")[0];
      el.onmousedown = e => {
        this.staCoor = getCoordInDocument(e);
      };
      el.onmouseup = e => {
        this.finCoor = getCoordInDocument(e);
        if (this.moveable) {
          this.offset_X += this.finCoor.x - this.staCoor.x;
          this.offset_Y += this.finCoor.y - this.staCoor.y;
        } else {
          this.moveable = true;
        }
        this.staCoor = 0;
        this.finCoor = 0;
      };
      el.onmousemove = e => {
        if (this.moveable) {
          if (this.staCoor) {
            this.finCoor = getCoordInDocument(e);
            this.offset_X += this.finCoor.x - this.staCoor.x;
            this.offset_Y += this.finCoor.y - this.staCoor.y;
            this.staCoor = this.finCoor;
          }
        }
      };

      let onMouseWheel = ev => {
        var ev = ev || window.event;
        var down = true; // 定义一个标志，当滚轮向下滚时，执行一些操作
        down = ev.wheelDelta ? ev.wheelDelta < 0 : ev.detail > 0;
        if (!down) {
          this.force = Math.max(0, this.force - 200);
          this.nodeSize = Math.max(0, this.nodeSize - 1);
          this.fontSize = Math.max(0, this.fontSize - 0.3);

          var list = document.getElementsByClassName("nodesNamespace");
          for (var i = 0; i < list.length; i++) {
            let element = list[i]
            this.$set(element.attributes[0], 'value', parseFloat(element.attributes[0].value) - 0.5);
          }

        } else {
          this.force = this.force + 200;
          this.nodeSize = this.nodeSize + 1;
          this.fontSize = this.fontSize + 0.3;


          var list = document.getElementsByClassName("nodesNamespace");
          for (var i = 0; i < list.length; i++) {
            let element = list[i]
            this.$set(element.attributes[0], 'value', parseFloat(element.attributes[0].value) + 0.5);
          }



        }
        if (ev.preventDefault) {
          /*FF 和 Chrome*/
          ev.preventDefault(); // 阻止默认事件
        }
        return false;
      };

      addEvent(el, "mousewheel", onMouseWheel);
      addEvent(el, "DOMMouseScroll", onMouseWheel);
    },
    created() {}
  };
</script>

<style>
  #button-group {
    position: fixed;
    right: 60px;
    bottom: 40px;
  }

  .noselect {
    -webkit-touch-callout: none;
    -webkit-user-select: none;
    -khtml-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;
  }

  #new-graph .selected {
    stroke: coral !important;
    stroke-width: 4px !important;
    marker-end: url(#m-end-selected);
  }

  #new-graph .nodesInit {
    fill: lightblue;
  }

  #new-graph .nodesMasterNode {
    fill: rgb(8, 113, 241);
    r: 22;
  }

  #new-graph .nodesNode {
    fill: rgb(4, 245, 241);
  }

  #new-graph .nodesPod {
    fill: rgb(7, 244, 188);
  }

  #new-graph .nodesContainer {
    fill: rgb(3, 248, 60);
  }

  #new-graph .nodesService {
    fill: rgb(209, 250, 4);
  }

  #new-graph .nodesNamespace {
    fill: rgb(1, 189, 252);
    r: 30;
  }

  #new-graph .linkManage {
    /* color: rgb(8, 113, 241); */
    fill: rgb(1, 1, 77);
    text-anchor: middle;
  }

  #new-graph .linkDeployed {
    fill: rgb(0, 71, 70);
    text-anchor: middle;
  }

  #new-graph .linkProvides {
    fill: rgb(2, 75, 58);
    text-anchor: middle;
  }

  #new-graph .linkContains {
    fill: rgb(1, 78, 18);
    text-anchor: middle;
  }

  #new-graph .linkService {
    fill: rgb(68, 82, 2);
    text-anchor: middle;
  }

  #new-graph .linkNamespace {
    fill: rgb(76, 2, 78);
    text-anchor: middle;
  }

  #m-end path {
    fill: lightgray;
    z-index: -1;
  }

  #m-end-selected {
    fill: coral
  }
</style>