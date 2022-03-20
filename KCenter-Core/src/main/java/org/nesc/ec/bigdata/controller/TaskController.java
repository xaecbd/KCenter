package org.nesc.ec.bigdata.controller;

import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.common.BaseController;
import org.nesc.ec.bigdata.common.RestResponse;
import org.nesc.ec.bigdata.common.RoleEnum;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.constant.TopicConfig;
import org.nesc.ec.bigdata.job.InitRunJob;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.nesc.ec.bigdata.model.TaskInfo;
import org.nesc.ec.bigdata.model.UserInfo;
import org.nesc.ec.bigdata.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Truman.P.Du
 * @version 1.0
 * @date 2019年4月16日 上午9:58:18
 */
@RestController
@RequestMapping("topic/task")
public class TaskController extends BaseController {
    private static final Logger LOG = LoggerFactory.getLogger(TaskController.class);

    public static final Long REJECT = -1L;
    public static final Long CHECK = 1L;
    public static final Long NORMAL = 0L;

    @Autowired
    TaskInfoService taskInfoService;

    @Autowired
    ClusterService clusterService;

    @Autowired
    TopicInfoService topicInfoService;

    @Autowired
    UserInfoService userInfoService;

    @Autowired
    EmailService emailService;

    @Value("${mail.enable:true}")
    private Boolean mailEnable;

    @Autowired
    InitRunJob initRunJob;

    /**return the task list according to taskId*/
    @GetMapping("/get")
    @ResponseBody
    public RestResponse getTasksById(@RequestParam Long id) {
        try {
            TaskInfo task = taskInfoService.selectById(id);
            List<ClusterInfo> clusters = clusterService.getTotalData();
            encapsulateCluster(task, clusters);
            return SUCCESS_DATA(task);
        } catch (Exception e) {
            LOG.error("Get task by id error.", e);
            return ERROR("Get task data by id failed.");
        }
    }

    /**
     * 将Task需要创建的Cluster的名字用“，”隔开，放到clusterNames字段中。
     *
     * @param task
     * @param clusters
     */
    private void encapsulateCluster(TaskInfo task, List<ClusterInfo> clusters) {
        if (null == clusters || clusters.size() == 0) {
            return;
        }

        List<Object> clusterIds = new ArrayList<>();
        for (ClusterInfo cluster : clusters) {
            clusterIds.add(String.valueOf(cluster.getId()));
        }

        if (task.getClusterIds() != null) {
            StringBuilder clusterNames = new StringBuilder();
            String[] ids = task.getClusterIds().split(Constants.Symbol.COMMA);
            for (String id : ids) {
                if (clusterIds.contains(id)) {
                    clusters.forEach(cluster -> {
                                if (id.equals(cluster.getId().toString())) {
                                    clusterNames.append(", ").append(cluster.getName());
                                }
                            }
                    );
                } else {
                    task.setClusterNames(null);
                    return;
                }
            }
            if (clusterNames.length() != 0) {
                task.setClusterNames(clusterNames.substring(2));
            }
        }
    }

    /**return the task list by user role*/
    @GetMapping("/list")
    @ResponseBody
    public RestResponse getTasks() {
        try {
            List<TaskInfo> tasks = null;
            UserInfo user = this.getCurrentUser();
            List<ClusterInfo> clusters = clusterService.getTotalData();
            if (RoleEnum.ADMIN.getDescription().equals(user.getRole().getDescription())) {
                tasks = taskInfoService.getTotalData();
                tasks.forEach(task -> encapsulateCluster(task, clusters));
            } else {
                Long ownerId = user.getId();
                tasks = taskInfoService.getTasksByOwnerID(ownerId);
                tasks.forEach(task -> encapsulateCluster(task, clusters));
            }
            return SUCCESS_DATA(tasks);
        } catch (Exception e) {
            LOG.error("Find task list error.", e);
            return ERROR("Get data failed!");
        }
    }

    /**add task information to task table
     * if add task success,send the email to admin
     * */
    @PostMapping("/add")
    @ResponseBody
    public RestResponse add(@RequestBody TaskInfo task) {
        try {
            UserInfo user = getCurrentUser();
            if (user != null) {
                task.setOwnerId(user.getId());
            }
            if (taskInfoService.insert(task)) {
                if (mailEnable) {
                    Map<String, Object> emailMap = taskInfoService.getSendEmailInfo(task, 1);
                    initRunJob.sendEmail(emailMap,1);
                }

                return SUCCESS("Add task data success.");
            } else {
                return ERROR("Add task data failed.");
            }
        } catch (Exception e) {
            LOG.error("Add task error.", e);
            return ERROR(e.getMessage());
        }
    }

    /**update task information ,if update task success,send the email to admin*/
    @PutMapping("update")
    @ResponseBody
    public RestResponse update(@RequestBody TaskInfo task) {
        try {
            TaskInfo tasks = taskInfoService.selectById(task.getId());
            if (CHECK.equals(tasks.getApproved())) {
                return ERROR("The task is been reviewed,cannot be updated.");
            } else {
                UserInfo user = getCurrentUser();
                if (user != null) {
                    task.setOwnerId(user.getId());
                }
                task.setApprovedId(null);
                task.setApprovalOpinions("");
                if (taskInfoService.updateTask(task)) {
                    if (mailEnable) {
                        Map<String, Object> emailMap = taskInfoService.getSendEmailInfo(task, 1);
                        initRunJob.sendEmail(emailMap,1);
                    }
                    return SUCCESS("成功更新申请.");
                } else {
                    return ERROR("更新申请失败！");
                }
            }
        } catch (Exception e) {
            LOG.error("更新申请失败！", e);
            return ERROR(e.getMessage());
        }
    }

    /**delete the task,
     * if task is approved,return error,
     * else delete task from task table
     * */
    @DeleteMapping("/{id}")
    @ResponseBody
    public RestResponse delete(@PathVariable Long id) {
        try {
            TaskInfo task = taskInfoService.selectById(id);
            if (CHECK.equals(task.getApproved())) {
                return ERROR("The task is been reviewed,cannot be deleted.");
            } else {
                if (taskInfoService.delete(id)) {
                    return SUCCESS("Delete task data success.");
                } else {
                    return ERROR("Delete task data failed.");
                }
            }
        } catch (Exception e) {
            LOG.error("Delete task error.", e);
            return ERROR("DELETE TASK DATA FAILED!");
        }

    }

    /**
     * 审批时 选择topic所在集群
     * 同时可以选择性改变用户设置的partition和replicas
     *
     * @return
     */
    @PostMapping("/approve")
    public RestResponse approve(@RequestBody Map<String, String> queryMap) {
        Integer partition = Integer.valueOf(queryMap.get(TopicConfig.PARTITION));
        Short replication = Short.valueOf(queryMap.get(TopicConfig.REPLICATION));
        String clusterIds = queryMap.get(Constants.KeyStr.LOWER_CLUSTER_ID);
        Long id = Long.valueOf(queryMap.get(Constants.JsonObject.ID));
        try {
            TaskInfo task = taskInfoService.queryById(id);
            /**
             * 判断topic是否已存在
             */
            if (!taskInfoService.checkTopicIsExist(task, clusterIds)) {
                task.setClusterIds(clusterIds);
                task.setPartition(partition);
                task.setReplication(replication);
                taskInfoService.update(task);
                if (task.getApproved() == 0) {
                    // 如果下面出异常，将异常信息返回给前台。
                    JSONObject obj = topicInfoService.encapsulatedObject(task);
                    if (!obj.getBooleanValue(Constants.TRUE)) {
                        return ERROR(obj.getString(Constants.KeyStr.MESSAGE));
                    }
                    UserInfo user = getCurrentUser();
                    if (user != null) {
                        task.setApprovedId(user.getId());
                    }
                    task.setApproved(CHECK);
                    task.setApprovedTime(new Date());
                    if (taskInfoService.update(task)) {
                        if (mailEnable) {
                            Map<String, Object> emailMap = taskInfoService.getSendEmailInfo(task, 3);
                            initRunJob.sendEmail(emailMap,3);
                        }
                        return SUCCESS("成功通过创建申请，Topic将被创建。");
                    } else {
                        return ERROR("失败，未能通过申请。");
                    }
                } else {
                    return ERROR("这一申请已被处理过了！");
                }
            } else {
                return ERROR("申请不存在！");
            }
        } catch (Exception e) {
            LOG.error("Approve task error.", e);
            if (null == e.getMessage()) {
                return ERROR("Approve task failed");
            } else {
                return ERROR(" Approve task failed : " + e.getMessage());
            }

        }
    }

    /**reject task and send the email to task owner */
    @PostMapping("/reject/{id}")
    @ResponseBody
    public RestResponse reject(@PathVariable Long id, @RequestBody Map<String, String> queryMap) {
        try {
            String approvalComments = queryMap.get(Constants.KeyStr.APPROVAL_COMMENTS);
            TaskInfo task = taskInfoService.selectById(id);
            UserInfo user = getCurrentUser();
            if (user != null) {
                task.setApprovedId(user.getId());
            }
            task.setApproved(REJECT);
            task.setApprovedTime(new Date());
            task.setApprovalOpinions(approvalComments);
            if (taskInfoService.update(task)) {
                if (mailEnable) {
                    Map<String, Object> emailMap = taskInfoService.getSendEmailInfo(task, 2);
                    initRunJob.sendEmail(emailMap,2);
                }
                return SUCCESS("拒绝申请成功.");
            } else {
                return ERROR("拒绝失败！");
            }
        } catch (Exception e) {
            LOG.error("Reject task error.", e);
            return ERROR("拒绝失败！");
        }

    }
}
