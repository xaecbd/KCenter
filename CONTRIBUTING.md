Language: :us: - :[cn](./CONTRIBUTING_zh.md):

# Contributing to KafkaCenter
We are very happy that you want to contribute to our project, awesome!

Below are some guidelines to get you started. Feel free to reach out in case anything is unclear.

## Code Specifications

### API Design Principles
For scenarios that require interaction with the backend, the backend endpoint should be consistent with the frontend routing address. If there are different requests for one page, these should be under the same parent in the tree. 

Here's an example for the *Monitoring* module:

Monitor > Topic:
```
Frontend routing：/#/monitor/topic

API:
- Search all topics: /monitor/topic
```

Monitor > Alert:
```
Frontend routing：/#/monitor/alert

API:
- Search all alerts: /monitor/alert
- Add alert: /monitor/alert/add
```

### Frontend Specifications
- File line break: `LF`
- `ESLint` must be used
- If a subcomponent is used across multiple components, it must be moved up in the hierarchy and used jointly

### Backend Specifications
Please adhere to the [Alibaba-Java-Coding-Guidelines](https://alibaba.github.io/Alibaba-Java-Coding-Guidelines/).

## Frontend Development
[Visual Studio Code](https://code.visualstudio.com/) is recommended for development on the frontend. Since the frontend of the project depends on the backend service, you can either start the backend services locally or modify package.json `proxyConfig` in `package.json` to an address where the backend services are available.

Have a look at the separate [frontend README](KCenter-Frontend/README.md) for more details.

### 1. Install *nodejs*
*Self-explanatory...*

### 2. Run
```
$ cd KafkaCenter/KafkaCenter-Frontend
$ npm install
$ npm start
```

### 3. Release
```
$ cd KafkaCenter/KafkaCenter-Frontend
$ npm run build
```
The compiled code will be posted to: `../KafkaCenter-Core/src/main/resources/static`

## Backend Development

### 1. Install *jdk 11* & *maven3.5+*
*Self-explanatory...*

### 2. Compile / Run
```
$ cd KafkaCenter
$ mvn clean package -Dmaven.test.skip=true
$ cd KafkaCenter\KafkaCenter-Core\target
$ java -jar KafkaCenter-Core-0.0.1-SNAPSHOT.jar
```

### 3. Release
```
$ cd KafkaCenter
$ mvn clean package -Dmaven.test.skip=true
```
