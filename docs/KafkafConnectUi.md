# Kafka Center 中Kafka Connect ui 部署

Kafka Center中使用内嵌frame的形式加入了Kafka Connect ui，为了适配Kafka Center我们基于 https://github.com/lensesio/kafka-connect-ui 代码进行了稍微调整

## Run standalone with docker

```sh
docker run --rm -it -p 8000:8000 \
           -e "CONNECT_URL=http://connect.distributed.url" \
           landoop/kafka-connect-ui
```

更多详细信息请见： [README](https://github.com/xaecbd/kafka-connect-ui)

## Kafka Center Config

```
connect.url=http://localhost:8000/#/
```

