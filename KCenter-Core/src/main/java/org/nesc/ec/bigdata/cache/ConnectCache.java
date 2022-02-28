package org.nesc.ec.bigdata.cache;

import org.nesc.ec.bigdata.constant.Constants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectCache {

    private ConnectCache() {
    }

    public static final  Map<String, String> KSQL_URL_MAP = new ConcurrentHashMap<>();

    public static final Map<String,String> CONNECTOR_URL_MAP = new ConcurrentHashMap<>();

    public static String getKsqlUrl(String ksqlServerId,String clusterName){
        return KSQL_URL_MAP.getOrDefault(clusterName+"|"+ksqlServerId,"");
    }

    public static String getConnectorUrlMap(String clusterId,String name){
        return  CONNECTOR_URL_MAP.getOrDefault(clusterId+ Constants.Symbol.VERTICAL_STR+name,"");
    }
}
