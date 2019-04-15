<template>
  <div id="new-graph">
    <!-- 搜索和树 在 ../components/SearchTree 下 -->
    <search-tree v-on:focusNode="focusNode" :nodes="nodes"></search-tree>
    <!-- 箭头 -->
    <svg height="0">
      <defs>
        <!-- 普通箭头 -->
        <marker
          id="m-end"
          markerWidth="10"
          markerHeight="10"
          :refX="nodeSize / 8 + 4"
          refY="2"
          orient="auto"
          markerUnits="strokeWidth"
        >
          <path d="M0,0 L0,4 L4,2 z"></path>
        </marker>
        <!-- 高亮箭头 -->
        <marker
          id="m-end-selected"
          markerWidth="10"
          markerHeight="10"
          :refX="nodeSize / 8 + 4"
          refY="2"
          orient="auto"
          markerUnits="strokeWidth"
        >
          <path d="M0,0 L0,4 L4,2 z"></path>
        </marker>
      </defs>
    </svg>
    <!-- 节点和关系图 -->
    <d3-network
      ref="net"
      :net-nodes="nodes"
      :net-links="links"
      :options="options"
      :selection="selection"
      @node-click="clickNode"
      @link-click="clickLink"
      class="noselect"
      :link-cb="lcb"
      :node-cb="ncb"
    />
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
    <!-- 属性卡片 -->
    <el-card class="display-property">
      <div slot="header" class="clearfix">
        <span>{{currentNode.name}}</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="closeDisplayProps">关闭</el-button>
      </div>
      <el-form ref="propsForm" :model="propsForm" label-width="80px" label-position="left">
        <el-form-item v-for="(value, key, index) in currentNode.property" :key="key" :label="key">
          <el-input :placeholder="key" v-model="propertyValues[index]"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button @click="updatePropsHandler" style="width:100%" type="primary" plain>确定</el-button>
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
      <el-select v-model="newNodeType" placeholder="请选择节点类型">
        <el-option v-for="item in allNodeType" :key="item" :label="item" :value="item"></el-option>
      </el-select>
      <br>
      <label>节点名称：</label><el-input v-model="newNodeName" placeholder="请输入节点名" minlength=1 maxlength=20 style="margin-top:10px; width:200px"></el-input>
      <el-button @click="updateTypeHandler" style="margin-top: 20px; width: 100%" type="primary" plain>确定</el-button>
    </el-card>
  </div>
</template>

<script>
import D3Network from "../components/vue-d3-network/src/vue-d3-network.vue";
import SearchTree from "../components/SearchTree.vue";

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

const nodeIcons = {
  masterNode: '',
  node: '<svg t="1554884263168" class="icon" style="" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="15590" xmlns:xlink="http://www.w3.org/1999/xlink" width="16" height="16"><defs><style type="text/css"></style></defs><circle cx="512" cy="512" r="512" stroke="" stroke-width="0" fill="white"/><path d="M784 48v928H240V48h544m0-48H240a48 48 0 0 0-48 48v928a48 48 0 0 0 48 48h544a48 48 0 0 0 48-48V48a48 48 0 0 0-48-48zM680 152v83H343v-83h337m24-24H319v131h385V128z m-23 216v83H344v-83h337m24-24H320v131h385V320zM432 528v32h-96v-32h96m16-16H320v64h128v-64z m64 192a16 16 0 1 0 16 16 16 16 0 0 0-16-16z" fill="" p-id="15591"></path><path d="M512 848m-48 0a48 48 0 1 0 96 0 48 48 0 1 0-96 0Z" fill="#1890FF" p-id="15592"></path></svg>',
  pod: '<svg t="1554883368654" class="icon" style="" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="2427" xmlns:xlink="http://www.w3.org/1999/xlink" width="16" height="16"><defs><style type="text/css"></style></defs><circle cx="512" cy="512" r="512" stroke="" stroke-width="0" fill="white"/><path d="M512 580.266667c-2.133333 0-4.266667 0-4.266667-2.133334L277.333333 465.066667c-4.266667-2.133333-6.4-8.533333-4.266666-14.933334 2.133333-4.266667 8.533333-6.4 14.933333-4.266666L512 556.8l224-113.066667c4.266667-2.133333 10.666667 0 14.933333 4.266667 2.133333 4.266667 0 10.666667-4.266666 14.933333l-230.4 115.2c0 2.133333-2.133333 2.133333-4.266667 2.133334z" fill="#ED1E79" p-id="2428"></path><path d="M512 866.133333c-6.4 0-10.666667-4.266667-10.666667-10.666666V569.6c0-6.4 4.266667-10.666667 10.666667-10.666667s10.666667 4.266667 10.666667 10.666667v285.866667c0 6.4-4.266667 10.666667-10.666667 10.666666z" fill="#ED1E79" p-id="2429"></path><path d="M742.4 981.333333c-2.133333 0-4.266667 0-4.266667-2.133333L512 868.266667 288 981.333333H277.333333L49.066667 870.4c-4.266667 0-6.4-4.266667-6.4-8.533333V569.6c0-4.266667 2.133333-8.533333 6.4-8.533333L273.066667 448V168.533333c0-4.266667 2.133333-8.533333 6.4-8.533333l230.4-115.2c2.133333-2.133333 6.4-2.133333 8.533333 0L746.666667 157.866667c4.266667 2.133333 6.4 6.4 6.4 8.533333V448l224 110.933333c4.266667 2.133333 6.4 6.4 6.4 8.533334v292.266666c0 4.266667-2.133333 8.533333-6.4 10.666667L746.666667 981.333333h-4.266667zM512 844.8c2.133333 0 4.266667 0 4.266667 2.133333L740.266667 960 960 855.466667V576l-224-110.933333c-4.266667-2.133333-6.4-6.4-6.4-8.533334V174.933333L512 66.133333 294.4 174.933333v279.466667c0 4.266667-2.133333 8.533333-6.4 8.533333L64 576v279.466667L281.6 960l224-113.066667c2.133333-2.133333 4.266667-2.133333 6.4-2.133333zM281.6 454.4z" fill="#108EE9" p-id="2430"></path></svg>',
  container: '<svg t="1554884950404" class="icon" style="" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="17333" xmlns:xlink="http://www.w3.org/1999/xlink" width="16" height="16"><defs><style type="text/css"></style></defs><circle cx="512" cy="512" r="512" stroke="" stroke-width="0" fill="white"/><path d="M998.4 455.2c-3.2-0.8-70.4-24-131.2-13.6-5.6-30.4-21.6-80.8-72-100l-5.6-2.4-5.6 1.6c-2.4 0.8-24 4.8-40 29.6-19.2 28.8-23.2 72-12 128.8h-672c-11.2 0-41.6-0.8-44 24.8-0.8 6.4-1.6 15.2-1.6 17.6-0.8 4-0.8 4.8-0.8 16 0 298.4 216 332.8 378.4 332.8 317.6 0 432.8-170.4 473.6-316 34.4 6.4 120.8 11.2 146.4-96l4-17.6-17.6-5.6z m-130.4 79.2c-17.6-4-36 7.2-40.8 25.6-64 240-272.8 290.4-436 290.4-234.4 0-338.4-90.4-338.4-292.8v-8c0.8-2.4 0.8-4.8 1.6-11.2h687.2c9.6 0 18.4-4 24-12 6.4-7.2 8-17.6 6.4-27.2-16.8-77.6 0-110.4 14.4-119.2 41.6 21.6 42.4 84 42.4 85.6v28l26.4-9.6c36-12.8 84-4.8 110.4 1.6-19.2 54.4-63.2 56.8-97.6 48.8zM71.2 382.4h85.6v85.6H71.2z" p-id="17334"></path><path d="M196 382.4h85.6v85.6H196zM321.6 382.4h85.6v85.6H321.6zM446.4 382.4h85.6v85.6H446.4zM196 252h85.6v85.6H196zM321.6 252h85.6v85.6H321.6zM446.4 252h85.6v85.6H446.4zM446.4 121.6h85.6v85.6H446.4zM572 382.4h85.6v85.6H572zM228.8 607.2c-28.8 0-52 23.2-52 52s23.2 52 52 52 52-23.2 52-52-23.2-52-52-52z m0 64c-6.4 0-12-5.6-12-12s5.6-12 12-12 12 5.6 12 12-5.6 12-12 12z" p-id="17335"></path></svg>',
  service: '<svg t="1554884138254" class="icon" style="" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="12163" xmlns:xlink="http://www.w3.org/1999/xlink" width="16" height="16"><defs><style type="text/css"></style></defs><circle cx="512" cy="512" r="512" stroke="" stroke-width="0" fill="white"/><path d="M306.820741 191.525926c-51.579259 11.188148-103.632593 63.241481-114.820741 114.820741-21.428148 99.176296 53.475556 186.785185 148.954074 186.785185l152.651852 0 0-6.447407 0-31.478519L493.605926 333.937778l-0.18963 0C489.623704 241.682963 403.816296 170.571852 306.820741 191.525926zM455.68 455.205926l-114.725926 0c-63.905185 0-115.579259-52.242963-114.725926-116.337778 0.853333-61.819259 51.294815-112.260741 113.114074-113.114074 64.094815-0.853333 116.337778 50.820741 116.337778 114.725926L455.68 455.205926z" p-id="12164"></path><path d="M832.663704 306.346667c-11.188148-51.579259-63.241481-103.632593-114.820741-114.820741-96.900741-20.954074-182.802963 50.157037-186.595556 142.506667l-0.18963 0 0 121.173333 0 31.478519 0 6.447407 152.651852 0C779.093333 493.131852 854.091852 405.522963 832.663704 306.346667zM568.983704 340.48c0-63.905185 52.242963-115.579259 116.337778-114.725926 61.819259 0.853333 112.260741 51.294815 113.114074 113.114074 0.853333 64.094815-50.820741 116.337778-114.725926 116.337778l-114.725926 0L568.983704 340.48z" p-id="12165"></path><path d="M192 717.842963c11.188148 51.579259 63.241481 103.632593 114.820741 114.820741 96.900741 20.954074 182.802963-50.157037 186.595556-142.506667l0.18963 0L493.605926 568.983704l0-31.478519 0-6.447407-152.651852 0C245.475556 531.057778 170.571852 618.666667 192 717.842963zM455.68 683.70963c0 63.905185-52.242963 115.579259-116.337778 114.725926-61.819259-0.853333-112.260741-51.294815-113.114074-113.114074-0.853333-64.094815 50.820741-116.337778 114.725926-116.337778l114.725926 0L455.68 683.70963z" p-id="12166"></path><path d="M683.70963 531.057778l-152.651852 0 0 6.447407 0 31.478519 0 121.173333 0.18963 0c3.887407 92.34963 89.694815 163.460741 186.595556 142.506667 51.579259-11.188148 103.632593-63.241481 114.820741-114.820741C854.091852 618.666667 779.093333 531.057778 683.70963 531.057778zM798.435556 685.321481C797.582222 747.140741 747.140741 797.582222 685.321481 798.435556c-64.094815 0.853333-116.337778-50.820741-116.337778-114.725926l0-114.725926 114.725926 0C747.614815 568.983704 799.288889 621.226667 798.435556 685.321481z" p-id="12167"></path></svg>',
  namespace: '<svg t="1554884521660" class="icon" style="" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="16549" xmlns:xlink="http://www.w3.org/1999/xlink" width="16" height="16"><defs><style type="text/css"></style></defs><circle cx="512" cy="512" r="512" stroke="" stroke-width="0" fill="white"/><circle cx="512" cy="512" r="512" stroke="" stroke-width="0" fill="white"/><path d="M988.576 411.328c-10.848-74.88-133.44-104.224-244-115.424V231.168h-27.36v-0.032l27.008-0.192c-0.928-127.136-318.496-130.784-354.72-130.784-31.264 0-306.496 2.912-348.8 103.904l-5.792 38.432h-0.224c0-2.112 0.256-3.872 0.512-4.96l-0.8 3.488 0.288 7.936-0.352 411.264c-0.224 86.624 138.272 121.44 245.856 135.392 6.368 123.488 318.08 128.128 357.056 128.224 35.968 0 351.52-3.616 352-130.208l0.384-372.448-1.056-9.856z m-53.664 382.176c-0.16 35.84-127.424 75.968-300.576 75.968-171.872-0.544-300.16-41.472-300.064-77.76l0.096-97.44h114.272v-54.368h-114.368v-207.808h24.64v-54.368H279.904v263.52h0.096l-0.064 99.424c-130.912-17.76-191.264-52.672-191.232-80.384l0.352-412.48 3.168-25.536c21.312-37.632 152.448-67.776 297.28-67.776 171.552 0 300.064 40.576 300.352 76.768l0.16 24.672 0.16 0.064-0.032 121.728h-127.04v54.368h127.072v208.064h-24.64v54.368h79.008v-297.888h-0.032v-45.728c119.936 13.088 186.56 43.104 190.176 67.968l0.576 4.16-0.352 370.464z" fill="" p-id="16550"></path><path d="M490.464 640.16h43.072v54.368h-43.072v-54.368zM578.464 640.16h43.104v54.368h-43.104v-54.368zM484 377.728h43.104v54.368h-43.104v-54.368zM401.728 377.728H444.8v54.368h-43.072v-54.368z" fill="" p-id="16551"></path></svg>',
  environment: '<svg t="1554883839636" class="icon" style="" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5763" xmlns:xlink="http://www.w3.org/1999/xlink" width="16" height="16"><defs><style type="text/css"></style></defs><circle cx="512" cy="512" r="512" stroke="" stroke-width="0" fill="white"/><path d="M896 166.4h-128v25.6h128v-25.6z m-486.4-76.8v204.8h512v102.4h-230.4v51.2h230.4v76.8h51.2v-435.2H409.6z m512 153.6H460.8v-102.4h460.8v102.4z m-153.6 76.8v25.6h128v-25.6h-128z" fill="#999999" p-id="5764"></path><path d="M768 524.8h-102.4c0-112.64-92.16-204.8-204.8-204.8s-204.8 92.16-204.8 204.8c-112.64 0-204.8 92.16-204.8 204.8s92.16 204.8 204.8 204.8h512c112.64 0 204.8-92.16 204.8-204.8s-92.16-204.8-204.8-204.8z m0 358.4H256c-92.16 0-153.6-61.44-153.6-153.6s61.44-153.6 153.6-153.6h51.2v-51.2c0-92.16 61.44-153.6 153.6-153.6s153.6 61.44 153.6 153.6v51.2h153.6c92.16 0 153.6 61.44 153.6 153.6s-61.44 153.6-153.6 153.6z" fill="#333333" p-id="5765"></path></svg>'
}

export default {
  components: {
    D3Network,
    SearchTree
  },
  data() {
    return {
      id: 15,
      radio: "1",
      nodes: [
        {
          id: 1,
          name: "node 1",
          type: "node",
          property: { status: "", roles: "", age: 1, version: "" },
          svgSym: nodeIcons.node
        },
        {
          id: 2,
          name: "node 2",
          type: "masterNode",
          property: { status: "", roles: "", age: 5, version: "" },
          svgSym: nodeIcons.masterNode
        },
        {
          id: 3,
          name: "operation",
          type: "pod",
          property: {
            namespace: "",
            node: "",
            startTime: "2019-4-1",
            labels: "",
            annotations: "",
            status: ""
          },
          svgSym: nodeIcons.pod
        },
        {
          id: 4,
          name: "pod4",
          type: "pod",
          property: {
            namespace: "",
            node: "",
            startTime: "",
            labels: "",
            annotations: "",
            status: ""
          },
          svgSym: nodeIcons.pod
       },
        {
          id: 5,
          name: "c5",
          type: "container",
          property: {
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
          svgSym: nodeIcons.container
        },
        {
          id: 6,
          name: "c6",
          type: "container",
          property: {
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
          svgSym: nodeIcons.container
        },
        {
          id: 7,
          name: "s7",
          type: "service",
          property: {
            type: "",
            clusterIP: "",
            externalIP: "",
            ports: "",
            age: 0
          },
          svgSym: nodeIcons.service
        },
        {
          id: 8,
          name: "monitoring",
          type: "namespace",
          property: { status: "", age: 2 },
          svgSym: nodeIcons.namespace
        },
        {
          id: 9,
          name: "sock-shop",
          type: "namespace",
          property: { status: "", age: 10 },
          svgSym: nodeIcons.namespace
        },
        {
          id: 10,
          name: "s10",
          type: "service",
          property: {
            type: "",
            clusterIP: "",
            externalIP: "",
            ports: "",
            age: 0
          },
          svgSym: nodeIcons.service
        },
        {
          id: 11,
          name: "s11",
          type: "service",
          property: {
            type: "",
            clusterIP: "",
            externalIP: "",
            ports: "",
            age: 0
          },
          svgSym: nodeIcons.service
        },
        {
          id: 12,
          name: "n12",
          type: "node",
          property: { status: "ready", roles: "", age: 1, version: "" },
          svgSym: nodeIcons.node
        },
        {
          id: 13,
          name: "n13",
          type: "node",
          property: { status: "ok", roles: "", age: 2, version: "" },
          svgSym: nodeIcons.node
        },
        {
          id: 14,
          name: "n14",
          type: "node",
          property: { status: "", roles: "", age: 0, version: "" },
          svgSym: nodeIcons.node
        },
        {
          id: 15,
          name: "environment 1",
          type: "environment",
          property: { name: "", dataPort: "", type: "" },
          svgSym: nodeIcons.environment
        },
      ],
      links: [
        {
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
      nodeSize: 40,
      fontSize: 14,
      linkWidth: 4,
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
      force: 6000,
      moveable: false,
      focusedNode: {},
      allNodeType: [
        "masterNode",
        "node",
        "pod",
        "container",
        "service",
        "namespace",
        "environment"
      ],
      allLinkType: ["manage", "deployed-in", "provides", "contains", "supervises", "has"],
      styleList: [
        "nodesMasterNode",
        "nodesNode",
        "nodesPod",
        "nodesContainer",
        "nodesService",
        "nodesNamespace"
      ],
      linkStyleList: [
        "linkManage",
        "linkDeployed",
        "linkProvides",
        "linkContains"
      ],
      currentNode: {},
      propertyValues: [],
      propertyKeys: [],
      propsForm: {},
      newNodeType: "",
      newNodeName: '',
      oldNode: {},
      properties: {
        masterNode: {
          status: "", roles: "", age: 5, version: ""
        },
        node: {
          status: "", roles: "", age: 1, version: ""
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
        namespace: {
          status: "", age: 2
        },
        environment: {
          name: '',
          dataPort: '',
          type: ''
        }
      }
    };
  },
  methods: {
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
          // console.log(document.getElementsByClassName("nodesNamespace"))
          // 显示连接的节点
          _this.displayNodeRelation(node);
          // 如果已经有弹出框则关掉
          if (_this.notify.hasOwnProperty("message")) {
            _this.notify.close();
          }
          // 有「操作」的结点弹出框
          if (node.id === 3) {
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
          typeCard.style.right = "-20px";
          _this.oldNode = node;

          _this.addDblClickEvent()
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
          var removeList = [];
          _this.links.forEach(element => {
            if (node.id == element.sid || node.id == element.tid) {
              removeList.push(element);
            }
          });

          removeList.forEach(element => {
            _this.links.remove(element);
          });

          _this.nodes.forEach(element => {
            if (node.id == element.id) {
              _this.nodes.remove(element);
            }
          });
        }
        // 修改节点
        if (_this.radio === "5") {
          _this.displayOneNode(node);
          _this
            .$prompt("请输入新的节点名", "修改", {
              confirmButtonText: "确定",
              cancelButtonText: "取消"
            })
            .then(({ value }) => {
              if (value) {
                _this.$message({
                  type: "success",
                  message: "修改成功"
                });
                node.name = value;
              }
            })
            .catch(() => {
              _this.$message({
                type: "info",
                message: "取消输入"
              });
            });
        }
      }, 300);
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
          .then(({ value }) => {
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
        "marker-end": "url(#m-end)"
      };
      this.allLinkType.forEach((element, index, array) => {
        if (link.type == element) {
          link._linkLabelClass = this.linkStyleList[index];
          // console.log(link)
        }
      });
      return link;
    },
    ncb(node) {
      this.allNodeType.forEach((element, index, array) => {
        if (node.type == element) {
          node._cssClass = this.styleList[index];
          node._linkLabelClass = this.linkStyleList[index];
          // console.log(node)
        }
      });

      // node._cssClass = "nodesInit";
      return node;
    },
    focusNode(node) {
      this.focusedNode = node;
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
    },
    closeDisplayProps() {
      let displayProps = document.getElementsByClassName("display-property")[0];
      displayProps.style.right = "-420px";
      this.propertyValues = [];
      this.propertyKeys = [];
    },
    closeDisplayTypeCard() {
      let typeCard = document.querySelector(".display-type-selection");
      typeCard.style.right = "-360px";
      this.newNodeType = "";
      this.oldNode = {};
      this.newNodeName = ''
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
    updateTypeHandler() {
      // 根据两边的节点类型来确定关系方向
      switch (this.newNodeType) {
        case "masterNode": {
          if (this.oldNode.type === "node") {
            // masterNode -> node
            this.addNewNode(++this.id, this.oldNode.id, 'manage', 'manege', 'masterNode', this.newNodeName, this.properties.masterNode, nodeIcons.masterNode)
          } else {
            this.illegalRelation(this.newNodeType, this.oldNode.type)
          }
          break;
        }
        case "node": {
          if (this.oldNode.type === 'masterNode') {
            // node <- masterNode
            this.addNewNode(this.oldNode.id, ++this.id, 'manage', 'manage', 'node', this.properties.node, nodeIcons.node)
          }
          else if (this.oldNode.type === 'pod') {
            // node <- pod
            this.addNewNode(this.oldNode.id, ++this.id, 'deployed-in', 'deployed-in', 'node', this.properties.node, nodeIcons.node)
          }
          else if (this.oldNode.type === 'environment') {
            // node <- env
            this.addNewNode(this.oldNode.id, ++this.id, 'has', 'has', 'node', this.properties.node, nodeIcons.node)
          }
          else {
            this.illegalRelation(this.newNodeType, this.oldNode.type)
          }
          break;
        }
        case "pod": {
          if (this.oldNode.type === 'node') {
            // pod -> node
            this.addNewNode(++this.id, this.oldNode.id, 'deployed-in', 'deployed-in', 'pod', this.properties.pod, nodeIcons.pod)
          }
          else if (this.oldNode.type === 'container') {
            // pod -> container
            this.addNewNode(++this.id, this.oldNode.id, 'contains', 'contains', 'pod', this.properties.pod, nodeIcons.pod)
          }
          else if (this.oldNode.type === 'service') {
            // pod -> service
            this.addNewNode(++this.id, this.oldNode.id, 'provides', 'provides', 'pod', this.properties.pod, nodeIcons.pod)
          }
          else {
            this.illegalRelation(this.newNodeType, this.oldNode.type)
          }
          break;
        }
        case "container": {
          if (this.oldNode.type === 'pod') {
            // container <- pod
            this.addNewNode(this.oldNode.id, ++this.id, 'contains', 'contains', 'container', this.properties.container, nodeIcons.container)
          }
          else {
            this.illegalRelation(this.newNodeType, this.oldNode.type)
          }
          break;
        }
        case "service": {
          if (this.oldNode.type === 'pod') {
            // service <- pod
            this.addNewNode(this.oldNode.id, ++this.id, 'provides', 'provides', 'service', this.properties.service, nodeIcons.service)
          }
          else {
            this.illegalRelation(this.newNodeType, this.oldNode.type)
          }
          break;
        }
        case "namespace": {
          if (this.oldNode.type === 'namespace') {
            // old -> new
            this.addNewNode(this.oldNode.id, ++this.id, 'supervises', 'supervises', 'namespace', this.properties.namespace, nodeIcons.namespace)
          }
          else {
            this.illegalRelation(this.newNodeType, this.oldNode.type)
          }
          break;
        }
        case "environment": {
          if (this.oldNode.type === 'node') {
            // env -> node
            this.addNewNode(++this.id, this.oldNode.id, 'has', 'has', 'environment', this.properties.environment, nodeIcons.environment)
          }
          else {
            this.illegalRelation(this.newNodeType, this.oldNode.type)
          }
          break;
        }
      }

    },
    illegalRelation(newNodeType, oldNodeType) {
      this.$message({
        message: '类型为' + newNodeType + '的节点与类型为' + oldNodeType + '的节点没有关系',
        type: 'warning'
      })
    },
    addNewNode(sid, tid, linkName, linkType, newNodeType, property, svgSym) {
      // 新节点的 id 已经自增过了 这里不必增加
      this.nodes.push({
        id: this.id,
        name: this.newNodeName,
        type: newNodeType,
        property: property,
        svgSym: svgSym
      })
      this.links.push({
        sid: sid,
        tid: tid,
        name: linkName,
        type: linkType
      })
      this.$message({
        message: '添加成功',
        type: 'success'
      })
      this.closeDisplayTypeCard()
    },
    addDblClickEvent () {
      let onDblClick = e => {
        clearTimeout(timer);
        var e = e || window.event;
        let property = this.currentNode.property;
        this.propertyKeys = Object.keys(property);
        for (var key in property) {
          this.propertyValues.push(property[key]);
        }
        let displayProps = document.getElementsByClassName("display-property")[0];
        // console.log(displayProps)
        displayProps.style.right = "-20px";
      };
      let list = document.getElementsByTagName("circle");
      for (var i = 0; i < list.length; i++) {
        addEvent(list[i], "dblclick", onDblClick)
      }
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

        // 这两段代码会影响 namespace 节点的缩放 所以被我注释掉惹（cyl）

        // var list = document.getElementsByClassName("nodesNamespace");
        // for (var i = 0; i < list.length; i++) {
        //   let element = list[i];
        //   this.$set(
        //     element.attributes[0],
        //     "value",
        //     parseFloat(element.attributes[0].value) - 0.5
        //   );
        // }
      } else {
        this.force = this.force + 200;
        this.nodeSize = this.nodeSize + 1;
        this.fontSize = this.fontSize + 0.3;

        // var list = document.getElementsByClassName("nodesNamespace");
        // for (var i = 0; i < list.length; i++) {
        //   let element = list[i];
        //   this.$set(
        //     element.attributes[0],
        //     "value",
        //     parseFloat(element.attributes[0].value) + 0.5
        //   );
        // }
      }
      if (ev.preventDefault) {
        /*FF 和 Chrome*/
        ev.preventDefault(); // 阻止默认事件
      }
      return false;
    };

    addEvent(el, "mousewheel", onMouseWheel);
    addEvent(el, "DOMMouseScroll", onMouseWheel);

    //给节点添加双击事件，显示属性
    this.addDblClickEvent()
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

/* 被选中的图标 */
#new-graph .node-svg.selected {
  /* stroke:tomato !important; */
  stroke-width: 20px !important;
  fill: tomato !important;
}

/* 被选中的 link */
#new-graph .link.selected {
  marker-end: url(#m-end-selected);
}

/* 被选中的所有元素 */
#new-graph .selected {
  stroke:tomato !important;
  stroke-width: 4px;
  
}

#new-graph .nodesInit {
  fill: lightblue;
}

#new-graph .nodesMasterNode {
  /* fill: rgb(8, 113, 241); */
}

#new-graph .nodesNode {
  fill:darkcyan;
}

#new-graph .nodesPod {
  fill: rgb(7, 244, 188);
}

#new-graph .nodesContainer {
  fill:dimgray
}

#new-graph .nodesService {
  fill:cornflowerblue;
}

#new-graph .nodesNamespace {
  fill:darkgoldenrod
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
  /* z-index: -1; */
}

#m-end-selected {
  fill:tomato
}

.display-property {
  position: absolute;
  z-index: 10;
  top: 0;
  right: -420px;
  width: 400px;
  height: 100%;
  transition: right linear 0.2s;
  overflow: auto;
}

.display-type-selection {
  position: absolute;
  z-index: 10;
  top: 0;
  right: -360px;
  width: 340px;
  height: 250px;
  transition: right linear 0.2s;
  overflow: auto;
}

.node-svg:hover {
  stroke-width: 30px;
}

</style>