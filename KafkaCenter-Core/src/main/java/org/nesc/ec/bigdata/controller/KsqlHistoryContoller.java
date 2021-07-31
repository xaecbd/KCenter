package org.nesc.ec.bigdata.controller;

import org.nesc.ec.bigdata.common.BaseController;
import org.nesc.ec.bigdata.common.RestResponse;
import org.nesc.ec.bigdata.service.KsqlHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/ksql/history")
@RestController
public class KsqlHistoryContoller extends BaseController {

    @Autowired
    KsqlHistoryService ksqlHistoryService;

    @GetMapping("/list")
    @ResponseBody
    public RestResponse selectStreamHistory(@RequestParam(value = "ksqlServerId") String ksqlServerId,
                                            @RequestParam(value = "name") String name, @RequestParam(value = "type") String type){
      return SUCCESS_DATA(ksqlHistoryService.selectHistoryByType(ksqlServerId,name,type));
    }
}
