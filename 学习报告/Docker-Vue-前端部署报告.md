# Docker-Vue 前端部署报告

> 2018/10/16 梁琛

[TOC]

## 开发环境

### 前端开发

* macOS X 10.13.6
* Vue-cli 3.0 (Webpack 4)

### Docker镜像制作

* Ubuntu 16.04

## 详细步骤

### 1 打包vue项目

1. 在vue项目根目录打包项目

```
npm run build
```

![image-20181016233945023](img/1.png)

* 如果打包成功，会在根目录下生成一个dist文件夹。

### 2 制作镜像

1. 将dist文件夹下的全部内容拷贝到任一目录 **(Optional)**
2. 在同目录下创建`Dockerfile`

```
FROM nginx:1.13.7
# 使用Nginx

MAINTAINER LeonLiang <leonnnop@icloud.com> 

RUN rm /etc/nginx/conf.d/default.conf 
# 删除nginx 默认配置

ADD default.conf /etc/nginx/conf.d/ 
# 添加我们自己的配置 default.conf 在下面

COPY dist/  /usr/share/nginx/html/  
# 把刚才生成dist文件夹下的文件copy到nginx下面去

RUN /bin/bash -c 'echo init ok!!!'
```

* 我使用ngix镜像作为基础镜像

3. 在同目录下创建`default.conf`

```
server {
    # 项目中定义的端口号
        listen       8080;
        server_name  localhost;
    
        #charset koi8-r;
        #access_log  /var/log/nginx/log/host.access.log  main;
    
        location / {
            root   /usr/share/nginx/html;
            index  index.html index.htm;
        }
    
        location ^~/bbs/ {        
            # rewrite ^/api/(.*)$ /$1 break;        
            proxy_set_header   Host             $host;        
            proxy_set_header   x-forwarded-for  $remote_addr;        
            proxy_set_header   X-Real-IP        $remote_addr;        
            proxy_pass http://10.0.1.37:8088;
        }
    
    
        #error_page  404              /404.html;
    
        # redirect server error pages to the static page /50x.html
        #
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }
}
```

* default.conf 文件内容错误不会导致镜像制作失败，但会导致制作出的镜像运行时闪退。如果出现了闪退问题，基本就是编辑错误。

* 

* ```
         location ^~/bbs/ {        
             # rewrite ^/api/(.*)$ /$1 break;        
             proxy_set_header   Host             $host;        
             proxy_set_header   x-forwarded-for  $remote_addr;        
             proxy_set_header   X-Real-IP        $remote_addr;        
             proxy_pass http://10.0.1.37:8088;
         }
     ```
  ```

  * 这一段内容用于配置跨域代理路由。注意：只配置webpack的路由的话，发布后是不会再有路由的，因为是不同的服务器。需要重新配置nginx。

### 3 发布镜像

1. 注册一个Docker Hub账号

2. 登录到自己的Docker Hub账号

   `sudo docker login -u <用户名> -p <密码> -e <Email>` 

   或不带参数，Docker命令行客户端会提示你输入用户名、密码和Email。

3. 将容器保存到镜像中，输入命令：

  ```
   sudo docker commit containerID <用户名>/registry:[TAG]
   ```

   命令返回一串文本，这就是新提交的镜像ID。通过`docker images`命令，你可以看到创建了一个新的image。

4. 发布到云端，输入命令：

   ```
   sudo docker push <用户名>/registry:[TAG]
   ```

   上传成功后，会输出：`Image successfully pushed`。

### 4 使用镜像

   ```
docker pull <用户名>/registry:[TAG]
```

## 常见问题

### npm run build 失败 Error: Cannot find module '../package.json' 

* 更改项目目录后，需要删除 node_modules 文件夹下的全部内容，并重新 npm install

### 生成的镜像运行时闪退

* 能够生成就是Dockerfile没毛病，多半是修改的配置文件有错误。


```