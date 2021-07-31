package org.nesc.ec.bigdata.service;

import org.nesc.ec.bigdata.cache.ConnectCache;
import org.nesc.ec.bigdata.common.RoleEnum;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.exception.ConnectorException;
import org.nesc.ec.bigdata.mapper.ConnectInfoMapper;
import org.nesc.ec.bigdata.model.ConnectorInfo;
import org.nesc.ec.bigdata.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

@Service
public class ConnectInfoService {

    @Autowired
    ConnectInfoMapper connectInfoMapper;
    @Autowired
    ConnectClusterService connectClusterService;

    public static final Logger LOGGER = LoggerFactory.getLogger(ConnectInfoService.class);


    public boolean insertConnectToDb(ConnectorInfo connectorInfo){
        return connectInfoMapper.insert(connectorInfo) > 0;
    }

    public boolean saveConnect(ConnectorInfo connectorInfo) throws ConnectorException {
        if(connectExistInDb(connectorInfo)){
            String version = connectClusterService.descConnectorVersion(connectorInfo.getUrl());
            connectorInfo.setVersion(version);
            return insertConnectToDb(connectorInfo);
        }
        return false;
    }







    public boolean updateConnect(ConnectorInfo connectorInfo) throws ConnectorException {
        if(connectExistInDb(connectorInfo)){
            String version = connectClusterService.descConnectorVersion(connectorInfo.getUrl());
            connectorInfo.setVersion(version);
            ConnectCache.CONNECTOR_URL_MAP.put(connectorInfo.getId()+Constants.Symbol.VERTICAL_STR+connectorInfo.getName(),connectorInfo.getUrl());
            return  connectInfoMapper.updateById(connectorInfo) > 0;
        }
        return false;
    }

    public List<ConnectorInfo> selectConnectList(UserInfo userInfo){
        if(RoleEnum.ADMIN.getDescription().equalsIgnoreCase(userInfo.getRole().getDescription())){
            return connectInfoMapper.selectConnectList();
        }
        List<Long> teamIds = userInfo.getTeamIDs();
        return  connectInfoMapper.getConnectByTeams(teamIds);
    }

    public List<ConnectorInfo> selectConnectList(){
        return connectInfoMapper.selectConnectList();
    }

    public List<ConnectorInfo> selectConnectListAddCacheUrl(UserInfo userInfo){
        List<ConnectorInfo> connectorInfoList = selectConnectList(userInfo);
        connectorInfoList.forEach(connectorInfo -> ConnectCache.CONNECTOR_URL_MAP.put(connectorInfo.getId()+ Constants.Symbol.VERTICAL_STR+connectorInfo.getName(),connectorInfo.getUrl()));
        return connectorInfoList;
    }

    public boolean deleteConnectInfo(long id){
        return connectInfoMapper.deleteById(id) > 0;
    }



    private boolean connectExistInDb(ConnectorInfo connectorInfo){
        String url = connectorInfo.getUrl();
        List<String> urls = Arrays.asList(url.split(","));
        List<ConnectorInfo> connectorInfoList;
        if(connectorInfo.getId()!=0){
            connectorInfoList = connectInfoMapper.getConnectByUrlAndId(urls,connectorInfo.getId());
        }else {
            connectorInfoList = connectInfoMapper.getConnectByUrl(urls);
        }
        return CollectionUtils.isEmpty(connectorInfoList);
    }




}
