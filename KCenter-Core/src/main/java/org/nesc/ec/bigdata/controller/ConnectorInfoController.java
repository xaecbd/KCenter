package org.nesc.ec.bigdata.controller;

import org.nesc.ec.bigdata.common.BaseController;
import org.nesc.ec.bigdata.common.RestResponse;
import org.nesc.ec.bigdata.model.ConnectorInfo;
import org.nesc.ec.bigdata.model.UserInfo;
import org.nesc.ec.bigdata.service.ConnectInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/connector")
public class ConnectorInfoController extends BaseController {

    public static final Logger LOGGER = LoggerFactory.getLogger(ConnectorInfoController.class);

    @Autowired
    ConnectInfoService connectInfoService;

    @RequestMapping("/list")
    @ResponseBody
    public RestResponse connectorList(){
        UserInfo userInfo = getCurrentUser();
        try{
         List<ConnectorInfo> connectorInfoList =  connectInfoService.selectConnectListAddCacheUrl(userInfo);
         return SUCCESS_DATA(connectorInfoList);
        }catch (Exception e){
            LOGGER.error("list connector has error,please check",e);
            return ERROR("list connector has error!");
        }
    }

    @PostMapping("/insert")
    @ResponseBody
    public RestResponse insertConnectorInfo(@RequestBody ConnectorInfo connectorInfo){
        try{
            boolean flag =  connectInfoService.saveConnect(connectorInfo);
            if(flag){
                return SUCCESS("insert connector success!");
            }
        }catch (Exception e){
            LOGGER.error("insert connector has error,please check",e);
        }
        return ERROR("insert connector has error!");
    }


    @PostMapping("/update")
    @ResponseBody
    public RestResponse updateConnectorInfo(@RequestBody ConnectorInfo connectorInfo){
        try{
            boolean flag =  connectInfoService.updateConnect(connectorInfo);
            if(flag){
                return SUCCESS("update connector success!");
            }
        }catch (Exception e){
            LOGGER.error("update connector has error,please check",e);
        }
        return ERROR("update connector has error!");
    }

    @DeleteMapping("/del")
    @ResponseBody
    public RestResponse deleteConnectorInfo(@RequestParam long id){
        try{
            boolean flag =  connectInfoService.deleteConnectInfo(id);
            if(flag){
                return SUCCESS("delete connector success!");
            }
        }catch (Exception e){
            LOGGER.error("delete connector has error,please check",e);
        }
        return ERROR("delete connector has error!");
    }


}
