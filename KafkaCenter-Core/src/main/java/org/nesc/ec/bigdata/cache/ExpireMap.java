package org.nesc.ec.bigdata.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lg99
 */
public class ExpireMap {
    private static final Map<String, Object> CACHE_MAP = new ConcurrentHashMap<>();

    public static final long CACHE_HOLD_TIME_5MIN = 5*60*1000L;

    public static final String _EXPIRE = "_expire";

    public ExpireMap() {
    }

    public static void  put(String key, Object value){
        put(key,value,CACHE_HOLD_TIME_5MIN);
    }

    public static void put(String key,Object value,long expireTime){
        if(checkCacheName(key)){
            return;
        }
        CACHE_MAP.put(key,value);
        CACHE_MAP.put(key+_EXPIRE,System.currentTimeMillis()+expireTime);
    }

    public static boolean checkCacheName(String key){
        long expireTime = (long) CACHE_MAP.getOrDefault(key+_EXPIRE,0L);
        if(expireTime==0L){
            return false;
        }
        if(expireTime < System.currentTimeMillis()){
            remove(key);
            remove(key+_EXPIRE);
            return false;
        }
        return true;
    }

    public static Object get(String key){
        if(checkCacheName(key)){
            return CACHE_MAP.get(key);
        }
        return null;
    }

    public static void remove(String key){
        CACHE_MAP.remove(key);
        CACHE_MAP.remove(key+_EXPIRE);
    }

    public static void removeAll(){
         CACHE_MAP.clear();
    }
}
