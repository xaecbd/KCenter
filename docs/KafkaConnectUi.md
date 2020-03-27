# Kafka Center 中Kafka Connect ui 部署

Kafka Center中使用内嵌frame的形式加入了Kafka Connect ui，为了适配Kafka Center我们基于 https://github.com/lensesio/kafka-connect-ui 代码进行了稍微调整

## Run standalone with docker

```sh
docker run --rm -it -p 8000:8000 \
           -e "CONNECT_URL=http://connect.distributed.url" \
           xaecbd/kafka-connect-ui
```

更多详细信息请见： [README](https://github.com/xaecbd/kafka-connect-ui)

## Kafka Center Config

```
connect.url=http://localhost:8000/#/
```

## Kafka Connect

关于Kafka Connect相关信息，可通过官网地址 [Kafka Connect](https://docs.confluent.io/current/connect/index.html) 进行了解。

也可通过博客查看Kafak Connect的相关信息：[简书地址](https://www.jianshu.com/u/01a741ad7f8c)
