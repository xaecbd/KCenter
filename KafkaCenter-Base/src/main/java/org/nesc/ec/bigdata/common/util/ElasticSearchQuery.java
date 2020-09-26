package org.nesc.ec.bigdata.common.util;

import java.util.Objects;

public class ElasticSearchQuery {

    public static String summaryMetricQuery(long start, long end) {
        int totalSize = 10000;
        return "{\n" +
                "  \"size\": 0,\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"must_not\": [\n" +
                "        {\n" +
                "          \"terms\": {\n" +
                "            \"metricName.keyword\": [\n" +
                "              \"BytesRejectedPerSec\",\n" +
                "              \"FailedFetchRequestsPerSec\",\n" +
                "              \"FailedProduceRequestsPerSec\"\n" +
                "            ]\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"exists\": {\n" +
                "            \"field\": \"topic\"\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"must\": [\n" +
                "        {\n" +
                "          \"exists\": {\n" +
                "            \"field\": \"broker\"\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"exists\": {\n" +
                "            \"field\": \"count\"\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"filter\": {\n" +
                "        \"range\": {\n" +
                "          \"timestamp\": {\n" +
                "            \"gte\": \"" + start + "\",\n" +
                "            \"lt\": \"" + end + "\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"aggs\": {\n" +
                "    \"metric_name\": {\n" +
                "      \"terms\": {\n" +
                "        \"field\": \"metricName.keyword\",\n" +
                "        \"size\": 3\n" +
                "      },\n" +
                "      \"aggs\": {\n" +
                "        \"broker\": {\n" +
                "          \"terms\": {\n" +
                "            \"field\": \"broker.keyword\",\n" +
                "            \"size\": " + totalSize + "\n" +
                "          },\n" +
                "          \"aggs\": {\n" +
                "            \"max_data\": {\n" +
                "              \"max\": {\n" +
                "                \"field\": \"count\"\n" +
                "              }\n" +
                "            },\n" +
                "            \"min_data\": {\n" +
                "              \"min\": {\n" +
                "                \"field\": \"count\"\n" +
                "              }\n" +
                "            },\n" +
                "            \"diff_data\": {\n" +
                "              \"bucket_script\": {\n" +
                "                \"buckets_path\": {\n" +
                "                  \"max_data\": \"max_data\",\n" +
                "                  \"min_data\": \"min_data\"\n" +
                "                },\n" +
                "                \"script\": \"params.max_data - params.min_data\"\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }


    public static String summaryMetricTrendQuery(String internal, long start, long end) {
        int sizeDefault = 10000;
        String diff = (end - start) <= (24 * 60 * 60 * 1000) ? "hour" : "day";
        return "{\n" +
                "  \"size\": 0,\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"must_not\": [\n" +
                "        {\n" +
                "          \"terms\": {\n" +
                "            \"metricName.keyword\": [\n" +
                "              \"BytesRejectedPerSec\",\n" +
                "              \"FailedFetchRequestsPerSec\",\n" +
                "              \"FailedProduceRequestsPerSec\"\n" +
                "            ]\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"exists\": {\n" +
                "            \"field\": \"topic\"\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"must\": [\n" +
                "        {\n" +
                "          \"exists\": {\n" +
                "            \"field\": \"broker\"\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"exists\": {\n" +
                "            \"field\": \"count\"\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"filter\": {\n" +
                "        \"range\": {\n" +
                "          \"timestamp\": {\n" +
                "            \"gte\": " + start + ",\n" +
                "            \"lt\": " + end + "\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"aggs\": {\n" +
                "    \"datagrame\": {\n" +
                "      \"auto_date_histogram\": {\n" +
                "        \"field\": \"timestamp\",\n" +
                "        \"buckets\": \"50\",\n" +
                "        \"minimum_interval\": \"" + diff + "\"\n" +
                "      },\n" +
                "      \"aggs\": {\n" +
                "        \"cluster_id\": {\n" +
                "          \"terms\": {\n" +
                "            \"field\": \"clusterID.keyword\",\n" +
                "            \"size\": 100\n" +
                "          },\n" +
                "          \"aggs\": {\n" +
                "            \"metric_name\": {\n" +
                "              \"terms\": {\n" +
                "                \"field\": \"metricName.keyword\",\n" +
                "                \"size\": 100\n" +
                "              },\n" +
                "              \"aggs\": {\n" +
                "                \"broker\": {\n" +
                "                  \"terms\": {\n" +
                "                    \"field\": \"broker.keyword\",\n" +
                "                    \"size\": " + sizeDefault + "\n" +
                "                  },\n" +
                "                  \"aggs\": {\n" +
                "                    \"max_data\": {\n" +
                "                      \"max\": {\n" +
                "                        \"field\": \"count\"\n" +
                "                      }\n" +
                "                    },\n" +
                "                    \"min_data\": {\n" +
                "                      \"min\": {\n" +
                "                        \"field\": \"count\"\n" +
                "                      }\n" +
                "                    }\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }


    public static String clusterTrendAggres(long start, long end, long clientId, String interval) {
        return "{\n" +
                "  \"size\": 0,\n" +
                "  \"aggs\": {\n" +
                "    \"datagrame\": {\n" +
                "      \"date_histogram\": {\n" +
                "        \"field\": \"date\",\n" +
                "        \"interval\": \"" + interval + "\"\n" +
                "      },\n" +
                "      \"aggs\": {\n" +
                "        \"metric\": {\n" +
                "          \"terms\": {\n" +
                "            \"field\": \"metricName.keyword\",\n" +
                "            \"size\": 10\n" +
                "          },\n" +
                "          \"aggs\": {\n" +
                "            \"broker\": {\n" +
                "              \"terms\": {\n" +
                "                \"field\": \"broker.keyword\",\n" +
                "                \"size\": 30\n" +
                "              },\n" +
                "              \"aggs\": {\n" +
                "                \"avg_rate\": {\n" +
                "                  \"avg\": {\n" +
                "                    \"field\": \"oneMinuteRate\"\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"filter\": [\n" +
                "        {\n" +
                "          \"term\": {\n" +
                "            \"clusterID.keyword\": \"" + clientId + "\"\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"range\": {\n" +
                "            \"date\": {\n" +
                "              \"gte\":" + start + ",\n" +
                "              \"lte\":" + end + "\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"terms\": {\n" +
                "            \"metricName.keyword\": [\n" +
                "              \"MessagesInPerSec\",\n" +
                "              \"BytesInPerSec\",\n" +
                "              \"BytesOutPerSec\"\n" +
                "            ]\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"must_not\": [\n" +
                "        {\n" +
                "          \"exists\": {\n" +
                "            \"field\": \"topic\"\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"must\": [\n" +
                "        {\n" +
                "          \"exists\": {\n" +
                "            \"field\": \"broker\"\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"exists\": {\n" +
                "            \"field\": \"count\"\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    public static String getClusterQueryString(int size, Long clusterId, Long timeStamp) {
        return "{\n" +
                "  \"size\": " + size + ", \n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"must\": [\n" +
                "        {\n" +
                "          \"match\": {\n" +
                "            \"clusterId\": " + clusterId + "\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"range\": {\n" +
                "            \"timestamp\": {\n" +
                "              \"gte\": " + timeStamp + "\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    public static String getLagQueryString(String clientId) {
        return "{\n" +
                "				 \"size\": 0,\n" +
                "				  \"aggs\": {\n" +
                "				    \"2\": {\n" +
                "				      \"terms\": {\n" +
                "				        \"field\": \"clusterId\" \n" +
                "						},\n" +
                "				      \"aggs\": {\n" +
                "				        \"3\": {\n" +
                "				          \"max\": {\n" +
                "				            \"field\": \"timestamp\"\n" +
                "				          }\n" +
                "				        }\n" +
                "				      }\n" +
                "				    }\n" +
                "				  },\n" +
                "				  \"query\": {\n" +
                "				     \"bool\": {\n" +
                "				        \"must\": [\n" +
                "				          {\n" +
                "				            \"term\": {\n" +
                "				              \"clusterId\": {\n" +
                "				                \"value\": \"" + clientId + "\"\n" +
                "				              }\n" +
                "				            }\n" +
                "				          }\n" +
                "				        ], \n" +
                "				         \"filter\": {\n" +
                "				             \"exists\": {\n" +
                "				               \"field\": \"lag\"\n" +
                "				             }\n" +
                "				         }\n" +
                "				     }\n" +
                "				  }\n" +
                "				}";
    }

    /**
     * 查询生成消费情况历史信息
     */
    public static String getRequestBody(String clusterId, String topic, String group, String type, String start, String end) {

        return "{\n" +
                "  \"version\": true,\n" +
                "  \"size\": 10000,\n" +
                "  \"sort\": [\n" +
                "    {\n" +
                "      \"timestamp\": {\n" +
                "        \"order\": \"desc\",\n" +
                "        \"unmapped_type\": \"boolean\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"must\": [\n" +
                "        {\n" +
                "          \"match_all\": {}\n" +
                "        },\n" +
                "        {\n" +
                "          \"term\": {\n" +
                "            \"topic.keyword\": {\n" +
                "              \"value\": \"" + topic + "\"\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"term\": {\n" +
                "            \"group.keyword\": {\n" +
                "              \"value\": \"" + group + "\"\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"term\": {\n" +
                "            \"consumerType.keyword\": {\n" +
                "              \"value\": \"" + type + "\"\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"term\": {\n" +
                "            \"clusterId\": {\n" +
                "              \"value\": " + clusterId + "\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"range\": {\n" +
                "            \"date\": {\n" +
                "              \"gte\": " + start + ",\n" +
                "              \"lte\": " + end + ",\n" +
                "              \"format\": \"epoch_millis\"\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    /**
     * 获取cluster的oneMinuteRate信息
     */
    public static String clusterQuery(long start, long end, long clientId) {
        return "{\n" +
                "  \"size\": 10000,\n" +
                "  \"_source\": {\n" +
                "    \"includes\": [\n" +
                "      \"timestamp\",\n" +
                "      \"metricName\",\n" +
                "      \"oneMinuteRate\",\n" +
                "      \"broker\"\n" +
                "    ]\n" +
                "  },\n" +
                " \n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"filter\": [\n" +
                "        {\n" +
                "          \"term\": {\n" +
                "            \"clusterID.keyword\": \"" + clientId + "\"\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"range\": {\n" +
                "            \"date\": {\n" +
                "              \"gte\":" + start + ",\n" +
                "              \"lte\":" + end + "\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"terms\": {\n" +
                "            \"metricName.keyword\": [\n" +
                "              \"MessagesInPerSec\",\n" +
                "              \"BytesInPerSec\",\n" +
                "              \"BytesOutPerSec\"\n" +
                "            ]\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"must_not\": [\n" +
                "        {\n" +
                "          \"exists\": {\n" +
                "            \"field\": \"topic\"\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"must\": [\n" +
                "        {\n" +
                "          \"exists\": {\n" +
                "            \"field\": \"broker\"\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"exists\": {\n" +
                "            \"field\": \"count\"\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    /**
     * 获取消费情况历史信息
     */
    public static String getDateIntervalQueryBody(String clusterId, String topic, String group, String type, String start, String end, String interval) {
        return "{\n" +
                "    \"size\": 0,\n" +
                "    \"aggs\": {\n" +
                "        \"aggs\": {\n" +
                "            \"date_histogram\": {\n" +
                "                \"field\": \"timestamp\",\n" +
                "                \"interval\": \"" + interval + "\"\n" +
                "            },\n" +
                "              \"aggs\": {\n" +
                "                  \"offset\": {\n" +
                "                      \"max\": {\n" +
                "                          \"field\": \"offset\"\n" +
                "                      }\n" +
                "                  },\n" +
                "                  \"lag\": {\n" +
                "                      \"max\": {\n" +
                "                          \"field\": \"lag\"\n" +
                "                      }\n" +
                "                  }\n" +
                "              }\n" +
                "          }\n" +
                "        \n" +
                "    },\n" +
                "    \"query\": {\n" +
                "        \"bool\": {\n" +
                "            \"must\": [\n" +
                "                {\n" +
                "                    \"term\": {\n" +
                "                        \"consumerType.keyword\": {\n" +
                "                            \"value\": \"" + type + "\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                },\n" +
                "                {\n" +
                "                    \"term\": {\n" +
                "                        \"group.keyword\": {\n" +
                "                            \"value\": \"" + group + "\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                },\n" +
                "                {\n" +
                "                    \"term\": {\n" +
                "                        \"topic.keyword\": {\n" +
                "                            \"value\": \"" + topic + "\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                },\n" +
                "                {\n" +
                "                    \"term\": {\n" +
                "                        \"clusterId\": {\n" +
                "                            \"value\": \"" + clusterId + "\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                }\n" +
                "            ],\n" +
                "            \"filter\": [\n" +
                "                {\n" +
                "                    \"range\": {\n" +
                "                        \"timestamp\": {\n" +
                "                            \"gte\": " + start + ",\n" +
                "                            \"lte\": " + end + ",\n" +
                "                            \"format\": \"epoch_millis\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    }\n" +
                "}";
    }

    public static String searchTopicMetricQuery(long start, long end, String clusterId, String topic, String metric) {
        return "{\n" +
                "  \"size\": 0, \n" +
                "  \"aggs\": {\n" +
                "    \"date\": {\n" +
                "      \"auto_date_histogram\": {\n" +
                "        \"field\": \"date\",\n" +
                "        \"buckets\": \"10\",\n" +
                "        \"format\":\"yyyy-MM-dd HH:mm:ss\"\n" +
                "      },\n" +
                "      \"aggs\": {\n" +
                "        \"metric\": {\n" +
                "          \"max\": {\n" +
                "            \"field\": \"count\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"dev\":{\n" +
                "          \"derivative\": {\n" +
                "            \"buckets_path\": \"metric\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"filter\": [\n" +
                "        {\n" +
                "          \"range\": {\n" +
                "            \"date\": {\n" +
                "              \"gte\": \"" + start + "\",\n" +
                "              \"lte\": \"" + end + "\",\n" +
                "              \"format\":\"epoch_millis\"\n" +
                "            }\n" +
                "          }\n" +
                "        },{\n" +
                "          \"term\":{\n" +
                "            \"clusterID\":\"" + clusterId + "\"\n" +
                "          }\n" +
                "        },{\n" +
                "            \"term\":{\n" +
                "              \"topic.keyword\":\"" + topic + "\"\n" +
                "            }\n" +
                "          },{\n" +
                "            \"term\":{\n" +
                "             \"metricName.keyword\":\"" + metric + "\"\n" +
                "            }\n" +
                "            \n" +
                "          }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    public static String top10TopicLogSizeRang7Days(String topics, long start, long end) {
        String filterQuery = Objects.isNull(topics) ? "" : " ,{\n" +
                "          \"terms\": {\n" +
                "            \"topic.keyword\": [" + topics + "]\n" +
                "          }\n" +
                "        }";
        return "{\n" +
                "  \"size\": 0,\n" +
                "  \"aggs\": {\n" +
                "    \"date\": {\n" +
                "      \"auto_date_histogram\": {\n" +
                "        \"field\": \"date\",\n" +
                "        \"buckets\": \"8\",\n" +
                "        \"format\": \"yyyy-MM-dd HH:mm:ss\"\n" +
                "      },\n" +
                "      \"aggs\": {\n" +
                "        \"cluster\": {\n" +
                "          \"terms\": {\n" +
                "            \"field\": \"clusterName.keyword\",\n" +
                "            \"size\": 10\n" +
                "          },\n" +
                "          \"aggs\": {\n" +
                "            \"topic\": {\n" +
                "              \"terms\": {\n" +
                "                \"field\": \"topic.keyword\",\n" +
                "                \"order\": {\n" +
                "                  \"logSize\": \"desc\"\n" +
                "                },\n" +
                "                \"size\": 10\n" +
                "              },\n" +
                "              \"aggs\": {\n" +
                "                \"logSize\": {\n" +
                "                  \"max\": {\n" +
                "                    \"field\": \"logSize\"\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"filter\": [\n" +
                "        {\n" +
                "          \"range\": {\n" +
                "            \"date\": {\n" +
                "              \"gte\": \"" + start + "\",\n" +
                "              \"lte\": \"" + end + "\",\n" +
                "              \"format\": \"epoch_millis\"\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" + filterQuery +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    public static String top10TopicFileSizeRang7Days(String topics, long start, long end) {
        String filterQuery = Objects.isNull(topics) ? "" : " ,{\n" +
                "          \"terms\": {\n" +
                "            \"topic.keyword\": [" + topics + "]\n" +
                "          }\n" +
                "        }";
        return "{\n" +
                "  \"size\": 0,\n" +
                "  \"aggs\": {\n" +
                "    \"date\": {\n" +
                "      \"auto_date_histogram\": {\n" +
                "        \"field\": \"date\",\n" +
                "        \"buckets\": \"8\",\n" +
                "        \"format\": \"yyyy-MM-dd HH:mm:ss\"\n" +
                "      },\n" +
                "      \"aggs\": {\n" +
                "        \"cluster\": {\n" +
                "          \"terms\": {\n" +
                "            \"field\": \"clusterName.keyword\",\n" +
                "            \"size\": 100\n" +
                "          },\n" +
                "          \"aggs\": {\n" +
                "            \"topic\": {\n" +
                "              \"terms\": {\n" +
                "                \"field\": \"topic.keyword\",\n" +
                "                \"size\": 10\n" +
                "              },\n" +
                "              \"aggs\": {\n" +
                "                \"max_file\": {\n" +
                "                  \"max\": {\n" +
                "                    \"field\": \"fileSize\"\n" +
                "                  }\n" +
                "                },\n" +
                "                \"sort\": {\n" +
                "                  \"bucket_sort\": {\n" +
                "                    \"sort\": [\n" +
                "                      {\n" +
                "                        \"max_file\": {\n" +
                "                          \"order\": \"desc\"\n" +
                "                        }\n" +
                "                      }\n" +
                "                    ]\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"filter\": [\n" +
                "        {\n" +
                "          \"range\": {\n" +
                "            \"date\": {\n" +
                "              \"gte\": \"" + start + "\",\n" +
                "              \"lte\": \"" + end + "\",\n" +
                "              \"format\": \"epoch_millis\"\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" + filterQuery +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }
}
