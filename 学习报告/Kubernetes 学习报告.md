# Kubernetes 学习报告

> 2018/10/15 陈雨蕾
>
> 2018/10/17 梁琛 

[TOC]

## 应用场景

* 容器自动伸缩，修复，检查
* 多容器多机器自动负载均衡

## K8s架构和核心组件

**架构图**

![image](https://github.com/XLab-Tongji/Operation_KnowledgeGraph/blob/master/学习报告/img/k8s_arche.png?raw=true)

**核心组件**

- node：Kubernetes集群中相对于Master而言的工作主机，Node可以是一台物理主机，也可以是一台虚拟机（VM）。在每个Node上运行用于启动和管理Pid的服务Kubelet，并能够被Master管理。
- **pod**：Kubernetes的最基本操作单元，包含一个活**多个**紧密相关的容器，类似于豌豆荚的概念。一个Pod可以被一个容器化的环境看作应用层的“逻辑宿主机”（Logical Host）。一个Pod中的多个容器应用通常是紧耦合的。Pod在Node上被创建、启动或者销毁。
  - 作为容器的抽象，故并不限制于Docker。Docker凉了，他也不凉。为Service Mesh做准备。
- etcd：保存了整个集群的状态；
- apiserver：提供了资源操作的唯一入口，并提供认证、授权、访问控制、API 注册和发现等机制；
- controller manager：负责维护集群的状态，比如故障检测、自动扩展、滚动更新等；
  - **Deployment**: 最常用的Controller对象。
- scheduler：负责资源的调度，按照预定的调度策略将 Pod 调度到相应的机器上；
- kubelet：负责维护容器的生命周期，同时也负责 Volume（CVI）和网络（CNI）的管理；
  - **所以kubelet进程的日志可以反应错误的情况与原因。**
- Container runtime：负责镜像管理以及 Pod 和容器的真正运行（CRI）；
- kube-proxy：负责为 Service 提供 cluster 内部的服务发现和负载均衡
- **Service:**  容器在不同机器间切换，IP会变。Service可以为不同但是提供相同功能容器提供统一的入口。
  - **Ingress**：相当于一个分发器，分发给不同的Service，是集群的入口
- **P.S.:**在上面的架构图中没有涉及到网络（pods间通信），网络采用第三方，所以是要自己配置的。

## K8s对象模型

![image-object](https://github.com/XLab-Tongji/Operation_KnowledgeGraph/blob/master/学习报告/img/k8s_object.png?raw=true)

**Namespace**

- 是什么：资源隔离的一种手段；不同NS中的资源不能相互访问；多租户的一种常见解决方案。
- 拥有：名称
- 关联：Resource

**Label**

- 是什么：一个Key-Value值对；可以任意定义；可以贴在Resource对象上；用来过滤和选择某种资源。
- 拥有：Key、Value
- 关联：Resource、Label Selector

**Service**

- 是什么：微服务；容器方式隔离；TCP服务；通常无状态；可以部署多个实例同时服务；属于内部的“概念”，默认外部无法访问；可以滚动升级。
- 拥有：一个唯一的名字；一个虚拟访问的地址；IP地址(ClusterIP)+Port；一个外部系统访问的映射端口NodePort；对应后端分布于不同Node一组服务容器进程。
- 关联：多个相同Label的Pod

**Replication Controller**

- 是什么：Pod副本控制器；限定某种Pod的当前实例的个数；属于服务集群的控制范畴；服务的滚动升级就是靠它来实现。
- 拥有：一个标签选择器，选择目标Pod；一个数字，表明目标Pod的期望实例数；一个Pod模板，用来创建Pod实例。
- 关联：多个相同Label的Pod

**Volumes**

- 是什么：Pod上的存储卷；能被Pod内的多个容器使用。
- 拥有：名字；存储空间。
- 关联：Pod

## MacOS 安装 Minikube

直接使用命令：

```bash
$ brew cask install minikube
```

可能会遇到的问题：

1. 进度条没到100%就失败了。解决方法：多执行几次`brew cask install minikube`
2. 进度条到了100%后出现错误kube-proxy configmap update: timed out (unknown root cause) 。解决方法：参考https://github.com/kubernetes/minikube/issues/2755中**dciccale**的回答。

## 部署容器

### 使用 kubectl

启动minukube

```shell
$ minikube start
```

run一个deployment

```shell
$ kubectl run deployment名称 --image=镜像名 --replicas=1
```

例如：`kubectl run http --image=katacoda/docker-http-server:latest --replicas=1`

可以使用以下命令查看deployments的状态

```shell
$ kubectl get deployments
```

可以使用以下命令查看deployment的描述

```shell
$ kubectl describe deployment deployment名称
```

创建一个服务来暴露Pods的端口。以下命令expose the container port *80* on the host *8000* binding to the *external-ip* of the host.

```shell
$ kubectl expose deployment deployment名称 --external-ip="172.17.0.127" --port=8000 --target-port=80
```

接下来就可以ping一下主机

```shell
$ curl http://172.17.0.127:8000
```

以下命令可以用来扩充容器

```shell
$ kubectl scale --replicas=数量 deployment deployment名称
```

列出扩充的pods

```shell
$ kubectl get pods
```

### 使用 yaml

#### Create Deployment

实例.yaml

```yaml
apiVersion: extensions/v1beta1 #所使用的 Kubernetes API 版本
kind: Deployment #指定创建资源的角色/类型
metadata: #资源的元数据/属性
  name: webapp1 #资源的名字，在同一个namespace中必须唯一
spec: #specification of the resource content 指定该资源的内容
  replicas: 1 #指定ReplicationController中pod的个数
  template:
    metadata:
      labels:
        app: webapp1
    spec:
      containers: #指定资源中的容器
      - name: webapp1 #容器的名字
        image: katacoda/docker-http-server:latest #容器使用的镜像地址
        ports:
        - containerPort: 80 
```
命令

```shell
$ kubectl create -f deployment.yaml
$ kubectl get deployment
$ kubectl describe deployment webapp1
```

#### Create Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: webapp1-svc
  labels:
    app: webapp1
spec:
  type: NodePort #The Service makes the application available via a NodePort.
  ports:
  - port: 80
    nodePort: 30080 
  selector:
    app: webapp1
```

```shell
$ kubectl create -f service.yaml
$ kubectl get svc
$ kubectl describe svc webapp1-svc
$ curl host01:30080
```

#### Scale Deployment

更新 deployment.yaml 中的 replicas 为 4，然后

```shell
$ kubectl apply -f deployment.yaml
$ kubectl get deployment
$ kubectl get pods
$ curl host01:30080
```

