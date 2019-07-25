# SoftwareTesting
## 后端运行方式
1. 使用IDEA打开BackEnd19
2. 右击pom.xml，选择Maven项目，点击import，进行maven导入依赖
3. 打开Maven Project，点击Plugins/jetty/jetty:run
4. 随后项目会部署在0.0.0.0:8088上，接口的url为0.0.0.0:8088/bbs/api/接口名称

## 前端运行方式
1. 如果您没有安装过node.js，请先安装[node.js](https://nodejs.org/en/)
   - npm工具会与node.js一同被安装到您的电脑上
2. 控制台（终端）打开前端根目录
   - ` cd ~/Operation_KnowledgeGraph/frontend19v `
   - p.s. 项目18v原型界面在`/frontend18v`目录下。
3. 运行`npm install`指令，安装依赖
4. 运行`npm run serve`指令，项目前端自动部署到`localhost:8080`
5. 打开浏览器，键入`http://localhost:8080`检视项目内容
   - p.s. 需要先运行后端才可以在前端执行相关功能

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



## 关于Fuseki

服务器：部署在了服务器的docker上，端口为3030，访问地址为http://10.60.38.173:3030。

​	帐号为：admin

​	密码为：D0rlghQl5IAgYOm



## MongoDB数据库部署情况

服务器：部署在了服务器的docker上，端口为27020，访问地址为http://10.60.38.173:27020。

​	数据库名为knowledgegraph。

