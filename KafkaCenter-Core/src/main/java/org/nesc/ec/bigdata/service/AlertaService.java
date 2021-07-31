package org.nesc.ec.bigdata.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.nesc.ec.bigdata.common.model.OffsetInfo;
import org.nesc.ec.bigdata.config.AlertaConfig;
import org.nesc.ec.bigdata.config.InitConfig;
import org.nesc.ec.bigdata.constant.AlertConfig;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.constant.TopicConfig;
import org.nesc.ec.bigdata.job.NoticeJob;
import org.nesc.ec.bigdata.model.AlertGoup;
import org.nesc.ec.bigdata.model.MonitorNoticeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * @author Truman.P.Du
 * @date 2020/08/06
 * @description
 */
@Service
public class AlertaService {

    private static final Logger LOG = LoggerFactory.getLogger(AlertaService.class);
    @Autowired
    AlertaConfig alertaConfig;
    @Autowired
    InitConfig initConfig;

    @Autowired
    RestTemplate restTemplate;


    public boolean isSendToAlerta(MonitorNoticeInfo monitorNoticeInfo, Long now, Long lastTime) {
        AlertGoup alertGoup = monitorNoticeInfo.getAlertGoup();
        if (alertaConfig.isAlterEnable() && !alertGoup.isDisableAlerta()) {
            long disTime = Long.parseLong(alertaConfig.getAlertDispause());
            if (((now - lastTime) >= disTime * Constants.Time.SIXTY * Constants.Time.THOUSAND)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 判断是否启用alerta，如果开启即发送alerta
     *
     * @param monitorNoticeInfo
     */
    public void sendToAlerta(MonitorNoticeInfo monitorNoticeInfo) {
        AlertGoup alertGoup = monitorNoticeInfo.getAlertGoup();
        if (alertaConfig.isAlterEnable() && !alertGoup.isDisableAlerta()) {
            generateSendToAlterService(monitorNoticeInfo);
        }
    }

    /**
     * 获取virtual-email-groups对应关系
     *
     * @return
     */
    public Map<String, String> getAlarmGroupMap() {
        Map<String, String> map = new HashMap<>();
        if (!alertaConfig.isAlterEnable()) {
            return map;
        }
        try {
            String response = restTemplate.getForObject(alertaConfig.getAlarmGroupApi(), String.class);

            JSONArray array = JSONArray.parseArray(response);
            array.forEach(obj -> {
                JSONObject jsonObject = (JSONObject) obj;
                String name = jsonObject.getString("Name");
                jsonObject.remove("Id");
                jsonObject.remove("UniqId");
                map.put(name.toLowerCase(), jsonObject.toJSONString());
            });
        } catch (Exception e) {
            LOG.error("get virtual-email-groups has error.", e);
        }


        return map;
    }

    private void generateSendToAlterService(MonitorNoticeInfo monitorNoticeInfo) {
        String topic, group, method;
        AlertGoup alertGoup = monitorNoticeInfo.getAlertGoup();
        topic = alertGoup.getTopicName();
        group = alertGoup.getConsummerGroup();
        method = alertGoup.getConsummerApi();
        String resource = " Cluster:" + Constants.Symbol.LEFT_PARENTHESES + alertGoup.getCluster().getName() + Constants.Symbol.RIGHT_PARENTHESES + " Topic:" + Constants.Symbol.LEFT_PARENTHESES + alertGoup.getTopicName() + Constants.Symbol.RIGHT_PARENTHESES +
                " Group:" + Constants.Symbol.LEFT_PARENTHESES + alertGoup.getConsummerGroup() + Constants.Symbol.RIGHT_PARENTHESES;
        try {
            List<OffsetInfo> offsetInfos = monitorNoticeInfo.getOffsetInfos();
            long value = 0L;
            for (OffsetInfo offsetInfo : offsetInfos) {
                value = value + offsetInfo.getLag();
            }
            String key = alertGoup.getCluster().getId() + Constants.Symbol.VERTICAL_STR + topic + Constants.Symbol.VERTICAL_STR
                    + group;


            String href = "<a href=" + initConfig.getKafkaCenterUrl() + "/#/monitor/consumer/topic/consumer_offsets/chart/" + alertGoup.getCluster().getId() + "/" + topic + "/" + group + "/" + method.toLowerCase() + ">" + group + "</a>";
            String threshold = " " + group + " consumer lag > " + alertGoup.getThreshold();
            String clusterName = monitorNoticeInfo.getAlertGoup().getCluster().getName();
            JSONObject attriute = new JSONObject();
            if (StringUtils.isNotBlank(monitorNoticeInfo.getAlertaOwnerGroups())) {
                JSONObject jsonObject = JSONObject.parseObject(monitorNoticeInfo.getAlertaOwnerGroups());
                String ownerGroups = "";
                if (jsonObject.containsKey(AlertConfig.NAME)) {
                    ownerGroups = jsonObject.getString(AlertConfig.NAME);
                }
                attriute.put(Constants.KeyStr.OWNER_GROUPS, ownerGroups);
            }
            attriute.put(Constants.KeyStr.OWNER_EMAIL, alertGoup.getMailTo());
            attriute.put(Constants.KeyStr.MORE_INFO, href);
            attriute.put(Constants.KeyStr.THRESHOLDINFO, threshold);
            JSONObject object = new JSONObject();
            object.put(AlertConfig.RESOURCE, resource);
            object.put(AlertConfig.EVENT, TopicConfig.LAG);
            object.put(AlertConfig.ENVIRONMENT, alertaConfig.getAlterEvn());
            object.put(AlertConfig.SEVERITY, AlertConfig.CONSUMER_SEVERITY);
            object.put(AlertConfig.CORRELATE, new JSONArray());
            List<String> service = new ArrayList<>();
            service.add(topic);
            object.put(AlertConfig.SERVICE, service);
            object.put(AlertConfig.GROUP, AlertConfig.CONSUMER_GROUP);
            object.put(AlertConfig.VALUE, value);
            object.put(AlertConfig.TEXT, clusterName);
            object.put(AlertConfig.TAGS, new JSONArray());
            object.put(AlertConfig.ATTRIBUTES, attriute);
            object.put(AlertConfig.ORIGIN, AlertConfig.CONSUMER_ORIGIN);
            object.put(AlertConfig.TYPE, AlertConfig.CONSUMER_TYPE);
            object.put(AlertConfig.CREATETIME, new Date());
            object.put(AlertConfig.TIMEOUT, null);
            object.put(AlertConfig.RAWDATA, null);
            object.put(AlertConfig.CUSTOMER, null);
            HttpHeaders headers = new HttpHeaders();
            headers.add(Constants.KeyStr.CONTENT_TYPE, Constants.KeyStr.APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<>(object.toString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(alertaConfig.getAlertServiceUrl(), httpEntity, String.class);
            JSONObject respObj = JSONObject.parseObject(Objects.requireNonNull(response.getBody()));
            if (respObj.containsKey(AlertConfig.CODE) &&
                    respObj.getString(AlertConfig.CODE).equalsIgnoreCase(Constants.Number.TWO_HUNANDER)) {
                NoticeJob.cachedLastSendTime.put(key, System.currentTimeMillis());
            } else {
                LOG.warn("send to alerta has error. data:{} response:{} ", object.toString(), respObj.toJSONString());
            }
        } catch (Exception e) {
            LOG.error("generateToAlertaError " + resource, e);
        }
    }
}
