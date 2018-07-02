# SoftwareTesting
## 后端运行方式
使用IDEA打开后，点击pom进行maven导入依赖，然后打开Maven Project，点击jPlugins/jetty/jetty:run，随后项目会部署在localhost://8088上。接口的url为localhost:8088/bbs/api/接口名称

## 前端运行方式
控制台打开前端目录，运行npm install指令，安装完依赖后，运行npm run dev指令，随后项目前端将会部署到localhost://8010上，注意要先运行后端才可以在前端执行相关功能