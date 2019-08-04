<template>
  <div id="serviceCall">
     <LoadingEffect></LoadingEffect>
    <!-- 节点和关系图 -->
    <div @mouseover="showLinkLabel">
      <d3-network
        ref="net"
        :net-nodes="nodes"
        :net-links="links"
        :options="options"
        :selection="selection"
        @node-click="clickNode"
        @link-click="clickLink"
        :class="svgClass"
        :link-cb="lcb"
        :node-cb="ncb"
      />
    </div>
    <!-- 箭头 -->
    <svg>
      <defs>
        <!-- 普通箭头 -->
        <marker
          id="m-end"
          markerWidth="10"
          markerHeight="10"
          :refX="nodeSize / 8 + 2.5"
          refY="1.8"
          orient="auto"
          markerUnits="strokeWidth"
        >
          <path d="M0,0 L0,3.6 L3.6,1.8 z"/>
          <!-- <path d="M0,0 L0,6 L9,3 z" fill="#f00" /> -->
        </marker>
        <!-- 高亮箭头 -->
        <marker
          id="m-end-selected"
          markerWidth="10"
          markerHeight="10"
          :refX="nodeSize / 8 + 3.3"
          refY="1.8"
          orient="auto"
          markerUnits="strokeWidth"
        >
          <path d="M0,0 L0,3.6 L3.6,1.8 z" />
        </marker>
      </defs>
    </svg>
    <!-- 右下角的对节点进行操作的 button -->
    <div id="button-group">
      <el-radio-group v-model="radio">
        <!-- 普通点击 -->
        <el-tooltip class="item" effect="dark" content="查看" placement="top-start">
          <el-radio-button label="1">
            <i class="el-icon-view"></i>
          </el-radio-button>
        </el-tooltip>
        <!-- 添加节点 -->
        <el-tooltip class="item" effect="dark" content="添加节点" placement="top-start">
          <el-radio-button label="2">
            <i class="el-icon-plus"></i>
          </el-radio-button>
        </el-tooltip>
        <!-- 添加关系 -->
        <el-tooltip class="item" effect="dark" content="添加关系" placement="top-start">
          <el-radio-button label="3">
            <i class="el-icon-share"></i>
          </el-radio-button>
        </el-tooltip>
        <!-- 删除 -->
        <el-tooltip class="item" effect="dark" content="删除" placement="top-start">
          <el-radio-button label="4">
            <i class="el-icon-delete"></i>
          </el-radio-button>
        </el-tooltip>
        <!-- 修改 -->
        <el-tooltip class="item" effect="dark" content="修改" placement="top-start">
          <el-radio-button label="5">
            <i class="el-icon-edit"></i>
          </el-radio-button>
        </el-tooltip>
      </el-radio-group>
    </div>
    <!-- 右侧属性卡片 -->
    <el-card class="display-property">
      <div slot="header" class="clearfix">
        <span style="font-weight: bold;font-size:16px;">{{currentNode.name}}</span>
        <span style="color:#555;">（{{currentNode.type}}）</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="closeDisplayProps">关闭</el-button>
      </div>
      <el-form ref="propsForm" :model="propsForm" label-position="left">
        <el-form-item v-for="(value, key, index) in currentNode.property" :key="key" :label="key">
          <el-input :placeholder="key" v-model="propertyValues[index]"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button
            @click="getData"
            style="width:100%"
            type="primary"
            plain
            v-if="currentNode.type === 'environment'"
          >导入</el-button>
          <el-button @click="updatePropsHandler" style="width:100%" type="primary" plain v-else>确定</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <!-- 添加节点时选择类型 -->
    <el-card class="display-type-selection">
      <div slot="header" class="clearfix">
        <span>添加节点</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="closeDisplayTypeCard">关闭</el-button>
      </div>
      <label>节点类型：</label>
      <el-select class="selection-card-label" v-model="newNodeType" placeholder="请选择节点类型">
        <el-option
          v-for="(item,index) in selectNodeInfo"
          :key="index"
          :label="item.type"
          :value="item.type"
        ></el-option>
      </el-select>
      <label>节点&#32;ID：</label>
      <el-input
        class="selection-card-label"
        v-model="newNodeId"
        placeholder="请输入节点id"
        :minlength="1"
      ></el-input>
      <label>节点名称：</label>
      <el-input
        class="selection-card-label"
        v-model="newNodeName"
        placeholder="请输入节点名"
        :minlength="1"
        :maxlength="20"
      ></el-input>
      <el-button
        @click="updateTypeHandler"
        style="margin-top: 20px; width: 100%"
        type="primary"
        plain
      >确定</el-button>
    </el-card>
    <!-- 点击空白处新增节点 -->
    <transition name="infocard">
      <el-card v-if="showNewNodeInfoCard" class="new-node-info-card">
        <p>请选择节点类型:</p>
        <el-select v-model="newNodeType" placeholder="请选择">
          <el-option v-for="type in allNodeType" :key="type" :label="type" :value="type"></el-option>
        </el-select>
        <p>请输入节点标识符 (ID)：</p>
        <el-input v-model="newNodeId" placeholder="请输入内容"></el-input>
        <br />
        <br />
        <div>
          <el-button type="primary" @click="addNewNodeByClickBlank">添加节点</el-button>
          <el-button @click="showNewNodeInfoCard = false">取消</el-button>
        </div>
      </el-card>
    </transition>
  </div>
</template>

<script>
// import D3Network from "../../components/vue-d3-network/src/vue-d3-network.vue";
import D3Network from "../../components/vue-d3-network/src/d3-systemOverview.vue";
import SearchTree from "../../components/SearchTree.vue";
import axios from "axios";
import { nodeIcons } from "@/lib/nodeIcons.js";
import $ from "jquery";
import LoadingEffect from "../../components/LoadingEffect";

import serviceLinks from "./data/data.json"
import backurl from '../../Global'
HTMLCollection.prototype.forEach = Array.prototype.forEach;

const reqUrl = backurl.backurl+"/bbs";
// const reqUrl = "http://192.168.1.160:8088/bbs";
Array.prototype.indexOf = function(val) {
  for (var i = 0; i < this.length; i++) {
    if (this[i] == val) return i;
  }
  return -1;
};

Array.prototype.remove = function(val) {
  var index = this.indexOf(val);
  if (index > -1) {
    this.splice(index, 1);
  }
};

var getCoordInDocument = function(e) {
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

var timer = null;

export default {
  components: {
      LoadingEffect,
    D3Network,
    SearchTree,
  },
  data() {
    return {
      radio: "1",
      initialNode: {
        name: "Environment",
        id: "http://environment/10.60.38.181/environment",
        type: "environment",
        property: {
          masterName: "192.168.199.191", // 192.168.199.191
          podName: "sock-shop", // sock-shop
          serviceName: "sock-shop", // sock-shop
          address: "10.60.38.181", // 10.60.38.181
          namespace: "sock-shop" // sock-shop
        },
        svgSym: nodeIcons["environment"]
      },
      nodes: [],
      links: [],
      pureNodes: [],
      // normalNodes: [],
      selection: {
        links: {},
        nodes: {}
      },
      nodeSize: 40,
      fontSize: 14,
      linkWidth: 5,
      canvas: false,
      nodeOperations:
        '<input type="button" value="开机">\t<input type="button" value="关机">',
      notify: {},
      sourceNodeId: 0,
      targetNodeId: 0,
      offset_X: 0,
      offset_Y: 0,
      finCoor: 0,
      staCoor: 0,
      force: 3000,
      moveable: false,
      focusedNode: {},
      allNodeType: [
        "masterServer",
        "server",
        "pod",
        "container",
        "service",
        "environment",
        "serviceServer",
        "serviceDatabase",
        "containerNetwork",
        "containerStorage",
        //
        "namespace",
        "event",
        "timestamp",
        //
        "deletedNode",
        "deletedPropertyNode",
        "addedNode",
        "addedPropertyNode"
      ],
      styleList: [
        // 需要与 allNodeType 的位置对应
        "nodesmasterServer",
        "nodesServer",
        "nodesPod",
        "nodesContainer",
        "nodesService",
        "nodesNamespace",
        "nodesEnvironment",
        "nodesServiceServer",
        "nodesServiceDatabase",
        "nodesContainerNetwork",
        "nodesContainerStorage",
        "nodesEvent",
        "nodesTimestamp",
        "nodesDeletedNode",
        "nodesDeletedPropertyNode",
        "nodesAddedNode",
        "nodesAddedPropertyNode"
      ],
      allPropertyNodeTypes: [
        // 属性节点的类型
        "serviceServer",
        "serviceDatabase",
        "containerNetwork",
        "containerStorage"
      ],
      // 增加节点时可以选择的类型
      selectNodeInfo: [],
      allLinkType: [
        "manage",
        "deployed-in",
        "provides",
        "contains",
        "supervises",
        "has",
        "profile"
      ],
      linkStyleList: [
        "linkManage",
        "linkDeployed",
        "linkProvides",
        "linkContains",
        "linkSupervises",
        "linkHas",
        "linkProfile"
      ],
      currentNode: {},
      propertyValues: [],
      propertyKeys: [],
      propsForm: {},
      newNodeType: "",
      newNodeName: "",
      newNodeId: "",
      oldNode: {},
      // 用于新增节点时
      properties: {
        masterServer: {
          status: "",
          roles: "",
          age: 5,
          version: ""
        },
        server: {
          status: "",
          roles: "",
          age: 1,
          version: ""
        },
        pod: {
          namespace: "",
          node: "",
          startTime: "2019-4-1",
          labels: "",
          annotations: "",
          status: ""
        },
        container: {
          port: "",
          hostPort: "",
          command: "",
          state: "",
          startedTime: "",
          ready: "",
          restartCount: "",
          environment: "",
          mounts: "",
          args: "",
          lastState: "",
          liveness: "",
          readiness: ""
        },
        service: {
          type: "",
          clusterIP: "",
          externalIP: "",
          ports: "",
          age: 0
        },
        environment: {
          name: "",
          dataPort: "",
          type: ""
        },
        namespace: {
          status: "",
          age: 2
        },
        event: {
          start: "",
          end: "",
          description: ""
        }
      },
      // 添加节点时点击空白处需要填写的属性
      propertiesToFill: "",
      // 用于新增节点时判断节点间是否有关系
      allNodeRelation: [
        {
          sNodeType: "masterServer",
          tNodeType: "server",
          relName: "manage",
          relType: "manage"
        },
        {
          sNodeType: "pod",
          tNodeType: "server",
          relName: "deployed-in",
          relType: "deployed-in"
        },
        {
          sNodeType: "environment",
          tNodeType: "server",
          relName: "has",
          relType: "has"
        },
        {
          sNodeType: "pod",
          tNodeType: "container",
          relName: "contains",
          relType: "contains"
        },
        {
          sNodeType: "pod",
          tNodeType: "service",
          relName: "provides",
          relType: "provides"
        },
        {
          sNodeType: "namespace",
          tNodeType: "namespace",
          relName: "supervises",
          relType: "supervises"
        },
        {
          sNodeType: "container",
          tNodeType: "containerNetwork",
          relName: "profile",
          relType: "profile"
        },
        {
          sNodeType: "container",
          tNodeType: "containerStorage",
          relName: "profile",
          relType: "profile"
        },
        {
          sNodeType: "service",
          tNodeType: "serviceServer",
          relName: "profile",
          relType: "profile"
        },
        {
          sNodeType: "service",
          tNodeType: "serviceServer",
          relName: "profile",
          relType: "profile"
        }
      ],
      showNewNodeInfoCard: false,
      svgClass: {
        noselect: true,
        crosshair: false
      },
    };
  },
  computed: {
    // 节点数量
    id() {
      return this.nodes.length;
    },
    options() {
      return {
        force: this.force,
        size: {
          h: window.innerHeight,
          w: window.innerWidth
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
  watch: {
    // 监听被选择的 radio
    radio(newVal) {
      // 添加节点 鼠标变成十字
      if (newVal === "2") {
        this.svgClass.crosshair = true;
      } else {
        this.svgClass.crosshair = false;
      }
    }
  },
  created() {
    this.nodes.push(this.initialNode);
      // this.getData();
  },
  methods: {
    getData() {
        $('#fountainG').show();
      this.nodes = [];
      // this.nodes.push(this.initialNode) // 等后端有 env 和其他节点的关系
      this.links = [];

      var envPropertyValues = new FormData();
      envPropertyValues.append("masterName", this.propertyValues[0]); // 192.168.199.191
      envPropertyValues.append("podName", this.propertyValues[1]); // sock-shop
      envPropertyValues.append("serviceName", this.propertyValues[2]); // sock-shop
      envPropertyValues.append("address", this.propertyValues[3]); // 10.60.38.181
      envPropertyValues.append("namespace", this.propertyValues[4]); // sock-shop

      axios
        // API GET
        .get(reqUrl + "/api/getNodesAndLinks")
        // .get("/example.json")
        // .get(reqUrl + "/api/getAllByTime?time=2019-06-02 22:20:59")
        .then(response => {
            $('#fountainG').hide();
          console.log(reqUrl)
          // this.currentTimeStampNodes = response.data.nodeList.slice()
          response.data.nodeList.forEach(x => {
            x.svgSym = nodeIcons[x.type];
          });

          let allNodes = response.data.nodeList;

          this.links = serviceLinks.linkList;
          // console.log(this.links)

          // sl = JSON.parse(serviceLinks.linkList)
          // this.links = sl

          this.nodes = []

        this.pureNodes = allNodes.filter(node => {
            if (
              node.type === "service" 
            ) {
              return true;
            } else {
              return false;
            }
          });

        this.nodes = this.pureNodes;
        // console.log(this.nodes)
          
          // 在 links 和 nodes 都有数据之后
          this.eventList = response.data.eventList;
        })
        .catch(function(error) {
          // handle error
        });

      let displayProps = document.getElementsByClassName("display-property")[0];
      displayProps.style.right = "-420px";
      // displayProps.style.display = 'none'
    },
    // yyyy-MM-ddThh:mm:ss -> yyyyMMdd hh:mm:ss
    frontTimeFottoEnd(time) {
      let timeArr = time.split("");
      timeArr.splice(4, 1);
      timeArr.splice(6, 1);
      return timeArr.join("");
    },
    // yyyyMMdd hh:mm:ss -> yyyy-MM-dd hh:mm:ss
    endTimeFottoFront(time) {
      let timeArr = time.split("");
      timeArr.splice(4, 0, "-");
      timeArr.splice(7, 0, "-");
      return timeArr.join("");
    },
    clickNode(e, node) {
      clearTimeout(timer);
      let _this = this;
      this.currentNode = node;
      timer = setTimeout(function() {
        //在此写单击事件要执行的代码
        _this.moveable = false;
        if (e.preventDefault) {
          /*FF 和 Chrome*/
          e.preventDefault(); // 阻止默认事件
        }
        // 普通点击
        if (_this.radio === "1") {
          // 显示连接的节点
          _this.displayNodeRelation(node);
          // 如果已经有弹出框则关掉
          if (_this.notify.hasOwnProperty("message")) {
            _this.notify.close();
          }
          // 有「操作」的结点弹出框
          if (node.id === 14) {
            // 弹出操作框 只能插入 html 片段 不能操作 dom
            // 暂时这么写吧 。。感觉之后都不能实现。。。
            _this.notify = _this.$notify({
              title: "请选择对结点的操作：",
              dangerouslyUseHTMLString: true,
              message: _this.nodeOperations,
              offset: 50,
              duration: 0
            });
          }
        }
        // 增加节点
        if (_this.radio === "2") {
          _this.displayOneNode(node);
          // 选择类型
          let typeCard = document.querySelector(".display-type-selection");
          // typeCard.style.display = 'block'
          typeCard.style.right = "0px";
          _this.oldNode = node;
          _this.updateSelectType();
        }
        // 增加边
        if (_this.radio === "3") {
          _this.displayOneNode(node);
          // 节点的 id 必须唯一且从1开始
          if (_this.sourceNodeId) {
            if (node.id !== _this.sourceNodeId) {
              _this.targetNodeId = node.id;
              _this.links.push({
                sid: _this.sourceNodeId,
                tid: _this.targetNodeId
              });
              _this.sourceNodeId = 0;
              _this.targetNodeId = 0;
            }
          } else {
            _this.sourceNodeId = node.id;
          }
        }
        if (_this.radio === "4") {
          // 删除
          let removeLinkList = []; // 要删除的边们
          let removeNodeList = _this.relationalPropertyNodes(node); // 要删除的节点（点击的节点的第一层且是属性节点
          removeNodeList.push(node);
          _this.links.forEach(element => {
            if (node.id == element.sid || node.id == element.tid) {
              removeLinkList.push(element);
            }
          });

          removeLinkList.forEach(link => {
            _this.links.remove(link);
          });

          removeNodeList.forEach(node => {
            _this.nodes.remove(node);
          });
          // 删除请求（先删除关系->怕后端出问题
          axios
            .post(reqUrl + "/api/delLinks", removeLinkList)
            .then(response => {
              axios
                .post(reqUrl + "/api/delNodes", removeNodeList)
                .then(response => {})
                .catch(error => {
                  console.log(error);
                });
            })
            .catch(error => {
              console.error(error);
            });
        }
        // 修改节点名
        if (_this.radio === "5") {
          _this.displayOneNode(node);
          _this
            .$prompt("请输入新的节点名", "修改", {
              confirmButtonText: "确定",
              cancelButtonText: "取消"
            })
            .then(({ value }) => {
              if (value) {
                node.name = value;
                axios
                  .post(reqUrl + "/api/modifyOneNode", node)
                  .then(response => {
                    if (response) {
                      _this.$message({
                        type: "success",
                        message: "修改成功"
                      });
                    }
                  })
                  .catch(error => {
                    console.log(error);
                  });
              }
            })
            .catch(() => {
              _this.$message({
                type: "info",
                message: "取消输入"
              });
            });
        }
      }, 0);
    },
    clickLink(e, link) {
      this.displayOneLink(link);
      // 修改关系名
      if (this.radio === "5") {
        this.$prompt("请输入新的关系名", "修改", {
          confirmButtonText: "确定",
          // inputPattern: /[\w!#$%&'/,
          // inputErrorMessage: '邮箱格式不正确',
          cancelButtonText: "取消"
        })
          .then(({ value }) => {
            // value 不为空
            if (value) {
              link.name = value;
              axios.post(reqUrl + "/api/modifyOneLink", link).then(response => {
                if (response) {
                  this.$message({
                    type: "success",
                    message: "修改成功"
                  });
                }
              });
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
      link._svgAttrs = {
        "stroke-width": this.linkWidth,
        opacity: 1,
        "marker-end": "url(#m-end)"
      };
      this.allLinkType.forEach((element, index, array) => {
        if (link.type == element) {
          link._linkLabelClass = this.linkStyleList[index];
        }
      });
      return link;
    },
    ncb(node) {
      this.allNodeType.forEach((element, index, array) => {
        if (node.type == element) {
          node._cssClass = this.styleList[index];
        }
      });

      // node._cssClass = "nodesInit";
      return node;
    },
    displayNodeRelation(node) {
      this.selection = {
        nodes: {},
        links: {}
      };
      // let
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
    // 返回它第一层连着的节点且是属性节点
    relationalPropertyNodes(node) {
      let relproNodes = [];
      this.links.forEach(link => {
        if (link.sid === node.id) {
          let anotherNode = this.findNodebyId(link.tid);
          if (this.allPropertyNodeTypes.indexOf(anotherNode.type) !== -1) {
            relproNodes.push(anotherNode);
          }
        } else if (link.tid === node.id) {
          let anotherNode = this.findNodebyId(link.sid);
          if (this.allPropertyNodeTypes.indexOf(anotherNode.type) !== -1) {
            relproNodes.push(anotherNode);
          }
        }
      });
      return relproNodes;
    },
    findNodebyId(id) {
      return this.nodes.find(node => {
        return node.id === id;
      });
    },
    focusNodePosition(node) {
      let netSvg = document.getElementsByClassName("net-svg")[0];
      // offset 是与中心的偏差 是一个相对值
      this.offset_X += netSvg.scrollWidth / 2 - node.x;
      this.offset_Y += netSvg.scrollHeight / 2 - node.y - 150;
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
    },
    closeDisplayProps() {
      let displayProps = document.getElementsByClassName("display-property")[0];
      displayProps.style.right = "-420px";
      // displayProps.style.display = 'none'
      this.propertyValues = [];
      this.propertyKeys = [];
    },
    closeDisplayTypeCard() {
      let typeCard = document.querySelector(".display-type-selection");
      typeCard.style.right = "-360px";
      // typeCard.style.display = 'none'
      this.newNodeType = "";
      this.oldNode = {};
      this.newNodeName = "";
      this.newNodeId = "";
      this.selectNodeInfo = [];
    },
    updatePropsHandler() {
      this.$confirm("确认要修改属性吗", "修改属性", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      })
        .then(() => {
          let index = this.nodes.indexOf(this.currentNode);
          var i = 0;
          for (var key in this.currentNode.property) {
            this.nodes[index].property[key] = this.propertyValues[i++];
          }
          this.$message({
            message: "修改成功",
            type: "success"
          });
        })
        .catch(() => {
          this.$message({
            message: "已取消修改属性",
            type: "info"
          });
        });
    },
    // 更新增加节点时的选择列表
    updateSelectType() {
      let temp = {};
      for (let rel of this.allNodeRelation) {
        if (rel.sNodeType == this.oldNode.type) {
          temp = {
            type: rel.tNodeType,
            direction: "target",
            relName: rel.relName,
            relType: rel.relType
          };
          this.selectNodeInfo.push(temp);
        } else if (rel.tNodeType == this.oldNode.type) {
          temp = {
            type: rel.sNodeType,
            direction: "source",
            relName: rel.relName,
            relType: rel.relType
          };
          this.selectNodeInfo.push(temp);
        }
      }
    },
    // 根据两边的节点类型来确定关系方向
    updateTypeHandler() {
      for (let info of this.selectNodeInfo) {
        if (info.type == this.newNodeType) {
          // 如果新节点是 source
          if (info.direction == "source") {
            this.addNewNode(
              this.newNodeId,
              this.oldNode.id,
              info.relName,
              info.relType,
              this.newNodeType,
              this.properties[this.newNodeType],
              nodeIcons[this.newNodeType]
            );
            break;
          } else {
            this.addNewNode(
              this.oldNode.id,
              this.newNodeId,
              info.relName,
              info.relType,
              this.newNodeType,
              this.properties[this.newNodeType],
              nodeIcons[this.newNodeType]
            );
          }
        }
      }
    },
    addNewNode(sid, tid, linkName, linkType, newNodeType, property, svgSym) {
      // 检查 id 是否符合标准
      if (this.newNodeId.startsWith("http://")) {
        let newNode = {
          id: this.newNodeId,
          name: this.newNodeName
            ? this.newNodeName
            : newNodeType + "-" + this.id,
          type: newNodeType,
          property: property,
          svgSym: svgSym
        };
        this.nodes.push(newNode);

        let newLink = {
          sid: sid,
          tid: tid,
          name: linkName,
          type: linkType
        };
        this.links.push(newLink);

        axios
          .post(reqUrl + "/api/addNewNode", newNode)
          .then(response => {
            axios
              .post(reqUrl + "/api/addNewLink", newLink)
              .then(response => {})
              .catch(function(error) {
                console.log(error);
              });
          })
          .catch(function(error) {
            console.log(error);
          });

        this.$message({
          message: "添加成功",
          type: "success"
        });
        this.closeDisplayTypeCard();
      } else {
        this.$message.error("添加失败，节点 ID 应为 URI");
      }
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
    },
    addNewNodeByClickBlank() {
      if (this.newNodeId.startsWith("http://")) {
        let newNode = {
          id: this.newNodeId,
          name: this.newNodeName
            ? this.newNodeName
            : this.newNodeType + "-" + this.id,
          type: this.newNodeType,
          property: this.properties[this.newNodeType]
        };

        this.nodes.push(newNode);
        axios
          .post(reqUrl + "/api/addNewNode", newNode)
          .then(response => {})
          .catch(function(error) {
            console.log(error);
          });

        this.currentNode = newNode;
        // 显示属性面板
        this.showNewNodeInfoCard = false;
        let property = this.currentNode.property;
        this.propertyKeys = Object.keys(property);
        for (var key in this.propertyKeys) {
          this.propertyValues.push(property[key]);
        }
        let displayProps = document.getElementsByClassName(
          "display-property"
        )[0];
        // displayProps.style.display = 'block'
        displayProps.style.right = "0px";
      } else {
        this.$message.error("添加失败，节点 ID 应为 URI");
      }
    }
  },
  mounted() {
      $('#fountainG').hide();
    var el = document.getElementsByClassName("net-svg")[0];
    el.onmousedown = e => {
      this.staCoor = getCoordInDocument(e);
    };
    el.onmouseup = e => {
      // 点击空白处添加节点（因事件节点而起）
      if (
        this.radio === "2" &&
        e.target.localName !== "circle" &&
        e.target.localName !== "path"
      ) {
        this.showNewNodeInfoCard = true;
      } else {
        // 点击空白处取消高亮
        this.selection = {
          nodes: {},
          links: {}
        };
        // 如果是双击显示属性面板
        if (
          e.detail === 2 &&
          (e.target.localName === "path" || e.target.localName === "circle")
        ) {
          let property = this.currentNode.property;
          this.propertyKeys = Object.keys(property);
          for (var key in property) {
            this.propertyValues.push(property[key]);
          }
          let displayProps = document.getElementsByClassName(
            "display-property"
          )[0];
          // displayProps.style.display = 'block'
          displayProps.style.right = "0px";
        }
        this.finCoor = getCoordInDocument(e);
        if (this.moveable) {
          this.offset_X += this.finCoor.x - this.staCoor.x;
          this.offset_Y += this.finCoor.y - this.staCoor.y;
        } else {
          this.moveable = true;
        }
        this.staCoor = 0;
        this.finCoor = 0;
      }
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
      if (down) {
        if (this.nodeSize > 33.5) {
          this.force = Math.max(0, this.force - 80);
          this.nodeSize = Math.max(0, this.nodeSize - 0.2);
          this.fontSize = Math.max(0, this.fontSize - 0.1);
        }

      } else {
        this.force = this.force + 80;
        this.nodeSize = this.nodeSize + 0.2;
        this.fontSize = this.fontSize + 0.1;

      }
      if (ev.preventDefault) {
        /*FF 和 Chrome*/
        ev.preventDefault(); // 阻止默认事件
      }
      return false;
    };

    addEvent(el, "mousewheel", onMouseWheel);
    addEvent(el, "DOMMouseScroll", onMouseWheel);

  }
};
</script>

<style>

#serviceCall .link {
  marker-end: url(#m-end);
}

#m-end path {
  fill: rgba(48, 161, 95, 0.692);
}

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

.crosshair {
  cursor: crosshair;
}

/* 被选中的图标 */
#serviceCall .node-svg.selected {
  /* stroke:tomato !important; */
  stroke-width: 20px !important;
  fill: tomato !important;
}

/* 被选中的 link */
#serviceCall .link.selected {
  marker-end: url(#m-end-selected);
}


/* 被选中的所有元素 */
#serviceCall .selected {
  stroke: tomato !important;
  stroke-width: 4px;
}

#serviceCall .nodesInit {
  fill: lightblue;
}

#serviceCall .nodesServer {
  fill: darkcyan;
}

#serviceCall .nodesPod {
  fill: rgb(7, 244, 188);
}

#serviceCall .nodesContainer {
  fill: dimgray;
}

#serviceCall .nodesContainerNetwork,
#serviceCall .nodesContainerStorage {
  fill: rgb(200, 255, 195);
}

#serviceCall .nodesService {
  fill: cornflowerblue;
}

#serviceCall .nodesServiceDatabase,
#serviceCall .nodesServiceServer {
  fill: lightgoldenrodyellow;
}

#serviceCall .nodesNamespace {
  fill: darkgoldenrod;
}

#serviceCall .linkManage {
  /* color: rgb(8, 113, 241); */
  /* fill: rgb(1, 1, 77); */
  fill: gray;
  text-anchor: middle;
}

#serviceCall .linkDeployed {
  fill: rgb(0, 71, 70);
  text-anchor: middle;
}

#serviceCall .linkProvides {
  fill: rgb(2, 75, 58);
  text-anchor: middle;
}

#serviceCall .linkContains {
  fill: rgb(1, 78, 18);
  text-anchor: middle;
}

#serviceCall .linkService {
  fill: rgb(68, 82, 2);
  text-anchor: middle;
}

#serviceCall .linkNamespace {
  fill: rgb(76, 2, 78);
  text-anchor: middle;
}


#m-end-selected {
  fill: tomato;
}

.display-property {
  /* display: none; */
  position: absolute;
  z-index: 10;
  top: 0;
  right: -420px;
  width: 400px;
  height: 83%;
  transition: right linear 0.2s;
  overflow: auto;
}

.display-type-selection {
  /* display: none; */
  position: absolute;
  z-index: 10;
  top: 0;
  right: -360px;
  width: 340px;
  height: 400px;
  transition: right linear 0.2s;
  overflow: auto;
}

.node-svg:hover {
  stroke-width: 30px;
}

#serviceCall #switch-p-node {
  background: white;
  border: 1px lightgray solid;
  border-radius: 10px;
  padding: 10px 20px;
  position: fixed;
  bottom: 90px;
  right: 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.new-node-info-card {
  position: absolute;
  right: 10px;
  top: 5px;
}

/* 可以设置不同的进入和离开动画 */
/* 设置持续时间和动画函数 */
.infocard-enter-active {
  transition: all 0.3s linear;
}
.infocard-leave-active {
  transition: all 0.3s linear;
}
.infocard-enter, .infocard-leave-to
/* .infocard-leave-active for below version 2.1.8 */ {
  transform: translateX(400px);
  /* opacity: 0; */
}
.selection-card-label {
  display: block;
  padding: 10px 0;
}

#serviceCall .nodesAddedNode {
  stroke: #409eff;
  stroke-width: 50px;
}

#serviceCall .nodesDeletedNode {
  stroke: #f56c6c;
  stroke-width: 50px;
}

#serviceCall .nodesDeletedPropertyNode {
  stroke: #f56c6c;
}

#serviceCall .nodesAddedPropertyNode {
  stroke: #409eff;
}
</style>