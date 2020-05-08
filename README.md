Language: :us::[cn](./README_zh.md): 
# KafkaCenter

![](https://img.shields.io/badge/java-1.8+-green.svg)
![](https://img.shields.io/badge/maven-3.5+-green.svg)

KafkaCenter is a unified one-stop platform for Kafka cluster management and maintenance, producer / consumer monitoring, and use of ecological components.
- [KafkaCenter](#kafkacenter)
  - [Main Features](#main-features)
  - [Getting Started](#getting-started)
    - [Building and Running KafkaCenter, and/or Contributing Code](#building-and-running-kafkacenter-andor-contributing-code)
  - [Documentation](#documentation)
  - [TODO](#todo)
  - [Changelog](#changelog)
  - [Questions? Problems? Suggestions?](#questions-problems-suggestions)

## Main Features
![avatar](docs/images/kafka-center.png)
![avatar](docs/images/screenshot.png)
- **Home**->
view kafka cluster list and monitoring information
- **Topic**->
Users can view their own topics in this module, apply for new topics, mock and consumption data .
- **Monitor**->
Users can view the production and consumption of topics in this module, and set warning information for consumption delays.
- **Kafka Connect**->
Users able to quickly create their own Connect jobs and maintain their own connect.
- **KSQL**->
Users able to quickly create their own KSQL jobs and maintain their jobs.
- **Approve**->
This module is mainly used as a common user application to create Topic, operations administrator for approval.
- **Setting**->
The administrator maintains User, Team.
- **Kafka Manager**->
maintain kafka cluster information.
## Config
[application.properties](KafkaCenter-Core/src/main/resources/application.properties)
## Getting Started

**Important**: Before you begin, make sure you have installed **mysql**.

resource|dependencies|use
---|---|---
mysql|must|Configuration information is stored in **mysql**
elasticsearch(7.0+)|optional|Monitoring information, such as cluster metirc, consumption lag visualization, etc.
email server|optional|Apply, approval, warning e-mail alert
### 1.init
#### create database and table
execute [table_script.sql](KafkaCenter-Core/sql/table_script.sql)
#### edit config
down [application.properties](KafkaCenter-Core/src/main/resources/application.properties),edit config.
### 2.run
- Docker run(**recommend**)

```
docker run -d -p 8080:8080 --name KafkaCenter -v ${PWD}/application.properties:/opt/app/kafka-center/config/application.properties xaecbd/kafka-center:2.1.0
```

- Local run

**Important**: Before you begin, make sure you have installed **jre1.8** and download the release package in the release.
```
$ git clone https://github.com/xaecbd/KafkaCenter.git
$ cd KafkaCenter
$ mvn clean package -Dmaven.test.skip=true
$ cd KafkaCenter\KafkaCenter-Core\target
$ java -jar KafkaCenter-Core-2.1.0-SNAPSHOT.jar
```

### 3.view system
view`http://localhost:8080`,default administrator ：**admin/admin**
### Building and Running KafkaCenter, and/or Contributing Code

You might want to build KafkaCenter locally to contribute some code, test out the latest features, or try
out an open PR:

- [CONTRIBUTING.md](./CONTRIBUTING.md) will help you get KafkaCenter up and running.

## Documentation

For more information, see the README in [KafkaCenter/docs](./docs).<br/>
For information about user guide the documentation, see the UserGuide in [KafkaCenter/docs/UserGuide](./docs/UserGuide.md)  
For information about module the documentation, see the Module in [KafkaCenter/docs/Module](./docs/Module.md).<br/>
For information about kafka connect ui, see docs in [KafkaConnectUi](./docs/KafkaConnectUi.md).
## TODO

See [TODO List](https://github.com/xaecbd/KafkaCenter/projects/1)

## Changelog

See [CHANGELOG.md](./CHANGELOG.md)

## Questions? Problems? Suggestions?

- If you've found a bug or want to request a feature, please create a [Issue](https://github.com/xaecbd/KafkaCenter/issues/new).
Please check to make sure someone else hasn't already created an issue for the same topic.
- Need help using KafkaCenter? Ask EC Bigdata Team member.
