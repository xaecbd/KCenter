# Monitor API

为了给用户提供更多的感知consumer状态，我们系统对外暴露出一系列Rest API
## 消费延迟
数据准确性存在5分钟延迟（为避免过度调用对集群产生影响）
```http request
GET http://127.0.0.1:8080/api/consumer/status?cluster_id=1&topic=trumandu_topic&group=trumandu_test

{
    "code": 200,
    "data": [
        {
            "group": "trumandu_test",
            "topic": "trumandu_topic",
            "status": "active",
            "lag": 0,
            "method": "zk"
        }
    ]
}
```
cluster_id 可以在Monitor/Consumer查看指定group的页面，在地址栏中的查询。暂时这么查找，后期我们会改善。

例如：如下1即为E4Kafka的cluster_id
```http request
https://127.0.0.1:8080/#/monitor/consumer/topic/consumer_offsets/1/E4Kafka/trumandu_topic
```

status有三种状态：active，unknown，dead


group不存在，返回值如下：
```http request
{
    "code": 500,
    "message": "topic is not exits,please check!"
}
```
如果group不存在，返回值如下：
```http request
{
    "code": 200,
    "data": []
}
```