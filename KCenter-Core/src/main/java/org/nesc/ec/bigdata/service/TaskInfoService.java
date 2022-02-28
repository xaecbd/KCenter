package org.nesc.ec.bigdata.service;

import org.apache.commons.lang3.StringUtils;
import org.nesc.ec.bigdata.common.RoleEnum;
import org.nesc.ec.bigdata.config.InitConfig;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.mapper.TaskInfoMapper;
import org.nesc.ec.bigdata.model.EmailEntity;
import org.nesc.ec.bigdata.model.TaskInfo;
import org.nesc.ec.bigdata.model.UserInfo;
import org.nesc.ec.bigdata.model.vo.TaskClusterVo;
import org.nesc.ec.bigdata.model.vo.TaskInfoVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class TaskInfoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskInfoService.class);

    @Autowired
    TaskInfoMapper taskInfoMapper;
    @Autowired
    DBLogService dbLogService;
    @Autowired
    ClusterService clusterService;
    @Autowired
    KafkaAdminService kafkaAdminService;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    InitConfig initConfig;

    public boolean insert(TaskInfo task) {
        task.setCreateTime(new Date());
        Integer result = taskInfoMapper.insert(task);
        return checkResult(result);
    }

    public TaskInfo selectById(Long id) {
        return taskInfoMapper.selectById(id);
    }

    public TaskInfo queryById(Long id) {
        return taskInfoMapper.queryById(id);
    }

    public List<TaskInfo> getTotalData() {
        return taskInfoMapper.selectTaskList();
    }

    public boolean update(TaskInfo task) {
        task.setCreateTime(new Date());
        Integer result = taskInfoMapper.updateById(task);
        return checkResult(result);
    }

    public boolean updateTask(TaskInfo taskInfo){
        Integer result =  taskInfoMapper.updateAllColumnById(taskInfo);
        return checkResult(result);
    }

    public boolean delete(Long id) {
        Integer result = taskInfoMapper.deleteById(id);
        dbLogService.dbLog("delete task by id:" + id);
        return checkResult(result);
    }


    public Set<String> intersection(Set<String> s1, Set<String> s2) {
        Set<String> result = new HashSet<>();
        result.clear();
        result.addAll(s1);
        result.retainAll(s2);
        return result;
    }

    private boolean checkResult(Integer result) {
        return result > 0;
    }

    public List<TaskInfo> getTasksByOwnerID(Long ownerId) {
        return taskInfoMapper.selectByOwnerId(ownerId);
    }

    boolean deleteByClusterId(Long id) {
        boolean flag = false;
        String sql = Constants.Symbol.PERCENT + id + Constants.Symbol.PERCENT;
        List<TaskInfo> tasks = taskInfoMapper.selectByClusterId(sql);
        if (tasks.isEmpty()) {
            return true;
        }
        List<Long> ids = new ArrayList<>();
        List<TaskInfo> updates = new ArrayList<>();
        tasks.forEach(task -> {
            String[] clusterIds = task.getClusterIds().split(Constants.Symbol.COMMA);
            if (clusterIds.length == 1) {
                ids.add(task.getId());
            } else {
                List<String> clusterIdList = Arrays.asList(clusterIds);
                List<String> clusterIdArrayList = new ArrayList<>(clusterIdList);
                clusterIdArrayList.remove(id.toString());
                Object[] arr = clusterIdArrayList.toArray();
                task.setClusterIds(Arrays.toString(arr).replaceAll(Constants.Symbol.DOUBLE_THE_SLASH + Constants.Symbol.LEFT_PARENTHESES, Constants.Symbol.EMPTY_STR).
                        replaceAll(Constants.Symbol.DOUBLE_THE_SLASH + Constants.Symbol.RIGHT_PARENTHESES, Constants.Symbol.EMPTY_STR));
                updates.add(task);
            }
        });
        int delete = 0;
        boolean xxx = true;
        if (!ids.isEmpty()) {
            delete = taskInfoMapper.deleteBatchIds(ids);
        }
        if (!updates.isEmpty()) {
            for (TaskInfo update : updates) {
                if (taskInfoMapper.updateById(update) > 0) {
                } else {
                    xxx = false;
                }
            }
        }
        if (delete > 0 && xxx) {
            flag = true;
        }
        return flag;
    }

    public boolean checkTopicIsExist(TaskInfo task, String selectValue) throws Exception {
        String[] clusterArray = selectValue.split(",");
        for (String clusterID : clusterArray) {
            if (kafkaAdminService.getKafkaAdmins(clusterID).checkExists(task.getTopicName())) {
                throw new Exception(" Topic already exists in the" + clusterService.selectById(Long.parseLong(clusterID)).getName() + " .");
            }
        }
        return false;
    }

    public Map<String,Object> getSendEmailInfo(TaskInfo taskInfo,Integer emailType){
        Map<String, Object> mailMap = new HashMap<>();
        try{
            TaskInfoVo taskInfoVo = new TaskInfoVo();
            EmailEntity emailEntity = generateAdminEmailList();
            emailEntity.setEmailFrom(initConfig.getEmailFrom());
            UserInfo userInfo = taskInfo.getOwner();
            if(Objects.isNull(userInfo)){
                userInfo = userInfoService.getUserInfoById(taskInfo.getOwnerId());
            }
            String subject ="";
            if(emailType==1){
                subject = "(info) Topic [" + taskInfo.getTopicName() + "]  Approval Notice";
            }else {
                emailEntity.setEmailTo(userInfo.getEmail());
                if(emailType==2){
                    subject="(info) Topic [" + taskInfo.getTopicName() + "] Create Be Rejected ";
                }else {
                    subject="(info) Topic [" + taskInfo.getTopicName() + "] Create Success ";
                    List<TaskClusterVo> clusterMessList = clusterService.getClusterMessById(taskInfo.getClusterIds());
                    taskInfoVo.setClusterMessList(clusterMessList);
                }
            }
            emailEntity.setEmailSubject(subject);

            taskInfoVo.setApproveURL(initConfig.getKafkaCenterUrl());
            taskInfoVo.setOwner(userInfo.getName());
            BeanUtils.copyProperties(taskInfo, taskInfoVo);

            Map<String, Object> mailContentMap = new HashMap<>();
            mailContentMap.put("taskInfo", taskInfoVo);

            mailMap.put("emailEntity", emailEntity);
            mailMap.put("emailContent", mailContentMap);
        }catch (Exception e){
            LOGGER.error("get emailAllMessage  error.", e);
        }
        return mailMap;
    }

    private EmailEntity generateAdminEmailList(){
        EmailEntity emailEntity = new EmailEntity();
        List<String> emailList = userInfoService.selectEmailByRole(RoleEnum.ADMIN);
        StringBuilder stringBuilder = new StringBuilder();
        for (String email : emailList) {
            stringBuilder.append(email);
            stringBuilder.append(Constants.Symbol.SEMICOLON);
            String allAdminEmail = stringBuilder.substring(0, stringBuilder.length() - 1);
            emailEntity.setEmailTo(allAdminEmail);
        }
        return  emailEntity;
    }

//    public Map<String, Object> getEmailAllMessage(TaskInfo task, Integer emailType) throws InterruptedException, ExecutionException {
//        Map<String, Object> mailMap = new HashMap<>();
//        try {
//            TaskInfoVo taskInfoVo = new TaskInfoVo();
//            EmailEntity emailEntity = new EmailEntity();
//
//            String userEmail;
//            String allAdminEamil;
//            String owner;
//            if (null != task.getOwner()) {
//                userEmail = task.getOwner().getEmail();
//                owner = task.getOwner().getName();
//            } else {
//                userEmail = userInfoService.getUserInfoById(task.getOwnerId()).getEmail();
//                owner = userInfoService.getUserInfoById(task.getOwnerId()).getName();
//            }
//            List<String> emailList = userInfoService.selectEmailByRole(RoleEnum.ADMIN);
//            StringBuilder stringBuilder = new StringBuilder();
//            for (String email : emailList) {
//                stringBuilder.append(email);
//                stringBuilder.append(Constants.Symbol.SEMICOLON);
//            }
//            if (StringUtils.isNotBlank(stringBuilder)) {
//                allAdminEamil = stringBuilder.substring(0, stringBuilder.length() - 1);
//                emailEntity.setEmailTo(allAdminEamil);
//            }
//            emailEntity.setEmailFrom(initConfig.getEmailFrom());
//            if (emailType != 1) {
//                emailEntity.setEmailTo(userEmail);
//                if (emailType == 2) {
//                    emailEntity.setEmailSubject("(info) Topic [" + task.getTopicName() + "] Create Be Rejected ");
//                } else {
//                    emailEntity.setEmailSubject("(info) Topic [" + task.getTopicName() + "] Create Success ");
//                    List<TaskClusterVo> clusterMessList = clusterService.getClusterMessById(task.getClusterIds());
//                    taskInfoVo.setClusterMessList(clusterMessList);
//                }
//            } else {
//                emailEntity.setEmailSubject("(info) Topic [" + task.getTopicName() + "]  Approval Notice");
//            }
//
//            Map<String, Object> mailContentMap = new HashMap<>();
//            taskInfoVo.setApproveURL(initConfig.getKafkaCenterUrl());
//            taskInfoVo.setOwner(owner);
//            BeanUtils.copyProperties(task, taskInfoVo);
//            mailContentMap.put("taskInfo", taskInfoVo);
//
//            mailMap.put("emailEntity", emailEntity);
//            mailMap.put("emailContent", mailContentMap);
//
//        } catch (Exception e) {
//            LOGGER.error("get emailAllMessage  error.", e);
//        }
//        return mailMap;
//    }
}