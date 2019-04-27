# frontend18v

## 4.26 - 4.30 任务

- [x] 选中聚焦居中

- [ ] 点击空白处取消高亮

- [ ] 某些展示信息错误更正

- [x] 关系线上的文字 hover 时才显示

- [ ] 界面上刚开始的时候只有一个 environment 节点，填写信息展示 KG

- [ ] 前端增删改的实现，注意和后端连通

- [x] 界面右下角操作图表，鼠标在 hover 上去的时候给出相应的操作的名字

---

- [ ] 展示属性的时候，在信息框最开始节点名旁边加上其节点类别信息

- [ ] 左边树状结构的“node”一词改一下，不要和传统的节点相混合

- [ ] 前端查询的实现

## 发现的 bug

- [ ] 左边树状列表收起来的时候 右边的节点会消失

## Project setup
```
npm install
```

### Compiles and hot-reloads for development
```
npm run serve
```

### Compiles and minifies for production
```
npm run build
```

### Run your tests
```
npm run test
```

### Lints and fixes files
```
npm run lint
```

### Customize configuration
See [Configuration Reference](https://cli.vuejs.org/config/).


### Build
```
docker build -t {dockerhub-username}/frontend .
```

### Push
```
docker login -u {dockerhub-username}
docker tag {dockerhub-username}/frontend {dockerhub-username}/frontend
docker push {dockerhub-username}/frontend
```

If there is an error, see https://github.com/aws/aws-cli/issues/3264
```
Error saving credentials: error storing credentials - err: exit status 1, out: `The user name or passphrase you entered is not correct.`
```

## Pull
```
sudo docker pull {dockerhub-username}/frontend
```

### Run
```
sudo docker run -p 9998:8080 -d {dockerhub-username}/frontend
```
