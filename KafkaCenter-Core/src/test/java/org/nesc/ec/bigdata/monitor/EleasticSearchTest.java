package org.nesc.ec.bigdata.monitor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.constant.Constants;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.util.*;


public class EleasticSearchTest {

    private RestHighLevelClient client = null;

    public String summaryTrend(long start,long end){
        String diff = (end-start)<= 24*60*60*1000?"hour":"day";
        String query = "{\n" +
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
                "        }\n" +
                "      ],\n" +
                "      \"filter\": {\n" +
                "        \"range\": {\n" +
                "          \"timestamp\": {\n" +
                "            \"gte\": "+start+",\n" +
                "            \"lt\": "+end+"\n" +
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
                "        \"minimum_interval\": \""+diff+"\""+
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
                "                    \"size\": "+100+"\n" +
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
//                "                    \"diff_data\": {\n" +
//                "                      \"bucket_script\": {\n" +
//                "                        \"buckets_path\": {\n" +
//                "                          \"max_data\": \"max_data\",\n" +
//                "                          \"min_data\": \"min_data\"\n" +
//                "                        },\n" +
//                "                        \"script\": \"params.max_data - params.min_data\"\n" +
//                "                      }\n" +
//                "                    }\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
        return  query;
    }

    public String summaryCount(long start,long end){
        String query = "{\n" +
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
                "        }\n" +
                "      ],\n" +
                "      \"filter\": {\n" +
                "        \"range\": {\n" +
                "          \"timestamp\": {\n" +
                "            \"gte\": \""+start+"\",\n" +
                "            \"lt\": \""+end+"\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"aggs\": {\n" +
                "    \"metric_name\": {\n" +
                "      \"terms\": {\n" +
                "        \"field\": \"metricName.keyword\"\n" +
                "      },\n" +
                "      \"aggs\": {\n" +
                "        \"broker\": {\n" +
                "          \"terms\": {\n" +
                "            \"field\": \"broker.keyword\",\n" +
                "            \"size\": 12\n" +
                "          },\n" +
                "          \"aggs\": {\n" +
                "            \"max_data\": {\n" +
                "              \"max\": {\n" +
                "                \"field\": \"count\"\n" +
                "              }\n" +
                "            },\n" +
                "            \"min_data\":{\n" +
                "              \"min\": {\n" +
                "                \"field\": \"count\"\n" +
                "              }\n" +
                "            },\n" +
                "                \"sum_data\": {\n" +
                "                  \"bucket_script\": {\n" +
                "                    \"buckets_path\": {\n" +
                "                      \"max_data\": \"max_data\",\n" +
                "                      \"min_data\": \"min_data\"\n" +
                "                    },\n" +
                "                    \"script\": \"params.max_data -params.min_data\"\n" +
                "                  }\n" +
                "                }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        return query;
    }

    public Map<String, Long> summaryCountData(long start, long end) {
        String searchQuery = this.summaryCount(start, end);
        Map<String, Long> map = new HashMap<>();
        List<Long> max_list = new ArrayList<Long>();
        List<Long> min_list = new ArrayList<Long>();
        try {
            JSONObject temp = this.searchES(searchQuery, "kafka_center_monitor-*");
            if (!temp.containsKey("aggregations")) {
                return map;
            }
            JSONArray buckets = temp.getJSONObject("aggregations").getJSONObject("metric_name").getJSONArray("buckets");
            buckets.forEach(obj -> {
                JSONObject clusters = (JSONObject) obj;
                //String clusterId = clusters.getString("key");
                String metric_name = clusters.getString("key");
                JSONArray bucketArr = clusters.getJSONObject("broker").getJSONArray("buckets");
                long value = 0L;
                for (Object object:bucketArr){
                    JSONObject jsonObject = (JSONObject) object;
                    value = value + jsonObject.getJSONObject("sum_data").getLongValue("value");
                }
                if (map.containsKey(metric_name)) {
                    long val = map.get(metric_name).longValue();
                    map.put(metric_name, val + value);
                } else {
                    map.put(metric_name, value);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    public Map<String,Map<String,Long>> summaryTrends(long start, long end) {
        Map<String,Map<String,Long>> result = new HashMap<>();
        String[] metricNames = {"BytesInPerSec", "BytesOutPerSec", "MessagesInPerSec"};

        try{
            String searchQuery = this.summaryTrend(start,end);
            JSONObject temp = this.searchES(searchQuery, "kafka_center_monitor-*");
            if (!temp.containsKey("aggregations")) {
                return result;
            }
            if(temp.containsKey("aggregations")){
                JSONObject sss = temp.getJSONObject("aggregations");
            }
            JSONObject tests = temp.getJSONObject("aggregations").getJSONObject("datagrame");
            JSONArray metrics = tests.getJSONArray("buckets");
            for (Object obj : metrics){
                Map<String,Long> map = new HashMap<>();
                JSONObject item = (JSONObject) obj;
                String time = item.getString("key");
                JSONArray clusters = item.getJSONObject("cluster_id").getJSONArray("buckets");
                for(Object metricObj : clusters){
                    JSONObject objs = (JSONObject) metricObj;
                    JSONArray broker = objs.getJSONObject("metric_name").getJSONArray("buckets");
                    broker.forEach(bus->{
                        JSONObject bucket = (JSONObject) bus;
                        String metricName = bucket.getString("key");
                        JSONArray buckets = bucket.getJSONObject("broker").getJSONArray("buckets");
                        long sumValue = 0L;
                        for (Object xxx:buckets){
                            JSONObject xxxObj = (JSONObject) xxx;
                            long minData = xxxObj.getJSONObject(Constants.KeyStr.MIN_DATA).getLongValue(Constants.JsonObject.VALUE);
                            long maxData = xxxObj.getJSONObject(Constants.KeyStr.MAX_DATA).getLongValue(Constants.JsonObject.VALUE);
                            sumValue = sumValue + (maxData-minData);
                        }
                        if(map.containsKey(metricName)){
                            long valus = map.get(metricName);
                            map.put(metricName,valus+sumValue);
                        }else{
                            map.put(metricName,sumValue);
                        }
                    });
                }
                result.put(time,map);
            }


        }catch (Exception e){
            e.printStackTrace();
        }
        return  result;
    }


    public JSONObject searchES(String requestBody, String index) throws IOException {
        RestClient lowLevelClient = client.getLowLevelClient();
        String endpoint =  "/" +  index +  "/_search";
        Request request  = new  Request(HttpPost.METHOD_NAME,  endpoint);
        request.setEntity(new NStringEntity(requestBody, ContentType.APPLICATION_JSON));
        Response response  = lowLevelClient.performRequest(request);
        HttpEntity entity  = response.getEntity();
        if(entity != null)  {
            long len = entity.getContentLength();
            if (len != -1 && len < 2048) {
                return   JSON.parseObject(EntityUtils.toString(entity));
            }else {
                return JSON.parseObject(entity.getContent(),  JSONObject.class);
            }
        }
        return null;
    }

    public EleasticSearchTest(String hosts) {
        String[] addressArray = hosts.split(",", -1);
        HttpHost[] httpHosts = new HttpHost[addressArray.length];

        for (int i = 0; i < addressArray.length; i++) {
            String[] hostAndPort = addressArray[i].split(":", -1);
            httpHosts[i] = new HttpHost(hostAndPort[0], Integer.parseInt(hostAndPort[1]), "http");
        }
        client = new RestHighLevelClient(RestClient.builder(httpHosts));
    }

    public static void main(String[] args) {
        EleasticSearchTest test = new EleasticSearchTest("127.0.0.1:9200");
        long start = 1572658295394L;
        long end = 1573263095394L;
        Map<String,Long> countMap = test.summaryCountData(start,end);
        System.out.println("summary start");
        countMap.forEach((k,v)->{
            System.out.println(k+":"+v);
        });
        System.out.println("summary end");
        Map<String,Map<String,Long>> map = test.summaryTrends(start,end);
        System.out.println("summary trend start");
        Map<String,Long> res = new HashMap<>();
        map.keySet().stream().sorted((o1, o2) -> {
            long o1s = Long.parseUnsignedLong(o1);
            long o2s = Long.parseUnsignedLong(o2);
            return o1s>=o2s?1:-1;
        }).forEach(k->{
            map.get(k).forEach((m,v)->{
                System.out.println(k+":"+m+":"+v);
            });
        });
//        map.forEach((k,vmap)->{
//            vmap.forEach((m,v)->{
////                if(res.containsKey(m)){
////                    long value = res.get(m);
////                    res.put(m,value);
////                }else{
////                    res.put(m,v);
////                }
//                System.out.println(k+":"+m+":"+v);
//            });
//        });
//        res.forEach((k,v)->{
//            System.out.println(k+":"+v);
//        });
        System.out.println("summary trend end");

    }
}
