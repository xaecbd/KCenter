package org.nesc.ec.bigdata.controller;

import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.common.BaseController;
import org.nesc.ec.bigdata.common.RestResponse;
import org.nesc.ec.bigdata.common.RoleEnum;
import org.nesc.ec.bigdata.exception.KSQLException;
import org.nesc.ec.bigdata.model.*;
import org.nesc.ec.bigdata.model.vo.KsqlInfoVo;
import org.nesc.ec.bigdata.service.KsqlHistoryService;
import org.nesc.ec.bigdata.service.KsqlStreamService;
import org.nesc.ec.bigdata.service.KsqlClusterService;
import org.nesc.ec.bigdata.service.KsqlTableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Truman.P.Du
 * @date 2020/02/27
 * @description
 */
@RestController
@RequestMapping("/ksql")
public class KsqlController extends BaseController {
    private static final Logger LOG = LoggerFactory.getLogger(KsqlController.class);

    @Autowired
    KsqlClusterService ksqlClusterService;

    @Autowired
    KsqlStreamService ksqlStreamService;
    @Autowired
    KsqlTableService ksqlTableService;

    @Autowired
    KsqlHistoryService ksqlHistoryService;

    @RequestMapping("/list")
    @ResponseBody
    public RestResponse ksqlList() {
        List<KsqlInfo> list = null;
        try {
            UserInfo userInfo = getCurrentUser();
            if(RoleEnum.ADMIN.getDescription().equalsIgnoreCase(userInfo.getRole().getDescription())){
                list = ksqlStreamService.getKsqlList(ksqlStreamService.getTotalData());
            }else {
                List<KsqlClusterInfo> userKsqlClusterInfo = ksqlClusterService.getKsqlClusterByTeams(userInfo.getTeamIDs());
                list = ksqlStreamService.getKsqlList(userKsqlClusterInfo);
            }
        } catch (Exception e) {
            LOG.error("Ksql list Error!", e);
            return ERROR("Ksql list Error!Please check");
        }
        return SUCCESS_DATA(list);
    }


    @GetMapping("/stream/list")
    @ResponseBody
    public RestResponse getTopics(@RequestParam("clusterId") String clusterId) {
        try {
            List<KStreamInfo> streamInfos = new ArrayList<>();
            UserInfo user = this.getCurrentUser();
            if(RoleEnum.ADMIN.getDescription().equalsIgnoreCase(user.getRole().getDescription())){
                streamInfos =  ksqlStreamService.getClusterKStreamData(Integer.parseInt(clusterId));
            }else {
                List<Long> teamIDs = user.getTeamIDs();
                if (teamIDs != null && teamIDs.size() > 0) {
                    streamInfos = ksqlStreamService.getClusterKStreamByTeam(teamIDs,Integer.parseInt(clusterId));
                }
            }
            return SUCCESS_DATA(streamInfos);
        } catch (Exception e) {
            LOG.error("Get all topic error.", e);
            return ERROR("GET TOPICS INFORMATION FAILED!");
        }
    }

    @GetMapping("/table/list")
    @ResponseBody
    public RestResponse showTables(@RequestParam("clusterId") String clusterId) {
        try {
            UserInfo user = this.getCurrentUser();
            List<KTableInfo> tableInfos =  ksqlTableService.getKsqlTableList(user,clusterId);
            return SUCCESS_DATA(tableInfos);
        } catch (Exception e) {
            LOG.error("Get ksql table has  error.", e);
            return ERROR("GET ksql tables FAILED!");
        }
    }

    @PostMapping("/create_table")
    @ResponseBody
    public RestResponse addTable(@RequestBody KsqlInfoVo ksqlInfoVo){
        UserInfo userInfo = getCurrentUser();
        String script = ksqlInfoVo.getkTableInfo().getScript();
        try{
            String result =  ksqlTableService.createTable(ksqlInfoVo,userInfo);
            ksqlHistoryService.insertTableHistory(userInfo,ksqlInfoVo.getKsqlServerId(),ksqlInfoVo.getkTableInfo().getName(),"create table success",script);
            return SUCCESS(result);
        }catch (KSQLException e){
            ksqlHistoryService.insertTableHistory(userInfo,ksqlInfoVo.getKsqlServerId(),ksqlInfoVo.getkTableInfo().getName(),"create table has error:"+e.getMessage(),script);
            return ERROR(e.getMessage());
        }catch (Exception e1){
            ksqlHistoryService.insertTableHistory(userInfo,ksqlInfoVo.getKsqlServerId(),ksqlInfoVo.getkTableInfo().getName(),"create table has error:"+e1.getMessage(),script);
            LOG.error("create ksql table has error,",e1);
            return ERROR("create ksql table has error");
        }
    }

    @PostMapping("/edit_table")
    public  RestResponse editTable(@RequestBody KsqlInfoVo ksqlInfoVo){
        UserInfo userInfo = getCurrentUser();
        String script = ksqlInfoVo.getkTableInfo().getScript();
        try {
            String ksqlServerId = ksqlInfoVo.getKsqlServerId();
            String clusterName = ksqlInfoVo.getClusterName();
            ksqlTableService.dropTable(ksqlServerId,clusterName,ksqlInfoVo.getkTableInfo().getName(),String.valueOf(ksqlInfoVo.getkTableInfo().getId()));
            String createResult = ksqlTableService.createTable(ksqlInfoVo,userInfo);
            ksqlHistoryService.insertTableHistory(userInfo,ksqlInfoVo.getKsqlServerId(),ksqlInfoVo.getkTableInfo().getName(),"edit table success"
                    ,script);
            return SUCCESS(createResult);
        }catch (KSQLException e){
            ksqlHistoryService.insertTableHistory(userInfo,ksqlInfoVo.getKsqlServerId(),ksqlInfoVo.getkTableInfo().getName(),"edit table has error:"+e.getMessage(),script);
            return ERROR(e.getMessage());
        }catch (Exception e1){
            ksqlHistoryService.insertTableHistory(userInfo,ksqlInfoVo.getKsqlServerId(),ksqlInfoVo.getkTableInfo().getName(),"edit table has error:"+e1.getMessage(),script);
            LOG.error("edit ksql table has error,",e1);
            return ERROR("edit ksql table has error");
        }
    }

    @GetMapping("/describe_table")
    @ResponseBody
    public RestResponse describeTable(@RequestParam("ksqlServerId") String ksqlServerId, @RequestParam("clusterName") String clusterName,
                                      @RequestParam("tableName") String tableName){
        UserInfo userInfo = getCurrentUser();
        try{
            JSONObject result = ksqlTableService.describeTable(ksqlServerId,clusterName,tableName);
            ksqlHistoryService.insertTableHistory(userInfo,ksqlServerId,tableName,"describe table success");
            return SUCCESS_DATA(result);
        }catch (KSQLException e){
            ksqlHistoryService.insertTableHistory(userInfo,ksqlServerId,tableName,"describe table has error:"+e.getMessage());
            return ERROR(e.getMessage());

        }catch (Exception e1){
            ksqlHistoryService.insertTableHistory(userInfo,ksqlServerId,tableName,"describe table has error:"+e1.getMessage());
            LOG.error("describe table has error,please check",e1);
        }
        return ERROR("describe table has error,please check");
    }

    @DeleteMapping("/drop_table")
    @ResponseBody
    public RestResponse dropTable(@RequestParam("ksqlServerId") String ksqlServerId, @RequestParam("clusterName") String clusterName, @RequestParam("tableName") String tableName
            ,@RequestParam(value = "id") String id) {
        UserInfo userInfo = getCurrentUser();
        try {
            String message = ksqlTableService.dropTable(ksqlServerId, clusterName, tableName, id);
            ksqlHistoryService.insertTableHistory(userInfo,ksqlServerId,tableName,"drop table success");
            return SUCCESS(message);
        }catch (KSQLException e){
            ksqlHistoryService.insertTableHistory(userInfo,ksqlServerId,tableName,"drop table has error:"+e.getMessage());
            return ERROR(e.getMessage());

        }catch (Exception e1){
            ksqlHistoryService.insertTableHistory(userInfo,ksqlServerId,tableName,"drop table has error:"+e1.getMessage());
            LOG.error(" drop table has error,please check",e1);
        }
        return ERROR("Ksql drop table Error!Please check");
    }

    @GetMapping("/select_table")
    @ResponseBody
    public RestResponse selectTable(@RequestParam("ksqlServerId") String ksqlServerId, @RequestParam("clusterName") String clusterName,
                                    @RequestParam("streamName") String streamName){
        UserInfo userInfo = getCurrentUser();
        try{
            List<JSONObject> list = ksqlTableService.selectTable(ksqlServerId,clusterName,streamName);
            ksqlHistoryService.insertTableHistory(userInfo,ksqlServerId,streamName,"select table success");
            return SUCCESS_DATA(list);
        }catch (KSQLException e){
            ksqlHistoryService.insertTableHistory(userInfo,ksqlServerId,streamName,"select table success has error:"+e.getMessage());
            return ERROR(e.getMessage());
        }catch (Exception e1){
            LOG.error("select table has error,please check,",e1);
            ksqlHistoryService.insertTableHistory(userInfo,ksqlServerId,streamName,"select table success has error:"+e1.getMessage());
            return ERROR("select table has error,please check!");
        }
    }

    @PostMapping("/show_queries")
    @ResponseBody
    public RestResponse showQueries(@RequestParam("ksqlServerId") String ksqlServerId, @RequestParam("clusterName") String clusterName){
        try{
            return SUCCESS_DATA(ksqlTableService.showQueries(ksqlServerId,clusterName));
        }catch (KSQLException e){
            return ERROR(e.getMessage());
        }catch (Exception e1){
            LOG.error("show queries has error,please check,",e1);
            return ERROR("show queries has error,please check!");
        }
    }


    @DeleteMapping("/terminate_query")
    @ResponseBody
    public RestResponse terminateQuery(@RequestParam("ksqlServerId") String ksqlServerId, @RequestParam("clusterName") String clusterName, @RequestParam("queryId") String queryId) {
        try {
            String message =  ksqlTableService.terminateQuery(ksqlServerId, clusterName, queryId);
            return SUCCESS(message);
        } catch (KSQLException e){
            return ERROR(e.getMessage());
        }catch (Exception e1) {
            LOG.error("Ksql terminateQuery Error!Please check,", e1);
            return ERROR("Ksql terminateQuery Error!Please check");
        }

    }


    @PostMapping("/create_stream")
    @ResponseBody
    public RestResponse createStream(@RequestBody KsqlInfoVo ksqlInfoVo){
        UserInfo userInfo = getCurrentUser();
        String script = ksqlInfoVo.getkStreamInfo().getScript();
        try{
            String msg =  ksqlStreamService.createStream(ksqlInfoVo,userInfo);
            ksqlHistoryService.insertStreamHistory(userInfo,ksqlInfoVo.getKsqlServerId(),ksqlInfoVo.getkStreamInfo().getName(),"create stream success",
                    script);
            return SUCCESS(msg);

        }catch (KSQLException e){
            ksqlHistoryService.insertStreamHistory(userInfo,ksqlInfoVo.getKsqlServerId(),ksqlInfoVo.getkStreamInfo().getName(),"create stream error:"+e.getMessage(),script);
            return ERROR(e.getMessage());

        }catch (Exception e1){
            ksqlHistoryService.insertStreamHistory(userInfo,ksqlInfoVo.getKsqlServerId(),ksqlInfoVo.getkStreamInfo().getName(),"create stream error:"+e1.getMessage(),script);
            LOG.error("create stream has error,please check",e1);
        }
        return ERROR("create stream has error,please check");
    }

    @PostMapping("/edit_stream")
    @ResponseBody
    public RestResponse editStream(@RequestBody KsqlInfoVo ksqlInfoVo){
        UserInfo userInfo = getCurrentUser();
        String script = ksqlInfoVo.getkStreamInfo().getScript();
        try{
            String ksqlServerId = ksqlInfoVo.getKsqlServerId();
            String clusterName = ksqlInfoVo.getClusterName();
            ksqlStreamService.dropStream(ksqlServerId,clusterName,ksqlInfoVo.getkStreamInfo().getName(),String.valueOf(ksqlInfoVo.getkStreamInfo().getId()));
            String createResult = ksqlStreamService.createStream(ksqlInfoVo,userInfo);
            ksqlHistoryService.insertStreamHistory(userInfo,ksqlInfoVo.getKsqlServerId(),ksqlInfoVo.getkStreamInfo().getName(),"edit stream success"
                    ,script);
            return SUCCESS(createResult);
        }catch (KSQLException e){
            ksqlHistoryService.insertStreamHistory(userInfo,ksqlInfoVo.getKsqlServerId(),ksqlInfoVo.getkStreamInfo().getName(),"edit stream has error:"+e.getMessage(),script);
            return ERROR(e.getMessage());
        }catch (Exception e1){
            ksqlHistoryService.insertStreamHistory(userInfo,ksqlInfoVo.getKsqlServerId(),ksqlInfoVo.getkStreamInfo().getName(),"edit stream error:"+e1.getMessage(),script);
            LOG.error("edit stream has error,please check",e1);
            return ERROR("edit stream has error,please check");
        }
    }

    @GetMapping("/select_stream")
    @ResponseBody
    public RestResponse selectStream(@RequestParam("ksqlServerId") String ksqlServerId, @RequestParam("clusterName") String clusterName, @RequestParam("streamName") String streamName){
        UserInfo userInfo = getCurrentUser();
        try{
            ksqlHistoryService.insertStreamHistory(userInfo,ksqlServerId,streamName,"select stream");
            return SUCCESS_DATA(ksqlStreamService.selectStream(ksqlServerId,clusterName,streamName));
        }catch (KSQLException e){
            ksqlHistoryService.insertStreamHistory(userInfo,ksqlServerId,streamName,"select stream has error:"+e.getMessage());
            return ERROR(e.getMessage());
        }catch (Exception e1){
            LOG.error("select stream has error,please check,",e1);
            ksqlHistoryService.insertStreamHistory(userInfo,ksqlServerId,streamName,"select stream has error:"+e1.getMessage());
            return ERROR("select stream has error,please check!");
        }
    }

    @GetMapping("/describe_stream")
    @ResponseBody
    public RestResponse describeStream(@RequestParam("ksqlServerId") String ksqlServerId, @RequestParam("clusterName") String clusterName, @RequestParam("streamName") String streamName){
        UserInfo userInfo = getCurrentUser();
        try{
            JSONObject result = ksqlStreamService.describeStream(ksqlServerId,clusterName,streamName);
            ksqlHistoryService.insertStreamHistory(userInfo,ksqlServerId,streamName,"describe stream");
            return SUCCESS_DATA(result);
        }catch (KSQLException e){
            ksqlHistoryService.insertStreamHistory(userInfo,ksqlServerId,streamName,"describe stream has error:"+e.getMessage());
            return ERROR(e.getMessage());

        }catch (Exception e1){
            ksqlHistoryService.insertStreamHistory(userInfo,ksqlServerId,streamName,"describe stream has error:"+e1.getMessage());
            LOG.error("describe stream has error,please check",e1);
        }
        return ERROR("describe stream has error,please check");
    }

    @DeleteMapping("/drop_stream")
    @ResponseBody
    public RestResponse dropStream(@RequestParam("ksqlServerId") String ksqlServerId, @RequestParam("clusterName") String clusterName, @RequestParam("streamName") String streamName
            ,@RequestParam(value = "id") String id) {
        UserInfo userInfo = getCurrentUser();
        try {
            String message = ksqlStreamService.dropStream(ksqlServerId, clusterName, streamName, id);
            ksqlHistoryService.insertStreamHistory(userInfo,ksqlServerId,streamName,"drop stream");
            return SUCCESS(message);
        }catch (KSQLException e){
            ksqlHistoryService.insertStreamHistory(userInfo,ksqlServerId,streamName,"drop stream has error:"+e.getMessage());
            return ERROR(e.getMessage());

        }catch (Exception e1){
            ksqlHistoryService.insertStreamHistory(userInfo,ksqlServerId,streamName,"drop stream has error:"+e1.getMessage());
            LOG.error("drop  stream has error,please check",e1);
        }
        return ERROR("ksql drop Stream Error!Please check");
    }



    @PostMapping("/add_ksql")
    @ResponseBody
    public RestResponse addKsql(@RequestBody KsqlClusterInfo ksqlClusterInfo){
        boolean result = false;
        try {

            result = ksqlClusterService.addKsql(ksqlClusterInfo);
            if (result) {
                return SUCCESS("Success");
            }else {
                return ERROR("fail ,Please check if the ksqlServerId already exists ");
            }
        } catch (Exception e) {
            LOG.warn("Please check KsqlAddress is valid");
            return ERROR("Please check KsqlAddress is valid");
        }
    }

    @RequestMapping("/del_ksql")
    @ResponseBody
    public RestResponse delksql(@RequestParam("clusterName") String clusterName, @RequestParam("ksqlServiceId") String ksqlServiceId,
                                @RequestParam("id") String id){
        boolean result =false;
        String errorMessage = "del ksqlServer fail";
        try {
            result = ksqlClusterService.delKsql(clusterName, ksqlServiceId,id);
            if (result){
                return SUCCESS("Success");
            }else {
                return ERROR(errorMessage);
            }
        } catch (Exception e) {
            LOG.warn(errorMessage,e);
            return ERROR(errorMessage);
        }
    }
}
