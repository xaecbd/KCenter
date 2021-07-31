package org.nesc.ec.bigdata.controller;

import com.alibaba.fastjson.JSONArray;
import org.nesc.ec.bigdata.common.BaseController;
import org.nesc.ec.bigdata.common.RestResponse;
import org.nesc.ec.bigdata.exception.ConnectorException;
import org.nesc.ec.bigdata.model.ConnectorJob;
import org.nesc.ec.bigdata.model.UserInfo;
import org.nesc.ec.bigdata.model.vo.ConnectorVo;
import org.nesc.ec.bigdata.service.ConnectorJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/connector/job")
@RestController
public class ConnectorJobController extends BaseController {

    public static final Logger LOGGER = LoggerFactory.getLogger(ConnectorJobController.class);


    @Autowired
    ConnectorJobService connectorJobService;


    @PostMapping("/insert")
    @ResponseBody
    public RestResponse insertConnector(@RequestBody ConnectorVo connectorVo){
        try{
            boolean flag =  connectorJobService.saveConnector(connectorVo,getCurrentUser());
            if(flag){
                return SUCCESS("insert connector success!");
            }
        }catch (ConnectorException e){
            LOGGER.error("insert connector  job has error,please check",e);
            return ERROR(e.getMessage());
        }catch (Exception e){
            LOGGER.error("insert connector  job has error,please check",e);
        }
        return ERROR("insert connector job has error!");
    }

    @PutMapping("/update")
    @ResponseBody
    public RestResponse updateConnector(@RequestBody ConnectorVo connectorVo){
        try{

            boolean flag =  connectorJobService.updateConnector(connectorVo);
            if(flag){
                return SUCCESS("update connector success!");
            }
        }catch (ConnectorException e){
            LOGGER.error("update connector  job has error,please check",e);
            return ERROR(e.getMessage());
        }catch (Exception e){
            LOGGER.error("update connector  job has error,please check",e);
        }
        return ERROR("update connector job has error!");
    }


    @DeleteMapping("/delete")
    @ResponseBody
    public RestResponse deleteConnector(@RequestBody ConnectorVo connectorVo){
        try{
            boolean flag =  connectorJobService.deleteConnector(connectorVo.getConnectorJob(),connectorVo.getClusterName());
            if(flag){
                return SUCCESS("delete connector success!");
            }
        }catch (ConnectorException e){
            LOGGER.error("delete connector  job has error,please check",e);
            return ERROR(e.getMessage());
        }catch (Exception e){
            LOGGER.error("delete connector  job has error,please check",e);
        }
        return ERROR("delete connector job has error!");
    }


    @GetMapping("/list")
    @ResponseBody
    public RestResponse connectorJobList(@RequestParam("clusterId") int clusterId,@RequestParam("clusterName") String clusterName){
        UserInfo userInfo = getCurrentUser();
        try{
            List<ConnectorJob> connectorInfoList =  connectorJobService.selectConnectAndState(userInfo,clusterId,clusterName);
            return SUCCESS_DATA(connectorInfoList);
        }catch (Exception e){
            LOGGER.error("list connector has error,please check",e);
            return ERROR("list connector has error!");
        }
    }

    @GetMapping("/plugins")
    @ResponseBody
    public RestResponse connectorJobPlugins(@RequestParam("clusterId") String clusterId,@RequestParam("clusterName") String clusterName){
        try{
          JSONArray array = connectorJobService.connectorJobPlugins(clusterId,clusterName);
            return SUCCESS_DATA(array);
        }catch (Exception e){
            LOGGER.error("list connector plugins has error,please check",e);
            return ERROR("list connector plugins has error!");
        }
    }

    @GetMapping("/task/status")
    @ResponseBody
    public RestResponse connectorJobTask(@RequestParam("clusterId") String clusterId,@RequestParam("clusterName") String clusterName,@RequestParam("connectorName") String connectorName){
        try{
            JSONArray array = connectorJobService.connectorJobTaskInfo(clusterId,clusterName,connectorName);
            return SUCCESS_DATA(array);
        }catch (Exception e){
            LOGGER.error("list connector task has error,please check",e);
            return ERROR("list connector task has error!");
        }
    }

    @GetMapping("/search")
    @ResponseBody
    public RestResponse searchConnectorJobTask(@RequestParam("id") String id){
        try{
            ConnectorJob connectorJob = connectorJobService.searchConnectorJob(id);
            return SUCCESS_DATA(connectorJob);
        }catch (Exception e){
            LOGGER.error("list connector task has error,please check",e);
            return ERROR("list connector task has error!");
        }
    }


    @PostMapping("/validate")
    @ResponseBody
    public RestResponse connectorJobValidate(@RequestBody ConnectorVo connectorVo){
        try{
           JSONArray connectorInfoList =  connectorJobService.validateConnector(connectorVo);
            return SUCCESS_DATA(connectorInfoList);
        }catch (ConnectorException e){
            LOGGER.error("validate connector  job has error,please check",e);
            return ERROR(e.getMessage());
        }catch (Exception e){
            LOGGER.error("validate connector has error,please check",e);
            return ERROR("validate connector has error");
        }
    }

    @GetMapping("/restart/task")
    @ResponseBody
    public RestResponse connectorTaskRestart(@RequestParam String clusterId,@RequestParam String clusterName,@RequestParam String connectorName,@RequestParam String taskId){
        try{
         String value =  connectorJobService.connectorTaskRestart(connectorName,taskId,clusterName,clusterId);
         return SUCCESS("restart connector success!");
        }catch (Exception e){
            LOGGER.error("restart connector has error,please check",e);
        }
        return ERROR("restart connector has error!");
    }

    @PostMapping("/restart")
    @ResponseBody
    public RestResponse connectorJobRestart(@RequestBody ConnectorVo connectorVo){
        try{
            String value =  connectorJobService.connectorJobRestart(connectorVo);
            return SUCCESS("restart connector job success!");
        }catch (Exception e){
            LOGGER.error("restart connector job  has error,please check",e);
        }
        return ERROR("restart connector job has error!");
    }


    @PutMapping("/pause")
    @ResponseBody
    public RestResponse connectorJobPause(@RequestBody ConnectorVo connectorVo){
        try{
            String value =  connectorJobService.connectorJobPause(connectorVo);
            return SUCCESS("pause connector job success!");
        }catch (Exception e){
            LOGGER.error("pause connector job  has error,please check",e);
        }
        return ERROR("pause connector job has error!");
    }

    @PutMapping("/resume")
    @ResponseBody
    public RestResponse connectorJobResume(@RequestBody ConnectorVo connectorVo){
        try{
            String value =  connectorJobService.connectorJobResume(connectorVo);
            return SUCCESS("resume connector job success!");
        }catch (Exception e){
            LOGGER.error("resume connector job  has error,please check",e);
        }
        return ERROR("resume connector job has error!");
    }
}
