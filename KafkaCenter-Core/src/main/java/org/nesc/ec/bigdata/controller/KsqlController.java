package org.nesc.ec.bigdata.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.common.BaseController;
import org.nesc.ec.bigdata.common.RestResponse;
import org.nesc.ec.bigdata.model.KsqlInfo;
import org.nesc.ec.bigdata.service.KsqlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
    KsqlService ksqlService;

    @RequestMapping("/list")
    @ResponseBody
    public RestResponse ksqlList() {
        List<KsqlInfo> list = null;
        try {
            list = ksqlService.getKsqlList();
        } catch (Exception e) {
            LOG.error("Ksql list Error!", e);
            return ERROR("Ksql list Error!Please check");
        }
        return SUCCESS_DATA(list);
    }

    @RequestMapping("/show_streams")
    @ResponseBody
    public RestResponse showStreams(@RequestParam("ksqlServerId") String ksqlServerId, @RequestParam("clusterName") String clusterName, @RequestParam(value = "streamsProperties", required = false) String streamsProperties) {
        try {
            JSONArray streams = ksqlService.showStreamOrTables(ksqlServerId, clusterName, "streams");
            return SUCCESS_DATA(streams);
        } catch (Exception e) {
            LOG.error("Ksql showStreams Error!Please check,", e);
        }
        return ERROR("ksql showStreams Error!Please check");
    }

    @RequestMapping("/show_tables")
    @ResponseBody
    public RestResponse showTables(@RequestParam("ksqlServerId") String ksqlServerId, @RequestParam("clusterName") String clusterName, @RequestParam(value = "streamsProperties", required = false) String streamsProperties) {
        try {
            JSONArray tables = ksqlService.showStreamOrTables(ksqlServerId, clusterName, "tables");
            return SUCCESS_DATA(tables);
        } catch (Exception e) {
            LOG.error("Ksql showTables Error!Please check,", e);
        }
        return ERROR("Ksql showTables Error!Please check");
    }

    @RequestMapping("/show_queries")
    @ResponseBody
    public RestResponse showQueries(@RequestParam("ksqlServerId") String ksqlServerId, @RequestParam("clusterName") String clusterName, @RequestParam(value = "streamsProperties", required = false) String streamsProperties) {
        try {
            JSONArray queries = ksqlService.showStreamOrTables(ksqlServerId, clusterName, "queries");
            return SUCCESS_DATA(queries);
        } catch (Exception e) {
            LOG.error("Ksql showQueries Error!Please check,", e);
        }
        return ERROR("Ksql showQueries Error!Please check");
    }


    @RequestMapping("/drop_stream")
    @ResponseBody
    public RestResponse dropStream(@RequestParam("ksqlServerId") String ksqlServerId, @RequestParam("clusterName") String clusterName, @RequestParam("streamName") String streamName) {
        try {
            JSONObject jsonObject = ksqlService.drop(ksqlServerId, streamName, clusterName, "streams");
            return SUCCESS_DATA(jsonObject);
        } catch (Exception e) {
            LOG.error("Ksql dropStream Error!Please check,", e);
        }
        return ERROR("Ksql dropStream Error!Please check");
    }

    @RequestMapping("/drop_table")
    @ResponseBody
    public RestResponse dropTable(@RequestParam("ksqlServerId") String ksqlServerId, @RequestParam("clusterName") String clusterName, @RequestParam("tableName") String tableName) {
        try {
            JSONObject jsonObject = ksqlService.drop(ksqlServerId, tableName, clusterName, "tables");
            return SUCCESS_DATA(jsonObject);
        } catch (Exception e) {
            LOG.error("Ksql dropTable Error!Please check,", e);
        }
        return ERROR("Ksql dropTable Error!Please check");
    }

    @RequestMapping("/terminate_query")
    @ResponseBody
    public RestResponse terminateQuery(@RequestParam("ksqlServerId") String ksqlServerId, @RequestParam("clusterName") String clusterName, @RequestParam("queryId") String queryId) {
        try {
            JSONObject jsonObject = ksqlService.terminateQuery(ksqlServerId, clusterName, queryId);
            return SUCCESS_DATA(jsonObject);
        } catch (Exception e) {
            LOG.error("Ksql terminateQuery Error!Please check,", e);
        }
        return ERROR("Ksql terminateQuery Error!Please check");
    }


    @RequestMapping("/add_ksql")
    @ResponseBody
    public RestResponse addKsql(@RequestParam("ksqlAddress") String ksqlAddress, @RequestParam("clusterId") Long clusterId){
        boolean result = false;
        try {
            result = ksqlService.addKsql(clusterId, ksqlAddress);
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
    public RestResponse delksql(@RequestParam("clusterName") String clusterName, @RequestParam("ksqlServiceId") String ksqlServiceId){
        boolean result =false;
        try {
            result = ksqlService.delKsql(clusterName, ksqlServiceId);
            if (result){
                return SUCCESS("Success");
            }else {
                return ERROR("del ksqlServer fail");
            }
        } catch (Exception e) {
            LOG.warn("del ksqlServer fail",e);
            return ERROR("del ksqlServer fail");
        }
    }
}
