package org.nesc.ec.bigdata.service;

import org.nesc.ec.bigdata.common.BaseController;
import org.nesc.ec.bigdata.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author ay05
 * @create 5/9/2019
 */
@Service
public class DBLogService {

    private static final Logger LOG = LoggerFactory.getLogger(DBLogService.class);


    void dbLog(String operation){
        Date date = new Date();
        BaseController baseController = new BaseController();
        UserInfo currentUser;
        try {
            currentUser= baseController.getCurrentUser();
        }catch (Exception e){
            currentUser =new UserInfo();
            currentUser.setName("admin");
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String dateFormat = format.format(date);
        LOG.info(currentUser.getName()+"  "+operation+" at "+dateFormat);
    }



}
