# High-level Design (概要设计规约)


## Interface Specifications （接口规约）

### 1 *api/k8s/availableNodes*   

#### Description (接口描述)

返回所有可用的节点。

| | |
|-|-|
| Request Method | Get |
| Authorization | Required |

#### Parameters (参数)

None

#### Responses (返回结果)

| Code | Description | Schema |
|:----:|:--------|:--|
| 200 | Successful response | nodes : [String] |

#### Request Sample (示例请求)

```
api/k8s/availableNodes?
```

#### Response Sample (示例结果)

```
{

  "nodes" : [
      {
          "node_id": "";
          "node_name": "";
      }
  ]

}

```
---

### 2 *api/k8s/getServices*   

#### Description (接口描述)

返回所有正在进行的服务。

|                |          |
| -------------- | -------- |
| Request Method | Get      |
| Authorization  | Required |

#### Parameters (参数)

None

#### Responses (返回结果)

| Code | Description         | Schema              |
| :--: | :------------------ | :------------------ |
| 200  | Successful response | services : [String] |

#### Request Sample (示例请求)

```
api/k8s/getServices?
```

#### Response Sample (示例结果)

```
{

  "services" : [
      {
          "service_id" : "";
          "service_name" : "";
      }
  ]

}

```

------

### 3 *api/k8s/getService_Node*   

#### Description (接口描述)

获取某一节点上正在运行的服务。

|                |          |
| -------------- | -------- |
| Request Method | Get      |
| Authorization  | Required |

#### Parameters (参数)

| Name | Located in | Description | Required | Schema |
| :--: | :--------: | :---------- | :------: | :----- |
|  id  |   query    | node id     |   Yes    | String |

#### Responses (返回结果)

| Code | Description         | Schema              |
| :--: | :------------------ | :------------------ |
| 200  | Successful response | services : [String] |

#### Request Sample (示例请求)

```
api/k8s/getService_Node?nodeID=
```

#### Response Sample (示例结果)

```
{

  "services" : [
      {
          "service_id" : "";
          "service_name" : "";
      }
  ]

}

```

------

