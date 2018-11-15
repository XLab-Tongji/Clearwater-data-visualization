# SoftwareTesting
## 后端运行方式
使用IDEA打开后，点击pom进行maven导入依赖，然后打开Maven Project，点击jPlugins/jetty/jetty:run，随后项目会部署在localhost://8088上。接口的url为localhost:8088/bbs/api/接口名称

## 前端运行方式
1. 如果您没有安装过node.js，请先安装[node.js](https://nodejs.org/en/)
   * npm工具会与node.js一同被安装到您的电脑上
2. 控制台（终端）打开前端根目录
   * ` cd ~/Operation_KnowledgeGraph/Front End `
   * ~ 为项目所在目录
   * P.S. 项目18v新原型界面在`frontend18v`目录下。
3. 运行`npm install`指令，安装依赖
4. 运行`npm run dev`指令，项目前端自动部署到`localhost:8010`
5. 打开浏览器，键入`http://localhost:8010`检视项目内容
   * p.s. 需要先运行后端才可以在前端执行相关功能

### 前端Docker镜像18v 使用方法

1. 如果您没有安装过Docker，请先安装Docker。（您可以参考学习报告目录下的Docker学习报告）
2. 拉取远端仓库

```
docker pull leonnnop/operation_knowledgegraph_tj18:frontendV0.2.2
```

3. 运行Docker 镜像

```
docker run -d -p 8080:8080 leonnnop/operation_knowledgegraph_tj18:frontendV0.2.2
```



## 关于Neo4j
后端所连接的Neo4j数据库服务器因为需要经费维持现在已经关闭，并且还有其他用途。所以无法访问到服务45.77.214.60:7474是正常现象，如果需要重新跑整个项目可以在本机或者自己的其他服务器上装一个Neo4j。然后利用在Clearwater终极版文件夹中我已经整理好的数据，利用loadcsv导入进去就可以了，当然在后端的Neo4j driver相应模块的代码里也要将连接的服务器url、用户名和密码变成你们自己的。下面是相关的Cypher语句，先确保你已经上传了csv文件到你的服务器文件目录下。
导入节点的操作：适用于我这里除了Edge.csv之外的所有csv文件
注意资源名称我预设的有4个：ResourceNode, Failure_Node, ADR_Node, TestCase_Node，你们可以直接用也可以更改名字，或者添加新的，但是要注意在Spring MVC的controller里也要更改相应的代码。

```
USING PERIODIC COMMIT 10   
LOAD CSV FROM "file:///node.csv（这里替换成你要导入的csv名称）" AS line  
create (a:Node(Node可以替换成你要创建的资源名称){id:line[0], name:line[1], type:line[2], layer:line[3], performance:line[4]})  
```
导入关系的操作：适用于这里的Edge.csv
```
LOAD CSV WITH HEADERS FROM "file:///Edge.csv" AS line
match (from{id:line.From_id}),(to{id:line.To_id})
merge (from)-[r:rel{type:line.Type}]->(to)
```
关于其他的Cypher操作可以自己去stack overflow和CSDN上搜索阅读，同时建议理解了一部分操作后再运行我这边的代码，这样即使出现问题也会大概知道原因。

If you still have questions, send email to dushuyang@126.com
