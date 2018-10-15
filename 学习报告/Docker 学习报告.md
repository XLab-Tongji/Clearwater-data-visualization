# Docker 学习报告

> 2018 / 10 / 12   陈雨蕾
>
> 2018 / 10 / 15   梁琛

[TOC]

## 一、Docker 简介

### 1. 什么是 Docker

**Docker 属于 Linux 容器的一种封装，提供简单易用的容器使用接口。**它是目前最流行的 Linux 容器解决方案。

Docker 将应用程序与该程序的依赖，打包在一个文件里面。运行这个文件，就会生成一个虚拟容器。程序在这个虚拟容器里运行，就好像在真实的物理机上运行一样。有了 Docker，就不用担心环境问题。

总体来说，Docker 的接口相当简单，用户可以方便地创建和使用容器，把自己的应用放入容器。容器还可以进行版本管理、复制、分享、修改，就像管理普通的代码一样。

### 2. Docker 的用途

Docker 的主要用途，目前有三大类。

- **提供一次性的环境。**比如，本地测试他人的软件、持续集成的时候提供单元测试和构建的环境。

- **提供弹性的云服务。**因为 Docker 容器可以随开随关，很适合动态扩容和缩容。

- **组建微服务架构。**通过多个容器，一台机器可以跑多个服务，因此在本机就可以模拟出微服务架构。

### 3. Docker 的三要素

Docker 包括三个基本要素：

- 镜像（`Image`）
- 容器（`Container`）
- 仓库（`Repository`）

## 二、MacOS 安装 Docker

### 1. 安装

**使用 Homebrew 安装**

```bash
$ brew cask install docker
```

**手动下载安装**

点击以下链接下载 [Stable](https://download.docker.com/mac/stable/Docker.dmg) 或 [Edge](https://download.docker.com/mac/edge/Docker.dmg) 版本的 Docker for Mac。

![img](https://github.com/XLab-Tongji/Operation_KnowledgeGraph/blob/master/学习报告/img/docker_app.png?raw=true)

**运行**

运行之后，会在右上角菜单栏看到多了一个鲸鱼图标，这个图标表明了 Docker 的运行状态。

![img](https://github.com/XLab-Tongji/Operation_KnowledgeGraph/blob/master/学习报告/img/docker_top.png?raw=true)

### 2. 配置阿里云镜像加速器

国内从 Docker Hub 拉取镜像会比较慢，此时可以配置镜像加速器。以阿里云镜像加速器为例：

**步骤一：获取加速器地址：**进入[阿里云开发者平台官网](http://dev.aliyun.com/search.html)，注册一个账户并登陆，进入管理中心，在左侧导航中依次点击容器镜像服务-镜像加速器，复制加速器地址。如下图。

![docker_alicloud](https://github.com/XLab-Tongji/Operation_KnowledgeGraph/blob/master/学习报告/img/docker_alicloud.png?raw=true)

**步骤二： 配置镜像加速器：**右键点击桌面顶栏的 docker 图标，选择 Preferences ，在 Daemon 标签下的 Registry mirrors 列表中将加速器地址加到"registry-mirrors"的数组里，点击 Apply & Restart按钮，等待Docker重启并应用配置的镜像加速器。

### 补充： Ubuntu 系统 Docker安装 与问题总结

> 2018 / 10 / 15   梁琛

具体的安装步骤在此省略，详情见官网文档：https://docs.docker.com/install/linux/docker-ce/ubuntu/#install-docker-ce

#### 常见问题1：在Ubuntu x64 16.04下安装docker-ce出现的错误

```
Reading package lists... Done
Building dependency tree       
Reading state information... Done
Package docker-ce is not available, but is referred to by another package.
This may mean that the package is missing, has been obsoleted, or
is only available from another source

E: Package 'docker-ce' has no installation candidate
```

问题原因与解决方法：

没找到合适的更新源，手动加入源

```
cd /etc/apt/sources.list.d
sudo vi docker.list
```

加入以下信息：（键入`i`进入insert模式）

```
deb https://download.docker.com/linux/ubuntu zesty edge
```

更新`apt-get`，并进行安装

```
sudo apt update
sudo apt install docker-ce
```

参考：

> [http://www.cnblogs.com/zhaiji...](http://www.cnblogs.com/zhaijiahui/p/6932820.html)

#### 常见问题2：sudo docker 频繁使用问题的解决

Docker 需要用户具有 sudo 权限，为了避免每次命令都输入`sudo docker cmd`，可以把用户加入 Docker 用户组。

1. 创建 `docker` 用户组

   ```
   $ sudo groupadd docker
   ```

2. 把当前用户加入 `docker` 用户组

   ```
   $ sudo usermod -aG docker $USER
   ```

3. 退出登录，并重新登入即可。

   1. 如果使用虚拟机，可能需要重新启动Ubuntu。亲测 parallel desktop 不需要。

## 三、Docker 常用命令

### 1. 镜像命令

#### 列出镜像 `docker images [OPTIONS]` 

列出本地镜像。OPTIONS：

- `-a` ：列出本地所有的镜像（含中间映像层）
- `-q` ：只显示镜像ID
- `--digests` ：显示镜像的摘要信息
- `--no-trunc` ：显示完整的镜像信息

#### 搜索镜像 `docker search [OPTIONS] 镜像名`

从阿里云镜像库中搜索镜像。OPTIONS：

- `--no-trunc` ：显示完整的镜像描述
- `-s` ：列出收藏数不小于指定值的镜像
- `--automated` ：只列出automated build类型的镜像

#### 拉取镜像 `docker pull 镜像名[:TAG]`

从远程仓库（阿里云或dockerhub）中拉取镜像。

#### 删除镜像`docker rmi 镜像名/ID`

- 删除单个：`docker rmi -f 镜像ID`

- 删除多个：`docker rmi -f 镜像名1:TAG 镜像名2:TAG`

- 删除全部：`docker rmi -f $(docker images -qa)`

### 2. 容器命令

#### 新建并启动容器 `docker run [OPTION] IMAGE [COMMAND] [ARG...]`

OPTION：

- `--namne="容器新名字"` ：为容器指定一个名称

- `-d` ：后台运行容器，并返回容器ID，也即启动守护式容器
- `-i` ：以交互模式运行容器，通常与 -t 同时使用
- `-t` ：为容器重新分配一个伪输入终端，通常与 -i 同时使用
- `-P` ：随机端口映射
- `-p` ：指定端口映射，有一下四种格式
  - ip:hostPort:containerPort
  - ip::containerPort
  - hostPort:containerPort
  - containerPort

#### 列出容器 `docker ps [OPTION]`

列出当前所有**正在运行**的容器。OPTION：

- `-a` ：列出当前所有正在运行和历史上运行过的容器
- `-l` ：显示最近创建的容器
- `-n` ：显示最近n个创建的容器
- `-q` ：静默模式，只显示容器编号
- `--no-trunc` ：不截断输出

#### 退出容器

两种退出方式：

- `exit` ：容器停止退出
- ctrl + P + Q：容器不停止退出

#### 删除已停止的容器 `docker rm 容器ID`

一次性删除多个容器：

- `docker rm -f $(docker ps -a -q)`
- `docker ps -a -q | xargs docker rm`

#### 查看容器日志 `docker logs -f -t tail 容器ID`

- `-t`：加入时间戳
- `-f`：跟随最新的日志打印
- `--tail`：显示最后多少条

#### 进入正在运行的容器并以命令行交互 

- `docker exec -it 容器ID bashShell`：在容器中打开新的终端，并且可以启动新的进程
- `docker attach 容器ID`：直接进入容器启动命令的终端，不会启动新的进程

#### 其他命令

- 启动容器 `docker start 容器ID/容器名`

- 重启容器 `docker restart 容器ID/容器名`

- 停止容器 `docker stop 容器ID/容器名`

- 强制停止容器 `docker kill 容器ID/容器名`
- 查看容器内运行的进程 `docker top 容器ID`
- 拷贝文件至主机 `docker cp 容器ID:容器内路径 目的主机路径`
- 查看容器内部细节 `docker inspect 容器ID`

## 四、Docker 镜像

### 1. Docker 镜像是什么

Docker 镜像是一个特殊的文件系统，除了提供容器运行时所需的程序、库、资源、配置等文件外，还包含了一些为运行时准备的一些配置参数（如匿名卷、环境变量、用户等）。镜像不包含任何动态数据，其内容在构建之后也不会被改变。

因为镜像包含操作系统完整的 `root` 文件系统，其体积往往是庞大的，因此在 Docker 设计时，就充分利用 [Union FS](https://en.wikipedia.org/wiki/Union_mount) 的技术，将其设计为分层存储的架构。

镜像构建时，会一层层构建，前一层是后一层的基础。每一层构建完就不会再发生改变，后一层上的任何改变只发生在自己这一层。

### 2. Docker 镜像的 Commit 操作

docker commit 提交容器副本使之成为一个新的镜像。格式：

`docker commit -m=“提交的描述信息” -a=“作者” 容器ID 要创建的目标镜像名[:TAG]`

## 五、Docker 数据卷

### 1. 什么是数据卷

`数据卷` 是一个可供一个或多个容器使用的特殊目录，它绕过 UFS，可以提供很多有用的特性：

- `数据卷` 可以在容器之间共享和重用
- 对 `数据卷` 的修改会立马生效
- 对 `数据卷` 的更新，不会影响镜像
- `数据卷` 默认会一直存在，即使容器被删除

> 注意：`数据卷` 的使用，类似于 Linux 下对目录或文件进行 mount，镜像中的被指定为挂载点的目录中的文件会隐藏掉，能显示看的是挂载的 `数据卷`。

### 2. 如何添加数据卷

#### 直接命令添加

```CQL
$ docker run -it -v /宿主机绝对路径目录:/容器内目录 镜像名
```

#### Dockerfile 添加

使用`VOLUME`命令来给镜像添加一个或多个数据卷，例如：

```dockerfile
FROM centos
VOLUME ["/dataVolumeContainer1", "/dataVolumeContainer2"]
CMD echo "finished"
CMD /bin/bash
```

### 3. 数据卷容器

**定义**

命名的容器挂载数据卷，其他容器通过挂载这个（父容器）实现数据共享，挂载数据卷的容器称之为数据卷容器。

**命令**

```
$ docker run -it --name 自定义容器名 --volumes-from 数据卷容器名 镜像名
```


## 六、Dockerfile 解析

### 1. Dockerfile 是什么

Dockerfile 是用来构建 Docker 镜像的构建文件，是由一系列命令和参数构成的脚本。

构建镜像的三步骤：编写 Dockerfile 文件；`docker build`；`docker run`。

### 2. Dockerfile 构建过程

#### 基础知识

- 每条保留字命令都必须为大写字母且后面要跟随至少一个参数
- 指令按照从上到下，顺序执行
- \#表示注释
- 每条指令都会创建一个新的镜像层，并对镜像进行提交

#### 执行 Dockerfile 的大致流程

1. docker 从基础镜像运行一个容器
2. 执行一条命令并对容器作出修改
3. 执行类似 docker commit 的操作提交一个新的镜像层
4. docker 再基于刚提交的镜像运行一个新容器
5. 执行 dockerfile 中的下一条指令直到所有指令都执行完成

### 3. Dockerfile 指令

- `FROM`：基础镜像，当前新镜像是基于哪个镜像的
- `MAINTAINER`：镜像维护者的姓名和邮箱地址
- `RUN`：容器构建时需要运行的命令
- `EXPOSE`：当前容器对外暴露出的端口
- `WORKDIR`：指定在创建容器后，终端默认登录进来的工作目录，一个落脚点
- `ENV`：在构建镜像的过程中设置环境变量
- `ADD`：将宿主机目录下的文件拷贝进镜像且ADD命令会自动处理URL和解压tar压缩包
- `COPY`：类似ADD，拷贝文件和目录到镜像中。将从构建上下文目录中<源路径>的文件/目录复制到新的一层的镜像内的<目标路径>位置
- `VOLUME`：容器数据卷，用于数据保存和持久化工作
- `CMD`：指定一个容器启动时要运行的命令。可有多个CMD命令，但只有最后一个生效，且会被docker run之后的参数替换
- `ENTRYPOINT`：指定一个容器启动时要运行的命令。docker run 之后的参数作为追加命令
- `ONBUILD`：当构建一个被继承的 Dockerfile 时运行命令，父镜像在被子继承后父镜像的 onbuild 被触发

## 七、本地镜像发布到阿里云

![DockerArchetecture](https://github.com/XLab-Tongji/Operation_KnowledgeGraph/blob/master/学习报告/img/docker_allSteps.png?raw=true)

如上图所示发布就是Images->docker registry的过程，**步骤：**

1. 在阿里云开发者平台创建镜像仓库

2. 将本地镜像推送到 registry 。这一步在仓库的基本信息-操作指南中会有详细说明，一般是：

   ```
   $ docker login --username= registry.cn-hangzhou.aliyuncs.com
   $ docker tag [ImageId] 仓库地址:[镜像版本号]
   $ docker push 仓库地址:[镜像版本号]
   ```