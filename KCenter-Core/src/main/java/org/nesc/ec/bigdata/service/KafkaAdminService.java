package org.nesc.ec.bigdata.service;

import org.apache.kafka.clients.CommonClientConfigs;
import org.nesc.ec.bigdata.common.util.KafkaAdmins;
import org.nesc.ec.bigdata.mapper.ClusterInfoMapper;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@Service		 
public class KafkaAdminService {


	@Autowired
	TopicInfoService topicInfoService;

	@Autowired
	ClusterInfoMapper clusterInfoMapper;


	private Map<String, KafkaAdmins> cacheKafkaMap  =  new HashMap<>( 1 << 4) ;

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaAdminService.class);


	/**
	 * 当cluster 修改后需要调用该方法重建对应cluster的链接
	 */
	public void updateKafkaAdminByClusterID(String clusterId) {
		synchronized (cacheKafkaMap) {
			ClusterInfo cluster =  clusterInfoMapper.selectById(clusterId);
			if(cacheKafkaMap.containsKey(clusterId)) {
				try {
					cacheKafkaMap.get(clusterId).close();
				} catch (Exception e) {
					LOGGER.error("close kafka cluster:{} con error ", clusterId, e);
				}
			} 
			// 重新创建到kafka的con
			Properties props = new Properties();
			props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, cluster.getBroker());
			this.cacheKafkaMap.put(clusterId, new KafkaAdmins(props));
		}
	}


	public KafkaAdmins getKafkaAdmins (String clusterId)  {
		synchronized (cacheKafkaMap) {
			ClusterInfo cluster =  clusterInfoMapper.selectById(clusterId);
			if(!cacheKafkaMap.containsKey(clusterId)) {
				Properties props = new Properties();
				props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, cluster.getBroker());
				this.cacheKafkaMap.put(clusterId, new KafkaAdmins(props));
			} 
			return  this.cacheKafkaMap.get(clusterId);
		}
	}

	public boolean kafkaIsHeath(String brokerAddr) {
		boolean flag = false;
		KafkaAdmins admin = null;
		try {
			Properties props = new Properties();
			props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, brokerAddr);
		    admin = new KafkaAdmins(props);
			if(admin!=null) {
				flag = true;
			}
		} catch (Exception e) {
			LOGGER.warn("connect kafka error.",e);
		}finally {
			try {
				if(admin!=null) {
					admin.close();
                }
			} catch (IOException e) {}
		}
		return flag;

	}

	public boolean validateKafkaAddress(String kafkaAddress) {
		boolean flag = false;
		FutureTask<Object> future = new FutureTask<>(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				boolean validateKafkaAddress = false;
				KafkaAdmins admin = null;
				try {
					Properties props = new Properties();
					props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaAddress);
					 admin = new KafkaAdmins(props);
					String host = admin.descCluster().controller().get().host();
					if (host != null) {
						validateKafkaAddress = true;
					}
				}catch (Exception e){
					LOGGER.warn("connect kafka error.",e);
				}finally {
					if(admin!=null){
						admin.close();
					}
				}
				return validateKafkaAddress;
			}
		});
		Thread thread = new Thread(future);
		thread.start();
		Object validateKafkaAddress =null;
		try {
			validateKafkaAddress = future.get(3, TimeUnit.SECONDS);
			if(validateKafkaAddress!=null) {
				flag = true;
			}
		}catch (Exception e) {
			LOGGER.warn("validateKafkaAddress  result is inactive",e);
		}
		return flag;
	}

}
