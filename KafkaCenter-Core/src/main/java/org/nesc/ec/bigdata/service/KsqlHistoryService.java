package org.nesc.ec.bigdata.service;

import org.nesc.ec.bigdata.mapper.KsqlHistoryMapper;
import org.nesc.ec.bigdata.model.KsqlHistoryInfo;
import org.nesc.ec.bigdata.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KsqlHistoryService {

    @Autowired
    KsqlHistoryMapper ksqlHistoryMapper;

    public void insertStreamHistory(KsqlHistoryInfo ksqlHistoryInfo){
        ksqlHistoryInfo.setDate(new Date());
        ksqlHistoryMapper.insert(ksqlHistoryInfo);
    }



    public void insertStreamHistory(UserInfo userInfo,String ksqlServerId,String name,String operate,String script){
        KsqlHistoryInfo ksqlHistoryInfo = generateKsqlHistoryEntity(userInfo,ksqlServerId,name,operate,script);
        ksqlHistoryInfo.setType("stream");
        ksqlHistoryMapper.insert(ksqlHistoryInfo);
    }

    public void insertStreamHistory(UserInfo userInfo,String ksqlServerId,String name,String operate){
        KsqlHistoryInfo ksqlHistoryInfo = generateKsqlHistoryEntity(userInfo,ksqlServerId,name,operate,"");
        ksqlHistoryInfo.setType("stream");
        ksqlHistoryMapper.insert(ksqlHistoryInfo);
    }

    public void insertTableHistory(UserInfo userInfo,String ksqlServerId,String name,String operate,String script){
        KsqlHistoryInfo ksqlHistoryInfo = generateKsqlHistoryEntity(userInfo,ksqlServerId,name,operate,script);
        ksqlHistoryInfo.setType("table");
        ksqlHistoryMapper.insert(ksqlHistoryInfo);

    }

    public void insertTableHistory(UserInfo userInfo,String ksqlServerId,String name,String operate){
        KsqlHistoryInfo ksqlHistoryInfo = generateKsqlHistoryEntity(userInfo,ksqlServerId,name,operate,"");
        ksqlHistoryInfo.setType("table");
        ksqlHistoryMapper.insert(ksqlHistoryInfo);

    }

    public  List<KsqlHistoryInfo> selectHistoryByType(String ksqlServerId,String name,String type){
        Map<String,Object> objectMap = new HashMap<>();
        objectMap.put("ksql_server_id",ksqlServerId);
        objectMap.put("name",name);
        objectMap.put("type",type);
        List<KsqlHistoryInfo> ksqlHistoryInfoList =   ksqlHistoryMapper.selectByMap(objectMap);
        return ksqlHistoryInfoList.stream().sorted(Comparator.comparing(KsqlHistoryInfo::getDate).reversed()).collect(Collectors.toList());
    }


    private KsqlHistoryInfo generateKsqlHistoryEntity(UserInfo userInfo,String ksqlServerId,String name,String operate,String script){
        KsqlHistoryInfo ksqlHistoryInfo = new KsqlHistoryInfo();
        ksqlHistoryInfo.setKsqlServerId(ksqlServerId);
        ksqlHistoryInfo.setName(name);
        ksqlHistoryInfo.setUser(userInfo.getRealName());
        ksqlHistoryInfo.setOperate(operate);
        ksqlHistoryInfo.setDate(new Date());
        ksqlHistoryInfo.setUser(userInfo.getName());
        ksqlHistoryInfo.setScript(script);
        return ksqlHistoryInfo;
    }
}
