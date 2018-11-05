# Neo4j 学习报告

> 2018/10/29 陈雨蕾

[TOC]

## Neo4j 基本概念

> Neo4j is an open-source, high-performance, enterprise-grade NOSQL graph database.

![The Labeled Property Graph Model](https://github.com/XLab-Tongji/Operation_KnowledgeGraph/blob/master/学习报告/img/neo4j_model.png?raw=true)

### Node（节点）

节点是图表的基本单位，它包含具有键值对的属性

### Property（属性）

属性是用于描述图节点和关系的键值对

### Relationship（关系）

关系是图形数据库的一个主要构建块，它连接两个节点

### Label（标签）

标签将一个公共名称与一组节点或关系相关联。 节点或关系可以包含一个或多个标签，我们可以为现有节点或关系创建新标签，也可以从现有节点或关系中删除现有标签



## Neo4j 下载

MacOS Desktop版下载地址：https://neo4j.com/download/



## Neo4j CQL

### CREATE 命令

**1. CREATE命令为节点创建单个标签**

语法：

```CQL
CREATE (<node-name>:<label-name>)
```

语法说明：

| 语法元素      | 描述                       |
| ------------- | -------------------------- |
| CREATE        | 它是一个Neo4j CQL命令。    |
| \<node-name>  | 它是我们要创建的节点名称。 |
| \<label-name> | 它是一个节点标签名称       |

**2. CREATE命令创建具有属性的节点**

语法：

```cql
CREATE (
   <node-name>:<label-name>
   { 	
      <Property1-name>:<Property1-Value>
      ........
      <Propertyn-name>:<Propertyn-Value>
   }
)
```

语法说明：

| 语法元素                                | 描述                                            |
| --------------------------------------- | ----------------------------------------------- |
| \<node-name>                            | 它是我们将要创建的节点名称。                    |
| \<label-name>                           | 它是一个节点标签名称                            |
| \<Property1-name>...\<Propertyn-name>   | 属性是键值对。 定义将分配给创建节点的属性的名称 |
| \<Property1-value>...\<Propertyn-value> | 属性是键值对。 定义将分配给创建节点的属性的值   |

**3. CREATE命令为节点创建多个标签**

语法：

```
CREATE (<node-name>:<label-name1>:<label-name2>.....:<label-namen>)
```

语法说明：

| 语法元素                                           | 描述                           |
| -------------------------------------------------- | ------------------------------ |
| CREATE 创建                                        | 这是一个Neo4j CQL关键字。      |
| \<node-name> <节点名称>                            | 它是一个节点的名称。           |
| \<label-name1>,\<label-name2> <标签名1>，<标签名2> | 它是一个节点的标签名称的列表。 |

**4. CREATE命令为关系创建单个标签**

语法：

```cql
CREATE (<node1-name>:<label1-name>)-
	[(<relationship-name>:<relationship-label-name>)]
	->(<node2-name>:<label2-name>)
```

语法说明：

| 语法元素                                  | 描述                      |
| ----------------------------------------- | ------------------------- |
| CREATE 创建                               | 它是一个Neo4J CQL关键字。 |
| \<node1-name> <节点1名>                   | 它是From节点的名称。      |
| \<node2-name> <节点2名>                   | 它是To节点的名称。        |
| \<label1-name> <LABEL1名称>               | 它是From节点的标签名称    |
| \<label1-name> <LABEL1名称>               | 它是To节点的标签名称。    |
| \<relationship-name> <关系名称>           | 它是一个关系的名称。      |
| \<relationship-label-name> <相关标签名称> | 它是一个关系的标签名称。  |

### MATCH 命令

语法：

```CQL
MATCH 
(
   <node-name>:<label-name>
)
```

语法说明：

| 语法元素      | 描述                         |
| ------------- | ---------------------------- |
| \<node-name>  | 这是我们要创建一个节点名称。 |
| \<label-name> | 这是一个节点的标签名称       |

### RETURN命令

语法：

```CQL
RETURN 
   <node-name>.<property1-name>,
   ........
   <node-name>.<propertyn-name>
```

语法说明：

| 语法元素                              | 描述                                                         |
| ------------------------------------- | ------------------------------------------------------------ |
| \<node-name>                          | 它是我们将要创建的节点名称。                                 |
| \<Property1-name>...\<Propertyn-name> | 属性是键值对。 \<Property-name>定义要分配给创建节点的属性的名称 |

### MATCH & RETURN 命令

> 在Neo4j CQL中，我们不能单独使用MATCH或RETURN命令，因此我们应该合并这两个命令以从数据库检索数据。

语法：

```CQL
MATCH Command
RETURN Command
```

语法说明：

| 语法元素   | 描述                       |
| ---------- | -------------------------- |
| MATCH命令  | 这是Neo4j CQL MATCH命令。  |
| RETURN命令 | 这是Neo4j CQL RETURN命令。 |

### WHERE 子句

简单WHERE子句语法：

```
WHERE <condition>
```

复杂WHERE子句语法：

```
WHERE <condition> <boolean-operator> <condition>
```

\<condition>语法：

```
<property-name> <comparison-operator> <value>
```

语法说明：

| 语法元素                            | 描述                                       |
| ----------------------------------- | ------------------------------------------ |
| WHERE                               | 它是一个Neo4j CQL关键字。                  |
| \<property-name> <属性名称>         | 它是节点或关系的属性名称。                 |
| \<comparison-operator> <比较运算符> | 它是Neo4j CQL比较运算符之一。              |
| \<value> <值>                       | 它是一个字面值，如数字文字，字符串文字等。 |

### DELETE 命令

**1. 删除节点及其属性**

语法：

```
DELETE <node-name-list>
```

语法说明：

| 语法元素          | 描述                                     |
| ----------------- | ---------------------------------------- |
| DELETE            | 它是一个Neo4j CQL关键字。                |
| \<node-name-list> | 它是一个要从数据库中删除的节点名称列表。 |

**2. 删除节点及其关联节点和关系**

语法：

```
DELETE <node1-name>,<node2-name>,<relationship-name>
```

语法说明：

| 语法元素             | 描述                                                         |
| -------------------- | ------------------------------------------------------------ |
| DELETE               | 它是一个Neo4j CQL关键字。                                    |
| \<node1-name>        | 它是用于创建关系\<relationship-name>的一个结束节点名称。     |
| \<node2-name>        | 它是用于创建关系\<relationship-name>的另一个节点名称。       |
| \<relationship-name> | 它是一个关系名称，它在\<node1-name>和\<node2-name>之间创建。 |

### REMOVE 命令

> Neo4j CQL DELETE和REMOVE命令之间的主要区别：
>
> - DELETE操作用于删除节点和关联关系。
> - REMOVE操作用于删除标签和属性。

**1. 删除节点/关系的属性**

语法：

```
REMOVE <property-name-list>
```

语法说明：

| 语法元素              | 描述                                                 |
| --------------------- | ---------------------------------------------------- |
| REMOVE                | 它是一个Neo4j CQL关键字。                            |
| \<property-name-list> | 它是一个属性列表，用于永久性地从节点或关系中删除它。 |

\<property-name-list> <属性名称列表>语法：

```
<node-name>.<property1-name>,
<node-name>.<property2-name>, 
.... 
<node-name>.<propertyn-name> 
```

语法说明：

| 语法元素         | 描述                 |
| ---------------- | -------------------- |
| \<node-name>     | 它是节点的名称。     |
| \<property-name> | 它是节点的属性名称。 |

**2. 删除节点/关系的标签**

语法：

```
REMOVE <label-name-list> 
```

语法说明：

| 语法元素           | 描述                      |
| ------------------ | ------------------------- |
| REMOVE             | 它是一个Neo4j CQL关键字。 |
| \<label-name-list> | 它是一个标签列表。        |

\<label-name-list>语法：

```
<node-name>:<label2-name>, 
.... 
<node-name>:<labeln-name> 
```

语法说明：

| 语法元素                 | 描述                     |
| ------------------------ | ------------------------ |
| \<node-name> <节点名称>  | 它是一个节点的名称。     |
| \<label-name> <标签名称> | 这是一个节点的标签名称。 |

### SET 子句

> Neo4j CQL已提供SET子句来执行以下操作。
>
> - 向现有节点或关系添加新属性
> - 添加或更新属性值

语法：

```
SET  <property-name-list>
```

语法说明：

| 语法元素              | 描述                                                       |
| --------------------- | ---------------------------------------------------------- |
| SET                   | 它是一个Neo4j的CQL关键字。                                 |
| \<property-name-list> | 它是一个属性列表，用于执行添加或更新操作以满足我们的要求。 |

\<property-name-list><属性名称列表>语法：

```
<node-label-name>.<property1-name>,
<node-label-name>.<property2-name>, 
.... 
<node-label-name>.<propertyn-name> 
```

语法说明：

| 语法元素                          | 描述                     |
| --------------------------------- | ------------------------ |
| \<node-label-name> <节点标签名称> | 这是一个节点的标签名称。 |
| \<property-name> <属性名称>       | 它是一个节点的属性名。   |

## Reference

1. [Neo4j W3Cschool](https://www.w3cschool.cn/neo4j/neo4j_cypher_api_example.html)

2. [图灵学院《Neo4j图数据库-大数据时代的新利器》](https://www.bilibili.com/video/av23298437?from=search&seid=17291821585620388429)

