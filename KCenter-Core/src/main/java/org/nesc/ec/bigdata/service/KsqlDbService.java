package org.nesc.ec.bigdata.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import org.nesc.ec.bigdata.cache.ConnectCache;
import org.nesc.ec.bigdata.exception.KSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class KsqlDbService {

    public static final Logger LOGGER = LoggerFactory.getLogger(KsqlDbService.class);

    @Autowired
    RestService restService;

    public String executeKsqlScript(String host,String body)throws KSQLException{
        try{
            String url = restService.generatorUrl(host,"/ksql");
            ResponseEntity<String> responseEntity = sendRequest(url,HttpMethod.POST,buildQuery(body,true));
            ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
            List<String> messageArray =  JSON.parseArray(responseEntity.getBody(), String.class);
            if(Objects.nonNull(messageArray) && !CollectionUtils.isEmpty(messageArray)){
                JSONObject message = JSON.parseObject(messageArray.get(0));
                if(message.containsKey("commandStatus")){
                    return message.getJSONObject("commandStatus").getString("message");
                }
                if(message.containsKey("sourceDescription")){
                    return message.getString("sourceDescription");
                }
            }
            return "";
        }catch (HttpClientErrorException e){
            ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
            JSONObject message = JSON.parseObject(e.getResponseBodyAsString());
            LOGGER.error("execute ksql script has error,",e);
            throw  new KSQLException(message.getString("message"));
        }
    }

    public String executeQueryScript(String host, String body,boolean isKsqlQuery) throws KSQLException {
        try{
            String  query = isKsqlQuery?"/ksql":"/query";
            String url = restService.generatorUrl(host,query);
            ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
            ResponseEntity<String> responseEntity = sendRequest(url,HttpMethod.POST,buildQuery(body,true));
            return responseEntity.getBody();
        }catch (HttpClientErrorException e){
            LOGGER.error("execute ksql sql script has error,",e);
           JSONObject message = JSONObject.parseObject(e.getResponseBodyAsString());
           throw new KSQLException(message.getString("message"));
        }
    }

    public JSONObject describeStreamOrTable(String serverId,String clusterName,String name) throws KSQLException {
        String ksql = "DESCRIBE  " + name +" ;";
        try{
            return JSON.parseObject(this.executeKsqlScript( ConnectCache.KSQL_URL_MAP.get(clusterName+"|"+serverId),ksql));
        }catch (KSQLException e){
            throw e;
        }
    }

    public String dropStreamOrTable(String serverId,String clusterName,String name,boolean isStream) throws KSQLException {
        String ksql = isStream? "DROP STREAM "+name+";":"DROP TABLE "+name+";";
        return this.executeKsqlScript( ConnectCache.KSQL_URL_MAP.get(clusterName+"|"+serverId),ksql);
    }

    public List<JSONObject> selectStreamOrTable(String serverId, String clusterName, String name) throws KSQLException {
        String ksql = "SELECT * FROM " + name.toUpperCase() +" EMIT CHANGES LIMIT 10;";
        String responseData =  this.executeQueryScript(ConnectCache.KSQL_URL_MAP.get(clusterName+"|"+serverId),ksql,false);
        JSONArray resArray = JSON.parseArray(responseData);
        List<JSONObject> data = new ArrayList<>();
        String schema = "";
        String[] schemas = new String[0];
        for (int i = 0;i<resArray.size();i++){
            if(i==0){
                JSONObject schemaObject = resArray.getJSONObject(i);
                schema = schemaObject.getJSONObject("header").getString("schema");
                schemas = splitSchema(schema);
                continue;
            }
            JSONObject columnObject = resArray.getJSONObject(i);
            JSONObject rowsObject = columnObject.containsKey("row")?columnObject.getJSONObject("row"):new JSONObject();
            String[] columnData =  rowsObject.getObject("columns",String[].class);
            JSONObject columnObj = transformColumnData(columnData,schemas);
            if(!CollectionUtils.isEmpty(columnObj.keySet())){
                data.add(columnObj);
            }

        }
        return data;
    }

    private String[] splitSchema(String schema){
        String[] schemas = schema.split(",");
        String[] str = new String[schemas.length];
        for (int i = 0;i<schemas.length;i++){
            int index = schemas[i].indexOf("`");
            int lastIndex = schemas[i].lastIndexOf("`");
            String column = schemas[i].substring(index+1,lastIndex);
            str[i] = column;
        }
        return str;
    }

    private JSONObject transformColumnData(String[] columnData,String[] schema){
        JSONObject object = new JSONObject();
        if(!Objects.isNull(columnData)){
            for (int i = 0;i<columnData.length;i++){
                object.put(schema[i],columnData[i]);
            }
        }

        return object;
    }



    private String buildQuery(String command,boolean isKql){
        JSONObject object = new JSONObject();
        String jsonKey  = "sql";
        if (isKql){
            jsonKey = "ksql";
        }
        object.put(jsonKey,command);
        object.put("streamsProperties",new JSONObject());
        return object.toJSONString();
    }


    public ResponseEntity<String> sendRequest(String url, HttpMethod httpMethod,String params){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept","application/vnd.ksql.v1+json");
        ResponseEntity<String> restResponse = null;
        if(HttpMethod.GET.equals(httpMethod)){
           restResponse =  restService.sendGetRequest(url,params,httpHeaders);
        }else if(HttpMethod.POST.equals(httpMethod)){
            restResponse = restService.sendPostRequest(url,params,httpHeaders);
        }
        return restResponse;
    }
}
