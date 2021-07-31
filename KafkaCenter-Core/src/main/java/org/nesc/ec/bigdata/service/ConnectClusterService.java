package org.nesc.ec.bigdata.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.cache.ConnectCache;
import org.nesc.ec.bigdata.exception.ConnectorException;
import org.nesc.ec.bigdata.model.ConnectorJob;
import org.nesc.ec.bigdata.model.vo.ConnectorVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Objects;

@Service
public class ConnectClusterService {
    @Autowired
    RestService restService;


    public static final Logger LOGGER = LoggerFactory.getLogger(ConnectClusterService.class);

    public JSONArray connectorPlugins(String clusterId,String clusterName) throws ConnectorException {
        String body = sendGetOrDeleteRequest("/connector-plugins",ConnectCache.getConnectorUrlMap(clusterId,clusterName),HttpMethod.GET);
        if(Objects.nonNull(body)){
            return JSON.parseArray(body);
        }
        return new JSONArray();
    }

    public JSONArray selectConnectors(String url) throws ConnectorException {
        String body = sendGetOrDeleteRequest("/connectors",url,HttpMethod.GET);
        if(Objects.nonNull(body)){
            return  JSON.parseArray(body);
        }
        return new JSONArray();
    }

    public JSONArray connectorTask(String clusterId,String clusterName,String connectorName) throws ConnectorException {
        String url = "/connectors/"+connectorName+"/status";
        String body = sendGetOrDeleteRequest(url,ConnectCache.getConnectorUrlMap(clusterId,clusterName),HttpMethod.GET);
        if(Objects.nonNull(body)){
           JSONObject result = JSON.parseObject(body);
           JSONArray taskArray = result.getJSONArray("tasks");
           return taskArray;
        }
        return new JSONArray();
    }

    public String restartConnectorTask(String connectorName,String taskId,String clusterName,String clusterId) throws ConnectorException {
        String url = "/connectors/"+connectorName+"/tasks/"+taskId+"/restart";
        String host = ConnectCache.getConnectorUrlMap(clusterId,clusterName);
        return sendPostOrPutConnectorRequest(url,host,new JSONObject().toJSONString(),HttpMethod.POST);
    }

    public String restartConnectorJob(String connectorName,String clusterName,String clusterId) throws ConnectorException {
        String url = "/connectors/"+connectorName+"/restart";
        String host = ConnectCache.getConnectorUrlMap(clusterId,clusterName);
        return sendPostOrPutConnectorRequest(url,host,new JSONObject().toJSONString(),HttpMethod.POST);
    }

    public String pauseConnectorJob(String connectorName,String clusterName,String clusterId) throws ConnectorException {
        String url = "/connectors/"+connectorName+"/pause";
        String host = ConnectCache.getConnectorUrlMap(clusterId,clusterName);
        return sendPostOrPutConnectorRequest(url,host,new JSONObject().toJSONString(),HttpMethod.PUT);
    }


    public String resumeConnectorTask(String connectorName,String clusterName,String clusterId) throws ConnectorException {
        String url = "/connectors/"+connectorName+"/resume";
        String host = ConnectCache.getConnectorUrlMap(clusterId,clusterName);
        return sendPostOrPutConnectorRequest(url,host,new JSONObject().toJSONString(),HttpMethod.PUT);
    }

    public JSONArray validateConnector(ConnectorVo connectorVo) throws ConnectorException {
        JSONArray array = new JSONArray();
        ConnectorJob connectorJob = connectorVo.getConnectorJob();
        String url = "/connector-plugins/"+connectorVo.getConnectorJob().getClassName()+"/config/validate";
        JSONObject script = JSON.parseObject(connectorJob.getScript());
        String host = ConnectCache.getConnectorUrlMap(String.valueOf(connectorJob.getClusterId()),connectorVo.getClusterName());
        String result = sendPostOrPutConnectorRequest(url,host,connectorJob.getScript(),HttpMethod.PUT);
        if(Objects.nonNull(result)){
            JSONObject resultObj = JSON.parseObject(result);
            JSONArray configArrays = resultObj.getJSONArray("configs");
            for (int i = 0;i<configArrays.size();i++){
                JSONObject configObject = configArrays.getJSONObject(i);
                JSONObject definitionObject = configObject.getJSONObject("definition");
                String name = definitionObject.getString("name");
                String document = definitionObject.getString("documentation");
                boolean required = definitionObject.getBooleanValue("required");
                if(required){
                    if(!script.containsKey(name)){
                        JSONObject object = generateObject(name,document);
                        array.add(object);
                    }
                }

            }
        }
        return array;
    }



    public String createConnector(ConnectorVo connectorVo) throws ConnectorException {
        return editConnector(connectorVo,false);
    }

    public String updateConnector(ConnectorVo connectorVo) throws ConnectorException {
       return editConnector(connectorVo,true);

    }

    public String status(String clusterId,String clusterName,String connectorName) throws ConnectorException {
         String url = "connectors/"+connectorName+"/status";
        String host = ConnectCache.getConnectorUrlMap(clusterId,clusterName);
        String responseStatus =  sendGetOrDeleteRequest(url,host,HttpMethod.GET);
        JSONObject object = JSON.parseObject(responseStatus);
        JSONObject connectorObj = object.getJSONObject("connector");
        JSONArray taskArray = object.getJSONArray("tasks");
        for (int i = 0;i<taskArray.size();i++){
            JSONObject taskObj = taskArray.getJSONObject(i);
            String state = taskObj.getString("state");
            if("FAILED".equalsIgnoreCase(state)){
                return state;
            }
        }
        return connectorObj.getString("state");

    }

    public JSONObject status(String host,String connectorName) {
        String url = "connectors/"+connectorName+"/status";
        try{
            String responseStatus =  sendGetOrDeleteRequest(url,host,HttpMethod.GET);
            return JSON.parseObject(responseStatus);
        }catch (ConnectorException connectorException){
            LOGGER.error("status has error,url:{}",url,connectorException);
        }
        return  new JSONObject();

    }


    public JSONObject connectorConfig(String host,String connectorName){
        String url = "connectors/"+connectorName+"/config";
        try{
            String responseStatus =  sendGetOrDeleteRequest(url,host,HttpMethod.GET);
            JSONObject object = JSON.parseObject(responseStatus);
            return object;
        }catch (ConnectorException connectorException){
            LOGGER.error("status has error,url:{}",url,connectorException);
        }
        return new JSONObject();

    }

//    public static void main(String[] args) {
//        String jsonString = "{\"_index\":\"book_shop\",\"_type\":\"it_book\",\"_id\":\"1\",\"_score\":1.0," +
//                "\"_source\":{\"name\": \"Java编程思想（第4版）\",\"author\": \"[美] Bruce Eckel\",\"category\": \"编程语言\"," +
//                "\"price\": 109.0,\"publisher\": \"机械工业出版社\",\"date\": \"2007-06-01\",\"tags\": [ \"Java\", \"编程语言\" ]}}";
//        JSONObject object = JSON.parseObject(jsonString);
//        System.out.println(object.toString());
//    }

    public String deleteConnector(String clusterId,String clusterName,String connectorName) throws ConnectorException {
       String url = "/connectors/"+connectorName;
       String host = ConnectCache.getConnectorUrlMap(clusterId,clusterName);
        return sendGetOrDeleteRequest(url,host,HttpMethod.DELETE);

    }

    private String editConnector(ConnectorVo connectorVo,boolean isEdit) throws ConnectorException {
        ConnectorJob connectorJob = connectorVo.getConnectorJob();
        String url = "/connectors";
        String host = ConnectCache.getConnectorUrlMap(String.valueOf(connectorJob.getClusterId()),connectorVo.getClusterName());
        if(isEdit){
             url = url+"/"+connectorJob.getName() +"/config";
            return sendPostOrPutConnectorRequest(url,host,connectorVo.getConnectorJob().getScript(),HttpMethod.PUT);
        }
        return sendPostOrPutConnectorRequest(url,host,connectorVo.getConnectorJob().getScript(),HttpMethod.POST);
    }

    private JSONObject generateObject(String name,String document){
        JSONObject object = new JSONObject();
        object.put("name",name);
        object.put("document",document);
        return object;
    }

    public String descConnectorVersion(String url) throws ConnectorException {
        JSONObject resultObj = JSON.parseObject(sendGetOrDeleteRequest("/",url,HttpMethod.GET));
        return resultObj.getString("version");
    }



    private String sendGetOrDeleteRequest(String requestUrl,String host,HttpMethod httpMethod) throws ConnectorException {
        String[] urls = host.split(",");
        for (String url:urls){
            url = restService.generatorUrl(url,requestUrl);
            try{
                ResponseEntity<String> responseEntity = restService.sendGetOrDeleteRequest(url,null,null,httpMethod);
                return responseEntity.getBody();
            }catch (HttpClientErrorException e){
                LOGGER.error("fetch has error,",e);
                throw new ConnectorException("request has error",e);
            }

        }
        return null;
    }

    private String sendPostOrPutConnectorRequest(String requestUrl, String host, String script, HttpMethod httpMethod) throws ConnectorException {
        String[] urls = host.split(",");
        for (String url:urls){
            url = restService.generatorUrl(url,requestUrl);
            try{
                ResponseEntity<String> responseEntity= restService.sendPostOrPutRequest(url,script,null,httpMethod);
                return responseEntity.getBody();
            }catch (HttpClientErrorException e){
                LOGGER.error("fetch has error,",e);
                throw new ConnectorException("request has error",e);
            }

        }
        return null;

    }





}
