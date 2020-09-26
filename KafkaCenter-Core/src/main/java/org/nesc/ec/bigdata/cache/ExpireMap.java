package org.nesc.ec.bigdata.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lg99
 * expire map
 */
public class ExpireMap {
    private static final Map<String, Object> CACHE_MAP = new ConcurrentHashMap<>();

    /** expire time for Maps.key or Maps.values*/
    public static final long CACHE_HOLD_TIME_5MIN = 5*60*1000L;
    /** A single key is used to store the expiration time of the key */
    public static final String EXPIRE_ = "_expire";

    public ExpireMap() {
    }

    /** put key,value to CacheMap */
    public static void  put(String key, Object value){
        put(key,value,CACHE_HOLD_TIME_5MIN);
    }

    /**
     * put key,value,expire time to cacheMap
     * */
    public static void put(String key,Object value,long expireTime){
        //check key whether exits in cacheMap
        if(checkCacheName(key)){
            return;
        }
        CACHE_MAP.put(key,value);
        CACHE_MAP.put(key+ EXPIRE_,System.currentTimeMillis()+expireTime);
    }

    /** check key whether exits in cacheMap,
     * 1.if not exits,return false
     * 2.if exits,the expireTime less than the current Time,remove the key and the expire key in cacheMap,return false
     * 3.else return true
     * */
    public static boolean checkCacheName(String key){
        long expireTime = (long) CACHE_MAP.getOrDefault(key+ EXPIRE_,0L);
        if(expireTime==0L){
            return false;
        }
        if(expireTime < System.currentTimeMillis()){
            remove(key);
            remove(key+ EXPIRE_);
            return false;
        }
        return true;
    }

    /**
     * get the key from the cacheMap
     * if key exits in cacheMap,return map.value
     * else return null
     * */
    public static Object get(String key){
        if(checkCacheName(key)){
            return CACHE_MAP.get(key);
        }
        return null;
    }

    /**
     *  remove the key and the expire key in cacheMap
     * */
    public static void remove(String key){
        CACHE_MAP.remove(key);
        CACHE_MAP.remove(key+ EXPIRE_);
    }

    /** clear the cacheMap */
    public static void removeAll(){
         CACHE_MAP.clear();
    }
}
