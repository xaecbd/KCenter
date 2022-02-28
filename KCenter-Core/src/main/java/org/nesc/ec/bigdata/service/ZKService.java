package org.nesc.ec.bigdata.service;

import org.nesc.ec.bigdata.common.util.ZKUtil;
import org.nesc.ec.bigdata.mapper.ClusterInfoMapper;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author Truman.P.Du
 * @version 1.0
 * @date 2019年4月12日 上午11:05:10
 */
@Service
public class ZKService {
    @Autowired
    TopicInfoService topicInfoService;

    @Autowired
    ClusterInfoMapper clusterInfoMapper;

    private Map<String, ZKUtil> cacheZKMap = new HashMap<>(30);

    private static final Logger LOGGER = LoggerFactory.getLogger(ZKService.class);

    /**
     * 当cluster 修改后需要调用该方法重建对应cluster的链接
     */
    public void updateZKAdminByClusterID(String clusterId) {
        synchronized (cacheZKMap) {
            ClusterInfo cluster = clusterInfoMapper.selectById(clusterId);
            if (cacheZKMap.containsKey(clusterId)) {
                try {
                    cacheZKMap.get(clusterId).close();
                } catch (Exception e) {
                    LOGGER.error("close kafka cluster:{} con error ", clusterId, e);
                }
            }
            this.cacheZKMap.put(clusterId, new ZKUtil(cluster.getZkAddress()));
        }
    }

    public ZKUtil getZK(String clusterId) {
        synchronized (cacheZKMap) {
            ClusterInfo cluster = clusterInfoMapper.selectById(clusterId);
            if (!cacheZKMap.containsKey(clusterId)) {
                this.cacheZKMap.put(clusterId, new ZKUtil(cluster.getZkAddress()));
            }
            return this.cacheZKMap.get(clusterId);
        }
    }

    public boolean checkZKAddressHealth(String zkAddress) {
        boolean flag = false;
        FutureTask<Object> future = new FutureTask<>(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                ZKUtil zkUtil = null;
                try {
                    zkUtil = new ZKUtil(zkAddress);
                } catch (Exception e) {
                    LOGGER.warn("connect zookeeper error.", e);
                }
                return zkUtil;
            }
        });
        new Thread(future).start();
        ZKUtil client = null;
        try {
            client = (ZKUtil) future.get(3, TimeUnit.SECONDS);
            if (client != null) {
                flag = true;
            }
        } catch (Exception e) {
            LOGGER.warn("checkZKAddressHealth  result is inactive", e);
        } finally {
            try {
                if (client != null) {
                    client.close();
                }
            } catch (Exception e) {
            }
        }
        return flag;
    }

    public boolean checkServiceHealth(String zkAddress) {
        boolean flag = false;
        ZKUtil zkUtil = null;
        try {
            zkUtil = new ZKUtil(zkAddress, 1000 * 20, 1000 * 20);
            flag = true;
        } catch (Exception e) {
            LOGGER.warn("connect zk error.", e);
        } finally {
            try {
                if (zkUtil != null) {
                    zkUtil.close();
                }
            } catch (IOException e) {
            }
        }
        return flag;
    }

    /**
     * zookeeper delete group
     *
     * @param clusterId     clusterId
     * @param consumerGroup group
     */
    public void deleteGroup(String clusterId, String consumerGroup) {
        ZKUtil zkUtil = this.getZK(clusterId);
        zkUtil.deleteGroup(consumerGroup);
    }

    public ZKUtil getZKUtilByZKAddress(String zkAddress) {
        return new ZKUtil(zkAddress);
    }

}
