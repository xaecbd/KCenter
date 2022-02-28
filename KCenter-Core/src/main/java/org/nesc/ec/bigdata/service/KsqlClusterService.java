package org.nesc.ec.bigdata.service;

import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.cache.ConnectCache;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.mapper.KsqlClusterInfoMapper;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.nesc.ec.bigdata.model.KsqlClusterInfo;
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
public class KsqlClusterService {

    private final static Logger LOGGER = LoggerFactory.getLogger(KsqlClusterService.class);

    @Autowired
    ClusterService clusterService;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    KsqlClusterInfoMapper ksqlClusterInfoMapper;
    @Autowired
    KsqlStreamService ksqlStreamService;
    @Autowired
    KsqlTableService ksqlTableService;

    /**
     * Map<clusterName|ksqlServerId, url>
     */
    private Map<String, String> ksqlMapUrl = ConnectCache.KSQL_URL_MAP;

    private Map<String, Long> ksqlQueryMap = new ConcurrentHashMap<>();

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


    public List<KsqlClusterInfo> getKsqlClusterByTeams(List<Long> teamIds){
        return ksqlClusterInfoMapper.getKsqlByTeamIDs(teamIds);
    }


    private JSONObject getKsqlInfo(String url) {
        return restTemplate.getForEntity(generatorUrl(url, "/info"), JSONObject.class).getBody();
    }

    public Boolean addKsql( KsqlClusterInfo ksqlClusterInfo) {
        long clusterId = ksqlClusterInfo.getClusterId();
        String ksqlAddress = ksqlClusterInfo.getKsqlUrl();
        boolean result = false;
        ClusterInfo clusterInfo = clusterService.selectById(clusterId);

        String clusterName = clusterInfo.getName();
        if (ksqlAddress != null && ksqlAddress.trim().length() > 0) {
            String[] urls = ksqlAddress.split(",");
            String ksqlServiceId = "";
            String version = "";
            for (String url : urls) {
                JSONObject ksqlInfo = getKsqlInfo(url);
                ksqlServiceId = ksqlInfo.getJSONObject("KsqlServerInfo").getString("ksqlServiceId");
                version = ksqlInfo.getJSONObject("KsqlServerInfo").getString("version");
            }
            KsqlClusterInfo sql_ksqlClusterInfo = ksqlClusterInfoMapper.selectKsqlInfo(clusterId, ksqlServiceId);
            if (sql_ksqlClusterInfo == null) {
                KsqlClusterInfo ksqlCluster = new KsqlClusterInfo();
                ksqlCluster.setClusterId(clusterId);
                ksqlCluster.setClusterName(clusterName);
                ksqlCluster.setKsqlUrl(ksqlAddress);
                ksqlCluster.setKsqlServerId(ksqlServiceId);
                ksqlCluster.setVersion(version);
                ksqlCluster.setTeamIds(ksqlClusterInfo.getTeamIds());
                result = insert(ksqlCluster);
            }
        }
        return result;
    }

    public Boolean delKsql(String clusterName, String ksqlServiceId,String id) {
        Map<String, Object> map = new HashMap<>();
        map.put("cluster_name", clusterName);
        map.put("ksql_serverId", ksqlServiceId);


        boolean flag =  checkResult(ksqlClusterInfoMapper.deleteByMap(map));
        ksqlStreamService.deleteStreamByCluster(Long.parseLong(id));
        ksqlTableService.deleteStreamByCluster(Long.parseLong(id));
        return flag;
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
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            connection = buildHttpURLConnection(generatorUrl(ksqlUrl, "/ksql"));
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            if (!"".equals(message)) {
                out.writeBytes(message);
            }
            out.flush();
            out.close();
            is = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            body = sb.toString();


        } catch (Exception e) {
            body = "{\"message\":\"ksql execute error.please check you sql.\"}";
            LOGGER.warn("ksql execute error.", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.error("", e);
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.error("", e);
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return body;
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
        BufferedReader reader = null;
        HttpURLConnection connection = null;
        try {
            connection = buildHttpURLConnection(generatorUrl(ksqlUrl, "/query"));
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            if (!"".equals(message.getMessage())) {
                out.writeBytes(message.getMessage());
            }
            out.flush();
            out.close();
            eventStream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(eventStream));
            String line = "";
            while ((line = reader.readLine()) != null && isRunQuery(session.getId())) {
                if (org.apache.commons.lang3.StringUtils.isNotBlank(line)) {
                    try {
                        if (line.startsWith("[")){
                            line = line.substring(1,line.length());
                        }
                        if (line.endsWith("]")){
                            line = line.substring(0,line.length()-1);
                        }
                        if (line.endsWith(",")){
                            line = line.substring(0,line.length()-1);
                        }
                        session.getBasicRemote().sendText(line);
                    } catch (IOException e) {
                        LOGGER.error("", e);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("ksql query error:Unable to get status event stream", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.error("", e);
                }
            }
            if (eventStream != null) {
                try {
                    eventStream.close();
                } catch (IOException e) {
                    LOGGER.error("", e);
                }
            }
            if (connection != null) {
                connection.disconnect();
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

    public String generatorUrl(String prefix, String suffix) {
        if (!prefix.startsWith("http:")) {
            prefix = "http://" + prefix;
        }

        if (!prefix.endsWith(suffix)) {
            prefix = prefix + suffix;
        }
        return prefix;
    }


    public boolean insert(KsqlClusterInfo ksqlClusterInfo) {
        Integer result = ksqlClusterInfoMapper.insert(ksqlClusterInfo);
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

    public KsqlClusterInfo selectByClusterId(Long clusterId) {
        return ksqlClusterInfoMapper.selectByClusterId(clusterId);
    }

    public List<KsqlClusterInfo> getTotalData() {
        return ksqlClusterInfoMapper.selectList(null);
    }

    public boolean checkResult(Integer result) {
        return result > 0 ? true : false;
    }

    /**
     * 构建 HttpURLConnection
     * @param urlString
     * @return
     * @throws Exception
     */
    private HttpURLConnection buildHttpURLConnection(String urlString) throws IOException{
        HttpURLConnection connection = null;
        URL url = new URL(urlString);
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
        connection.setRequestProperty("Charset", "UTF-8");
        connection.addRequestProperty("Accept","application/vnd.ksql.v1+json");
        connection.addRequestProperty(Constants.KeyStr.CONTENT_TYPE, Constants.KeyStr.KSQL_APPLICATION_JSON);
        connection.connect();
        return connection;
    }

}