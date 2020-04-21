package org.nesc.ec.bigdata.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.mapper.KsqlClusterInfoMapper;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.nesc.ec.bigdata.model.KsqlClusterInfo;
import org.nesc.ec.bigdata.model.KsqlInfo;
import org.nesc.ec.bigdata.model.WebSocketMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.websocket.Session;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Truman.P.Du
 * @date 2020/02/27
 * @description
 */
@Service
public class KsqlService {

    private final static Logger LOGGER = LoggerFactory.getLogger(KsqlService.class);

    @Autowired
    ClusterService clusterService;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    KsqlClusterInfoMapper ksqlClusterInfoMapper;

    /**
     * Map<clusterName|ksqlServerId, url>
     */
    private Map<String, String> ksqlMapUrl = new HashMap<>();

    private Map<String, Long> ksqlQueryMap = new ConcurrentHashMap<>();

    public List<KsqlInfo> getKsqlList() {
        List<KsqlInfo> list = new ArrayList<>();
        List<KsqlClusterInfo> ksqlClusters = getTotalData();
        ksqlClusters.forEach(ksqlClusterInfo -> {
            String ksqlServiceId = ksqlClusterInfo.getKsqlServerId();
            String clusterName = ksqlClusterInfo.getClusterName();
            String version = ksqlClusterInfo.getVersion();
            String ksqlUrl = ksqlClusterInfo.getKsqlUrl();
            String[] urls = ksqlUrl.split(",");
            KsqlInfo ksqlInfo = new KsqlInfo(ksqlServiceId, clusterName, version,ksqlUrl);
            KsqlInfo ksql = null;
            for (String url :urls){
                ksql = setHealthyInfo(url, ksqlInfo);
                String key = clusterName + "|" + ksqlServiceId;
                ksqlMapUrl.put(key, url);
                if(ksql!=null){
                    ksqlInfo = ksql;
                }
            }
            list.add(ksqlInfo);

        });
        return list;
    }

    private JSONObject getKsqlServer(String url, String method) {
        return restTemplate.getForEntity(generatorUrl(url, method), JSONObject.class).getBody();
    }

    private KsqlInfo setHealthyInfo(String url, KsqlInfo ksqlInfo) {
        try {
            JSONObject healthyInfo = this.getKsqlServer(url, "healthcheck");
            boolean ksqlHealthy = healthyInfo.getBooleanValue("isHealthy");
            ksqlInfo.setKsqlHealthy(ksqlHealthy);
            boolean kafkaHealthy = healthyInfo.getJSONObject("details").getJSONObject("kafka").getBooleanValue("isHealthy");
            ksqlInfo.setKafkaHealthy(kafkaHealthy);
            boolean metastoreHealthy = healthyInfo.getJSONObject("details").getJSONObject("metastore").getBooleanValue("isHealthy");
            ksqlInfo.setMetastoreHealthy(metastoreHealthy);
        } catch (Exception e) {
            LOGGER.warn("get ksql health error.", e.getMessage());
            return null;
        }
        return ksqlInfo;
    }

    public void executeConsole(WebSocketMessage message, Session session) {
        if ("query".equalsIgnoreCase(message.getType())) {
            new Thread(() -> query(message, session)).start();

        } else {
            String result = executeKsql(message.getMessage(), message.getId());
            try {
                if (result == null) {
                    result = "{\"message\":\"ksql execute error.please check you sql.\"}";
                }
                session.getBasicRemote().sendText(result);
            } catch (IOException e) {
                LOGGER.warn("executeConsole error :", e);
            }
        }

    }

    public JSONArray showStreamOrTables(String ksqlServerId, String clusterName, String showWhat) {
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        JSONArray streamOrTables = null;
        JSONObject jsonObject = new JSONObject();
        String ksqlID = clusterName + "|" + ksqlServerId;
        if ("streams".equalsIgnoreCase(showWhat)) {
            jsonObject.put("ksql", "LIST STREAMS;");
        }
        if ("tables".equalsIgnoreCase(showWhat)) {
            jsonObject.put("ksql", "LIST TABLES;");
        }
        if ("queries".equalsIgnoreCase(showWhat)) {
            jsonObject.put("ksql", "LIST QUERIES;");
        }
        String bodyString = executeKsql(jsonObject.toJSONString(), ksqlID);
        JSONArray body = JSONObject.parseArray(bodyString);
        if ("streams".equalsIgnoreCase(showWhat)) {
            streamOrTables = body.getJSONObject(0).getJSONArray("streams");
            JSONArray jsonArray = describeStream(ksqlID, streamOrTables);
            streamOrTables = jsonArray;
        }
        if ("tables".equalsIgnoreCase(showWhat)) {
            streamOrTables = body.getJSONObject(0).getJSONArray("tables");
            JSONArray jsonArray = describeTable(ksqlID, streamOrTables);
            streamOrTables = jsonArray;
        }
        if ("queries".equalsIgnoreCase(showWhat)) {
            streamOrTables = body.getJSONObject(0).getJSONArray("queries");
            JSONArray jsonArray = explainQuery(ksqlID, streamOrTables);
            streamOrTables = jsonArray;
        }
        return streamOrTables;
    }


    public JSONObject drop(String ksqlServerId, String name, String clusterName, String dropWhat) {
        JSONObject jsonObject = new JSONObject();
        String ksqlID = clusterName + "|" + ksqlServerId;
        if ("streams".equalsIgnoreCase(dropWhat)) {
            jsonObject.put("ksql", "DROP STREAM " + name + ";");
        }
        if ("tables".equalsIgnoreCase(dropWhat)) {
            jsonObject.put("ksql", "DROP TABLE " + name + ";");
        }
        String bodyString = executeKsql(jsonObject.toJSONString(), ksqlID);
        JSONObject body = JSONObject.parseArray(bodyString).getJSONObject(0);
        return body;
    }

    public JSONObject terminateQuery(String ksqlServerId, String clusterName, String queryId) {
        JSONObject jsonObject = new JSONObject();
        String ksqlID = clusterName + "|" + ksqlServerId;
        jsonObject.put("ksql", "TERMINATE " + queryId + ";");
        JSONObject body = (JSONObject) JSONObject.parseArray(executeKsql(jsonObject.toJSONString(), ksqlID)).get(0);
        return body;
    }

    private JSONObject getKsqlInfo(String url){
        return restTemplate.getForEntity(generatorUrl(url, "/info"), JSONObject.class).getBody();
   }
    public Boolean addKsql(Long clusterId , String ksqlAddress){
        boolean result = false;
        ClusterInfo clusterInfo = clusterService.selectById(clusterId);
        
        String clusterName = clusterInfo.getName();
        if (ksqlAddress != null && ksqlAddress.trim().length() > 0) {
            String[] urls = ksqlAddress.split(",");
            String ksqlServiceId = "";
           String version = "";
            for (String url :urls){
                JSONObject ksqlInfo = getKsqlInfo(url);
                ksqlServiceId = ksqlInfo.getJSONObject("KsqlServerInfo").getString("ksqlServiceId");
                version = ksqlInfo.getJSONObject("KsqlServerInfo").getString("version");
            }
            KsqlClusterInfo ksqlClusterInfo = ksqlClusterInfoMapper.selectKsqlInfo(clusterId,ksqlServiceId);
            if(ksqlClusterInfo==null){
                KsqlClusterInfo ksqlCluster = new KsqlClusterInfo();
                ksqlCluster.setClusterId(clusterId);
                ksqlCluster.setClusterName(clusterName);
                ksqlCluster.setKsqlUrl(ksqlAddress);
                ksqlCluster.setKsqlServerId(ksqlServiceId);
                ksqlCluster.setVersion(version);
                result = insert(ksqlCluster);
            }
        }
        return result;
    }
    public Boolean delKsql(String clusterName , String ksqlServiceId){
        Map<String, Object> map = new HashMap<>();
        map.put("cluster_name", clusterName);
        map.put("ksql_serverId", ksqlServiceId);
        return checkResult(ksqlClusterInfoMapper.deleteByMap(map));
    }
    /**
     * 执行ksql（不包含query）语句
     *
     * @param message
     * @param id
     * @return
     */
    private String executeKsql(String message, String id) {
        String ksqlUrl = ksqlMapUrl.get(id);

        String body = null;
        InputStream is = null;
        try {
            URL url = new URL(generatorUrl(ksqlUrl, "/ksql"));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
            connection.setRequestProperty("Charset", "UTF-8");
            connection.addRequestProperty(Constants.KeyStr.CONTENT_TYPE, Constants.KeyStr.KSQL_APPLICATION_JSON);
            connection.connect();
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            if (!"".equals(message)) {
                out.writeBytes(message);
            }
            out.flush();
            out.close();
            is = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuffer sb=new StringBuffer();
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            body = sb.toString();

        } catch (Exception e) {
            body = "{\"message\":\"ksql execute error.please check you sql.\"}";
            LOGGER.warn("ksql execute error.", e);
        }
        return body;
    }


    private static StringBuffer arrayToString(String source, StringBuffer sBuffer) {
        if (sBuffer.length() > 0) {
            sBuffer.append(",").append(source);
        } else {
            sBuffer.append(source);
        }
        return sBuffer;
    }



    /**
     * 执行query语句
     *
     * @param message
     * @param session
     */
    private void query(WebSocketMessage message, Session session) {
        ksqlQueryMap.put(session.getId(), System.currentTimeMillis());
        String ksqlUrl = ksqlMapUrl.get(message.getId());
        InputStream eventStream = null;
        try {
            URL url = new URL(generatorUrl(ksqlUrl, "/query"));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
            connection.setRequestProperty("Charset", "UTF-8");
            connection.addRequestProperty(Constants.KeyStr.CONTENT_TYPE, Constants.KeyStr.KSQL_APPLICATION_JSON);
            connection.connect();
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            if (!"".equals(message.getMessage())) {
                out.writeBytes(message.getMessage());
            }
            out.flush();
            out.close();
            eventStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(eventStream));

            String line = "";
            while ((line = reader.readLine()) != null && isRunQuery(session.getId())) {
                if (org.apache.commons.lang3.StringUtils.isNotBlank(line)) {
                    try {
                        session.getBasicRemote().sendText(line);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("ksql query error:Unable to get status event stream", e);
        } finally {
            if (eventStream != null) {
                try {
                    eventStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 检查是否持续运行query,最长运行1个小时，会自动关闭查询任务
     *
     * @param sessionId
     * @return
     */
    private boolean isRunQuery(String sessionId) {
        long currentTime = System.currentTimeMillis();
        if (ksqlQueryMap.containsKey(sessionId) && (currentTime - ksqlQueryMap.get(sessionId)) < 1000 * 60 * 60) {
            return true;
        } else {
            ksqlQueryMap.remove(sessionId);
            return false;
        }
    }

    public boolean stopQuery(Session session) {
        try {
            ksqlQueryMap.remove(session.getId());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String generatorUrl(String prefix, String suffix) {
        if (!prefix.startsWith("http:")) {
            prefix = "http://" + prefix;
        }

        if (!prefix.endsWith(suffix)) {
            prefix = prefix + suffix;
        }
        return prefix;
    }

    private JSONArray describeStream(String ksqlID, JSONArray streamOrTables) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < streamOrTables.size(); i++) {
            JSONObject stream = (JSONObject) streamOrTables.get(i);
            JSONObject getStreamName = new JSONObject();
            String streamName = (String) stream.get("name");
            getStreamName.put("ksql", "DESCRIBE EXTENDED " + streamName + ";");
            getStreamName.put("id", ksqlID);
            JSONObject describeStream = (JSONObject) JSONObject.parseArray(executeKsql(getStreamName.toJSONString(), ksqlID)).get(0);
            JSONArray fields = (JSONArray) describeStream.getJSONObject("sourceDescription").get("fields");
            Integer partitions = (Integer) describeStream.getJSONObject("sourceDescription").get("partitions");
            Integer replication = (Integer) describeStream.getJSONObject("sourceDescription").get("replication");
            stream.put("fields", fields);
            stream.put("partitions", partitions);
            stream.put("replication", replication);
            jsonArray.add(stream);
        }
        return jsonArray;
    }

    private JSONArray describeTable(String ksqlID, JSONArray streamOrTables) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < streamOrTables.size(); i++) {
            JSONObject table = (JSONObject) streamOrTables.get(i);
            JSONObject getTableName = new JSONObject();
            String tableName = (String) table.get("name");
            getTableName.put("ksql", "DESCRIBE EXTENDED " + tableName + ";");
            getTableName.put("id", ksqlID);
            JSONObject describeTable = (JSONObject) JSONObject.parseArray(executeKsql(getTableName.toJSONString(), ksqlID)).get(0);
            JSONArray fields = (JSONArray) describeTable.getJSONObject("sourceDescription").get("fields");
            Integer partitions = (Integer) describeTable.getJSONObject("sourceDescription").get("partitions");
            Integer replication = (Integer) describeTable.getJSONObject("sourceDescription").get("replication");
            table.put("fields", fields);
            table.put("partitions", partitions);
            table.put("replication", replication);
            jsonArray.add(table);
        }
        return jsonArray;
    }

    private JSONArray explainQuery(String ksqlID, JSONArray streamOrTables) {
        JSONArray jsonArray = new JSONArray();
        if (streamOrTables.size() != 0 || streamOrTables != null) {
            for (int i = 0; i < streamOrTables.size(); i++) {
                JSONObject query = (JSONObject) streamOrTables.get(i);
                String queryStringNoEnter = ((String) query.get("queryString")).replaceAll("\\n", " ");
                query.put("queryString", queryStringNoEnter);
                JSONObject getQueryId = new JSONObject();
                String queryId = (String) query.get("id");
                getQueryId.put("ksql", "EXPLAIN " + queryId + ";");
                getQueryId.put("id", ksqlID);
                JSONObject explainQuery = (JSONObject) JSONObject.parseArray(executeKsql(getQueryId.toJSONString(), ksqlID)).get(0);
                JSONArray arraySource = (JSONArray) explainQuery.getJSONObject("queryDescription").get("sources");
                JSONArray fields = (JSONArray) explainQuery.getJSONObject("queryDescription").get("fields");
                String state = (String) explainQuery.getJSONObject("queryDescription").get("state");
                StringBuffer sBuffer = new StringBuffer();
                arraySource.stream().forEach(obj -> arrayToString((String) obj, sBuffer));
                query.put("source", sBuffer);
                query.put("state", state);
                query.put("fields", fields);
                jsonArray.add(query);
            }
        }
        return jsonArray;
    }

    public boolean insert(KsqlClusterInfo ksqlClusterInfo) {
        Integer result =  ksqlClusterInfoMapper.insert(ksqlClusterInfo);
        return checkResult(result);
    }
    public boolean update(KsqlClusterInfo ksqlClusterInfo) {
        Integer result = ksqlClusterInfoMapper.updateById(ksqlClusterInfo);
        return checkResult(result);
    }
    public boolean delete(Long id) {
        Integer result = ksqlClusterInfoMapper.deleteById(id);
        return checkResult(result);
    }
    public KsqlClusterInfo selectById(Long id) {
        return ksqlClusterInfoMapper.selectById(id);
    }
    public KsqlClusterInfo selectByClusterId(Long clusterId){
        return ksqlClusterInfoMapper.selectByClusterId(clusterId);
    }

    public List<KsqlClusterInfo> getTotalData() {
        return ksqlClusterInfoMapper.selectList(null);
    }

    public boolean checkResult(Integer result) {
        return result>0?true:false;
    }

}