<template>
    <div id="class-graph">

    </div>
</template>

<script>
    import axios from "axios";
    import flowSvg from "@/lib/flowSvg.js";
    // import d3 from "@/lib/d3.js";
    import * as d3 from "d3";
    // import jquery from "@/lib/jquery.js";

    HTMLCollection.prototype.forEach = Array.prototype.forEach;

    export default {
        components: {},
        data() {
            return {
                sizeValue:3900,
                packageColor:"#feee7d",
                classColor:"#99f19e",
                methodColor:"#9055a2",
                isShowLinks:false  //初始不展示links（调用关系）
            };
        },
        computed: {
            options() {
                return {};
            }
        },
        created() {
            // this.nodes.push(this.initialNode);
            this.getData();
        },
        methods: {
            showOrHidFlowGraph(e) {
                console.log(e);
                // 先从 e.target 拿到数据 然后转成 svg 显示出来
                this.isFlowVisibel = !this.isFlowVisibel;
                if (this.isFlowVisibel) {
                    this.flowGraphSvg = flowSvg.svg1;
                }
            },
            getData() {
                axios
                // API GET LOCAL
                    .get("/data2.txt")
                    .then(response => {

                        var isShowLinks = false;

                        function pack(){
                            var _chart = {};

                            var _width = 1024, _height = 1024,  //1280
                                _svg,
                                _r = 800,  // 720
                                _x = d3.scaleLinear().range([0, _r]),
                                _y = d3.scaleLinear().range([0, _r]),
                                _nodes,   // original nodes info
                                _bodyG,
                                _switch;

                            // var newNodesInfo;  // a variable records the newest nodes info.

                            _chart.render = function () {
                                if (!_svg) {
                                    _svg = d3.select("div#class-graph").append("svg")
                                        .attr("height", _height)
                                        .attr("width", _width)
                                        .attr("id", "class-graph-svg");
                                }
                                if(!_switch){
                                    _switch = d3.select("div#class-graph").append("div")
                                        .attr("data-control", "BOX")
                                        .attr("id", "Box_points_switch")
                                        .append("p").text("调用关系：")
                                        .append("label")
                                        .append("input")
                                        .attr("class", "mui-switch mui-switch-anim")
                                        .attr("type", "checkbox")
                                        .attr("id", "switch");
                                }
                                renderBody(_svg);
                            };

                            function renderBody(svg) {
                                if (!_bodyG) {
                                    _bodyG = svg.append("g")
                                        .attr("class", "body")
                                        .attr("transform", function (d) {
                                            return "translate(" + (_width - _r) / 2 + "," + (_height - _r) / 2 + ")";
                                        });
                                }

                                var pack = d3.pack()
                                    .size([_r, _r])
                                    .radius(function (d) {
                                    return d.size;
                                }).padding(4);

                                const root = d3.hierarchy(_nodes)

                                var nodes = pack(root);

                                renderCircles(nodes.descendants());

                                renderLabels(nodes.descendants());

                                bindEvents(nodes.descendants());
                            }

                            function renderCircles(nodes) {
                                _bodyG.selectAll("circle").remove();

                                var circles = _bodyG.selectAll("circle")
                                    .data(nodes);

                                circles = circles.enter().append("circle");

                                circles.transition()
                                    .attr("id", function(d){
                                        if(d.data.type == "method")   //有的id重复了
                                        {
                                            return d.data.type+":"+d.parent.name+":"+d.data.name.split(":").splice(-1,1);
                                        }
                                        return d.data.type+":"+d.data.name;
                                    })
                                    .attr("class", function (d, i) {
                                        var classList = d.data.type;   // rootElement, package, class, method;
                                        if(d.data.type == "method")
                                        {
                                            classList = classList + " hidden";
                                        }
                                        return classList;
                                    })
                                    .attr("cx", function (d) {
                                        return d.x;
                                    })
                                    .attr("cy", function (d) {return d.y;})
                                    .attr("r", function (d) {

                                        // if(!d.children)
                                        // {
                                        //     return d.r * 0.65;
                                        // }
                                        // return d.r * 0.95;
                                        if(d.data.type == "package" && d.children)
                                        {
                                            for(let j=0;j<d.children.length;j++)
                                            {
                                                // console.log(d.children[j]);
                                                if(d.children[j].data.type == "class")
                                                {
                                                    return d.r;
                                                }
                                            }
                                            return d.r+(60-d.depth*10);
                                        }
                                        if(d.data.type == "class" && d.parent)
                                        {
                                            if(d.r == d.parent.r)
                                            {
                                                return d.r - 4;
                                            }
                                        }
                                        if(d.data.type == "method")
                                        {
                                            if(d.parent.children.length == 1)
                                            {
                                                return d.r*0.5;
                                            }
                                            return d.r*0.65;
                                        }
                                        return d.r;
                                    })
                                    .style("opacity", function(d){
                                        if(d.data.type == "method")
                                        {
                                            return .7;
                                        }
                                        return 1;
                                    });

                                circles.exit().transition()
                                    .attr("r", 0)
                                    .remove();

                            }

                            function renderLabels(nodes) {
                                _bodyG.selectAll("text").remove();

                                var labels = _bodyG.selectAll("text")
                                    .data(nodes);

                                labels = labels.enter().append("svg:text")
                                    .attr("dy", ".35em")
                                    .attr("text-anchor", "middle")  // start, middle, end
                                    .style("opacity", 0);

                                labels.transition()
                                    .attr("class", function (d, i) {
                                        var classList = d.data.type;   // rootElement, package, class, method;
                                        if(d.data.type == "method")
                                        {
                                            classList = classList + " hidden";
                                        }
                                        return classList;
                                    })
                                    .attr("x", function (d) {return d.x;})
                                    .attr("y", function (d) {
                                        if(d.data.type == "package" && d.children)
                                        {
                                            for(let j=0;j<d.children.length;j++)
                                            {
                                                if(d.children[j].data.type == "class")
                                                {
                                                    return d.y-d.r+4;
                                                }
                                            }
                                            return d.y-d.r-(60-d.depth*12);
                                        }
                                        return d.y;
                                    })
                                    .text(function (d)
                                    {
                                        var name = d.data.name;
                                        //处理方法名的展示
                                        if(d.data.type == "method") //方法名才包括了包名、类名、方法名和：
                                        {
                                            var array = name.split(":");
                                            return array[array.length-1];
                                        }
                                        //处理类名的展示
                                        if(d.data.type == "class") // 类名包括了包名和类名
                                        {
                                            var array = name.split(".");
                                            return array[array.length-1];
                                        }
                                        return name;
                                    })
                                    .style("opacity", function (d) {
                                        return d.r > 5 ? 1 : 0;
                                    });

                                labels.exit().remove();
                            }

                            function renderLinks(nodes) {
                                _bodyG.selectAll("line").remove();
                                if(!isShowLinks)
                                {
                                    return;
                                }

                                var links = dealLinkData(nodes);

                                var lines = _bodyG.selectAll("line").data(links);

                                lines = lines.enter().append("svg:line")
                                    .attr("x1",function(d){return d.x1;})
                                    .attr("y1",function(d){return d.y1;})
                                    .attr("x2",function(d){return d.x2;})
                                    .attr("y2",function(d){return d.y2;})
                                    .attr("stroke", "green")
                                    .attr("stroke-width",2)
                                    .attr("opacity",1)
                                    .attr("marker-end","url(#arrow)");
                            }

                            function dealLinkData(nodes) {
                                // 为svg增加 arrow marker
                                // console.log(nodes);
                                var links = [];
                                var linkInfo = nodes[0].data.links;
                                linkInfo.forEach(function(currentLink){
                                    links.push({"x1":0,"y1":0,"x2":0,"y2":0});
                                    let sourceExist = false;
                                    let targetExist = false;
                                    let source = "";
                                    let target = "";
                                    for(let i=1;i<nodes.length;i++)
                                    {
                                        if(currentLink.source == nodes[i].data.name)
                                        {
                                            if(nodes[i].parent.isChildrenVisible) {
                                                links[links.length - 1].x1 = nodes[i].x;
                                                links[links.length - 1].y1 = nodes[i].y;
                                                source = nodes[i].data.name;
                                            }
                                            else
                                            {
                                                links[links.length - 1].x1 = nodes[i].parent.x;
                                                links[links.length - 1].y1 = nodes[i].parent.y;
                                                source = nodes[i].parent.data.name;
                                            }
                                            sourceExist = true;
                                            continue;
                                        }
                                        if(currentLink.target == nodes[i].data.name)
                                        {
                                            if(nodes[i].parent.isChildrenVisible) {
                                                links[links.length - 1].x2 = nodes[i].x;
                                                links[links.length - 1].y2 = nodes[i].y;
                                                target = nodes[i].data.name;
                                            }
                                            else
                                            {
                                                links[links.length - 1].x2 = nodes[i].parent.x;
                                                links[links.length - 1].y2 = nodes[i].parent.y;
                                                target = nodes[i].parent.data.name;
                                            }
                                            targetExist = true;
                                            continue;
                                        }
                                    }
                                    if(!sourceExist || !targetExist)
                                    {
                                        links.pop();
                                    }
                                    if(source == target)
                                    {
                                        links.pop();
                                    }
                                });
                                //初始化箭头描述信息
                                var defs = d3.select('svg').append('defs');
                                //箭头
                                var arrowMarker = defs.append('marker')
                                    .attr('id','arrow')
                                    .attr('markerUnits', 'strokeWidth')
                                    .attr('markerWidth', 12)
                                    .attr('markerHeight', 12)
                                    .attr('viewBox', "0 0 12 12")
                                    .attr('refX', 6)
                                    .attr('refY', 6)
                                    .attr('orient', 'auto');
                                var arrowPath = "M2,2 L10,6 L2,10 L6,6 L2,2";
                                // var arrowPath = "M20,70 T80,100 T160,80 T200,90";
                                arrowMarker.append("path").attr("d", arrowPath).attr('fill','#937').attr("opacity",0.9);
                                return links;
                            }

                            // 为circles绑定事件
                            function bindEvents(nodes) {
                                var circles = _bodyG.selectAll("circle");
                                var labels = _bodyG.selectAll("text")._groups[0];
                                var lines = _bodyG.selectAll("line");
                                var switchButton = document.querySelector("input#switch");
                                // console.log(lines);

                                //双击circle的时候，class circle会展示method circle，method circle会展示方法流程图
                                circles.on("dblclick", function(d,i){
                                    if(d.data.type == "class" && d.children)  //控制该class下的method的显示与否，通过classList来控制
                                    {
                                        // 寻找class下的method：通过类名
                                        var circleElements = circles[0];
                                        var className = d.data.name;
                                        for(let k=0;k<circleElements.length;k++)
                                        {
                                            let circleIdInfo = circleElements[k].getAttribute("id").split(":");
                                            let cirCleType = circleIdInfo[0];
                                            if(cirCleType == "method" && circleIdInfo.length>2 &&circleIdInfo[1] == className)
                                            {
                                                if(circleElements[k].classList.contains("hidden"))
                                                {
                                                    circleElements[k].classList.remove("hidden");
                                                    labels[k].classList.remove("hidden");
                                                    labels[i].setAttribute("y",d.y-d.r+10);
                                                }
                                                else
                                                {
                                                    circleElements[k].classList.add("hidden");
                                                    labels[k].classList.add("hidden");
                                                    labels[i].setAttribute("y",d.y);
                                                }
                                            }
                                        }
                                        nodes[i].isChildrenVisible = !nodes[i].isChildrenVisible;
                                        renderLinks(nodes);
                                    }
                                    // console.log(nodes);
                                    // console.log(isVisible("works.weave.socks.cart.cart.CartContentsResource:lambda$add$1", nodes));
                                    return false;
                                });

                                // 鼠标悬浮在circle上的时候，label放大, circle和label的index是对应起来的
                                circles.on("mouseenter", function(d,i){
                                    // 相应的label变大
                                    console.log(labels[i])
                                    if(!labels[i].classList.contains("focus"))
                                    {
                                        labels[i].classList.add("focus");
                                    }

                                    return false;
                                });

                                // 鼠标移出circle的时候，label复原
                                circles.on("mouseout", function(d, i){
                                    if(labels[i].classList.contains("focus"))
                                    {
                                        labels[i].classList.remove("focus");
                                    }

                                    return false;
                                });

                                //鼠标悬浮在line上的时候，line的颜色改变，高亮
                                // lines.on("mouseenter", function(d, i){
                                //     console.log(this);
                                // })
                                switchButton.addEventListener("change", function(d){
                                    // console.log(d);
                                    isShowLinks = !isShowLinks;
                                    // console.log(this.isShowLinks);
                                    renderLinks(nodes);
                                })
                            }

                            _chart.width = function (w) {
                                if (!arguments.length) return _width;
                                _width = w;
                                return _chart;
                            };

                            _chart.height = function (h) {
                                if (!arguments.length) return _height;
                                _height = h;
                                return _chart;
                            };

                            _chart.r = function (r) {
                                if (!arguments.length) return _r;
                                _r = r;
                                return _chart;
                            };

                            _chart.nodes = function (n) {
                                if (!arguments.length) return _nodes;
                                _nodes = n;
                                return _chart;
                            };

                            return _chart;
                        }

                        // console.log(response);
                        var data=response.data;
                         console.log(data);
                        var nodesInfo = data.nodes;
                        var linksInfo = data.links;
                        var classesInfo = data.classes;
                        var classMethodMap = data.classMethodMap;
                        var nodes = {
                            "name":"rootElement",
                            "type":"rootElement",
                            "children":[],
                            "links":[]
                        };
                        //处理包结构
                        // console.log(classesInfo);
                        var packageInfo = new Array();  //记录了所有的包+其中包含的类结构
                        var curCA = nodes.children;
                        for(let i=0;i<classesInfo.length;i++)
                        {
                            // packageInfo.push(new Array());
                            var packageStructure = classesInfo[i].split("."); //最后一个元素不是package，是class名，所以这里不考虑
                            // packageInfo.push(packageStructure.slice(0, packageStructure.length-1));
                            packageInfo.push(packageStructure);
                        }
                        // console.log(packageInfo);
                        for(let i=0;i<packageInfo.length;i++)  //对每个类的包结构进行分析
                        {
                            curCA = nodes.children;  // curCA: current Children Array
                            // console.log(packageInfo[i]);
                            //在当前层次的children中寻找是否有对应的package名，若有，继续分析packageInfo的下一组，若无，则在相应位置加入children信息
                            for(let j=0;j<packageInfo[i].length;j++)
                            {
                                let isPackageFound = false;
                                for(let k=0;k<curCA.length;k++)
                                {
                                    if(curCA[k].name == packageInfo[i][j]) // 找到
                                    {
                                        curCA = curCA[k].children;
                                        isPackageFound = true;
                                        break;
                                    }
                                }
                                if(!isPackageFound)
                                {
                                    curCA.push({
                                        "name":(j<packageInfo[i].length-1?packageInfo[i][j]:packageInfo[i].join(".")),
                                        "children":[],
                                        "type":(j<packageInfo[i].length-1?"package":"class")
                                    });
                                    if(curCA[curCA.length-1].type == "class")
                                    {
                                        curCA[curCA.length-1].isChildrenVisible = false; //设置class的method最初不可见
                                    }
                                    curCA = curCA[curCA.length-1].children;
                                }
                            }
                            // console.log(curCA);
                            //将方法加入数据结构中
                            //注：类名和方法名都包含了包的名
                            let curClassName = packageInfo[i].join(".");
                            for(var classKey in classMethodMap)
                            {
                                if(classKey == curClassName)
                                {
                                    for(let i=0;i<classMethodMap[classKey].length;i++) {
                                        curCA.push({
                                            "name": classKey+":"+classMethodMap[classKey][i],
                                            // "name":classMethodMap[classKey][i],
                                            "type":"method",
                                            "size":this.sizeValue
                                        })
                                    }
                                }
                            }
                        }
                        // 处理links数据
                        for(let i=0;i<linksInfo.length;i++)
                        {
                            // 此处links中的source和target的name都是method名，method名包括了对应的包名和类名
                            nodes.links.push({"source":linksInfo[i].source.name,"target":linksInfo[i].target.name});
                        }
                        var chart=pack();
                        // console.log(chart);
                        chart.nodes(nodes).render();
                    })
                    .catch(function (error) {
                        console.log(error);
                    });
            }
        }
    };
</script>

<style>
    body{

    }
    svg#class-graph-svg
    {
        float:right;
    }

    button.showLinksButton{
        height: 30px;
        width:100px;
        float:right;
        margin-top:10%;
        margin-right:3%;
        cursor: pointer;
        background-color: #9DC8C9;
        -webkit-border-radius: 10px;
        -moz-border-radius: 10px;
        border-radius: 10px;
    }

    text {
        font-size: 11px;
        pointer-events: none;
        /*width:20em;*/
    }

    text.rootElement{
        display: none;
    }

    text.package,text.class {
        fill: brown;
        font-size: 13px;
    }

    text.class{
        fill: steelblue;
        font-size: 12px;
    }

    text.method {
        fill: #08182b;
        font-size:10px;
    }

    text.hidden
    {
        display:none;
    }

    text.focus
    {
        font-size: 15px;
        fill: red;
    }

    circle {
        /*fill: #ccc;*/
        stroke: #333333;
        pointer-events: all;
    }

    circle.rootElement
    {
        display:none;
    }

    circle:hover
    {
        cursor: pointer;
        stroke-width:2px;
        stroke:red;
    }

    circle.hidden
    {
        display:none;
    }

    circle.package
    {
        fill:#feee7d;
    }

    circle.class
    {
        fill:#99f19e;
    }

    circle.method
    {
        fill:#9055a2;
    }

    svg#class-graph-svg line
    {
        cursor: pointer;
    }

    svg#class-graph-svg line:hover
    {
        stroke: red;
        stroke-width: 4px;
    }

    /*switch开关*/
    div#Box_points_switch{
        float:left;
        margin-top:10%;
        margin-left:5%;
        vertical-align: top;
    }
    div#Box_points_switch p{
        font-family: "Songti SC", sans-serif;
        font-size: 16px;
        float:left;
        color: cadetblue;
        font-weight: bold;
        margin-top:5%;
    }

    .mui-switch {
        width: 5em;
        height: 3.2em;
        position: relative;
        border: 1px solid #dfdfdf;
        background-color: #fdfdfd;
        box-shadow: #dfdfdf 0 0 0 0 inset;
        border-radius: 3em;
        background-clip: content-box;
        display: inline-block;
        -webkit-appearance: none;
        user-select: none;
        outline: none;
    }
    .mui-switch:before {
        content: '';
        width: 3em;
        height: 3em;
        position: absolute;
        top: 0px;
        left: 0;
        border-radius: 3em;
        background-color: #fff;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.4);
    }
    .mui-switch:checked:before {
        left: 2em;
    }
    .mui-switch.mui-switch-anim {
        transition: border cubic-bezier(0, 0, 0, 1) 0.4s, box-shadow cubic-bezier(0, 0, 0, 1) 0.4s;
    }
    .mui-switch.mui-switch-anim:before {
        transition: left 0.3s;
    }
    .mui-switch.mui-switch-anim:checked {
        box-shadow: #64bd63 0 0 0 16px inset;
        background-color: #64bd63;
        transition: border ease 0.4s, box-shadow ease 0.4s, background-color ease 1.2s;
    }
    .mui-switch.mui-switch-anim:checked:before {
        transition: left 0.3s;
    }

</style>