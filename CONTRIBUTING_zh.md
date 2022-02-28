Language: :[us](./CONTRIBUTING.md): - :cn:

# CONTRIBUTING

## 代码规范

### 规约

#### API设计

针对需要和后端交互的场景，后端API应与前端路由地址保持一致，如果一个页面存在多个网络请求按树形结构划分，例如：
Monitor>Topic模块
```
前端路由：/#/monitor/topic
API:
- 查询所有Topic /monitor/topic
```

Monitor>Alert模块
```
前端路由：/#/monitor/alert
API:
- 查询所有alert /monitor/alert
- 新增alert /monitor/alert/add
```

### 前端规范

- 文件换行符`LF`
- 必须安装ESLint,检查代码规范
- 能公用组件，必须提升成公用组件

### 后端代码规范

使用[阿里java 规范手册约束](https://alibaba.github.io/Alibaba-Java-Coding-Guidelines/)

## 前端开发

推荐使用VS Code 开发前端代码,前端项目依赖后端服务，可以先启动后端服务，或者修改`package.json`中`proxyConfig`，将其中的地址改为可用的后端服务地址。更多内容[详见地址](KCenter-Frontend/README.md)

### 安装node

略

### 运行

```
$ cd KafkaCenter/KafkaCenter-Frontend
$ npm install
$ npm start
```

### 发布

```
$ cd KafkaCenter/KafkaCenter-Frontend
$ npm run build
```
编译后的代码会发布到`../KafkaCenter-Core/src/main/resources/static`
## 后端开发

### 安装jdk 11/maven3.5+

略

### 编译/运行

```
$ cd KafkaCenter
$ mvn clean package -Dmaven.test.skip=true
$ cd KafkaCenter\KafkaCenter-Core\target
$ java -jar KafkaCenter-Core-0.0.1-SNAPSHOT.jar
```

### 发布

```
$ cd KafkaCenter
$ mvn clean package -Dmaven.test.skip=true
```
