# SoftwareTesting
## 后端运行方式
使用IDEA打开后，点击pom进行maven导入依赖，然后打开Maven Project，点击jPlugins/jetty/jetty:run，随后项目会部署在localhost://8088上。接口的url为localhost:8088/bbs/api/接口名称

## 前端运行方式
控制台打开前端目录，运行npm install指令，安装完依赖后，运行npm run dev指令，随后项目前端将会部署到localhost://8010上，注意要先运行后端才可以在前端执行相关功能

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
match (from:{id:line.From_id}),(to:{id:line.To_id})
merge (from)-[r:rel{type:line.Type}]->(to)
```
关于其他的Cypher操作可以自己去stack overflow和CSDN上搜索阅读，同时建议理解了一部分操作后再运行我这边的代码，这样即使出现问题也会大概知道原因。

If you still have questions, send email to dushuyang@126.com
