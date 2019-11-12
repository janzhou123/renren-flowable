package io.renren.modules.flowable.controller;

import com.alibaba.fastjson.JSONObject;
import io.renren.common.utils.R;
import io.renren.modules.flowable.contants.ExpenseContantsEnum;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.flowable.engine.HistoryService;
import org.flowable.engine.IdentityService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * @Author: 出差报销流程
 * @Date: 2019/11/5 2:50 PM
 */
@Controller
@RequestMapping(value = "app/expense")
public class ExpenseController {

  private static Logger logger = LoggerFactory.getLogger(ExpenseController.class);
  @Autowired
  private RuntimeService runtimeService;
  @Autowired
  private TaskService taskService;
  @Autowired
  private RepositoryService repositoryService;

  @Autowired
  private ProcessEngine processEngine;
  @Autowired
  private IdentityService identityservice;
  @Autowired
  private HistoryService historyService;

  /**
   * 添加报销
   *
   * @param userId 用户Id
   * @param money 报销金额
   * @param descption 描述
   */
  @RequestMapping(value = "add")
  @ResponseBody
  public R addExpense(String userId, Integer money, String descption) {
    logger.info("========业务操作 开始=");
    //业务主键
    String businesskey = String.valueOf(System.currentTimeMillis());
    //业务操作
    /**
     * TO DO......
     */
    logger.info("========业务操作 结束=");
    //设置流程发起人
    identityservice.setAuthenticatedUserId(userId);
    //启动流程
    HashMap<String, Object> map = new HashMap<>();
    map.put(ExpenseContantsEnum.ARG_TASK_USER.getCode(), userId);
    map.put(ExpenseContantsEnum.ARG_MONEY.getCode(), money);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
        ExpenseContantsEnum.FLOW_NAME.getCode(), businesskey, map);

    logger.info("========businesskey=" + businesskey);
    logger.info("========instanceid=" + processInstance.getId());//流程主键ID

    /**
     * TO DO 流程主键ID 保存到业务表
     *
     */
    HashMap<String, Object> returnMap = new HashMap<>();
    returnMap.put("businesskey", businesskey);
    returnMap.put("instanceid", processInstance.getId());

    return R.ok(returnMap);
  }


  /**
   * 获取审批管理列表
   */
  @RequestMapping(value = "/list")
  @ResponseBody
  public Object list(String userId) {
    List<Task> tasks = taskService.createTaskQuery().taskAssignee(userId).orderByTaskCreateTime()
        .desc().list();

    ArrayList returnList = new ArrayList();
    for (Task task : tasks) {
      HashMap<String, Object> returnMap = new HashMap<>();
      logger.info("======runtimeService.getVariables" + task.toString());

      returnMap.put("task_id", task.getId());
      returnMap.put("task_name", task.getName());
      returnMap.put("task_time", task.getCreateTime());
      returnMap.put("processInstance", task.getProcessInstanceId());
      returnMap.put("ExecutionId", task.getExecutionId());

      returnList.add(returnMap);

      logger.info("======runtimeService.getVariables" + JSONObject
          .toJSONString(runtimeService.getVariables(task.getExecutionId())));

      logger.info(
          "======historyService.createHistoricProcessInstanceQuery" + JSONObject
              .toJSONString(historyService.createHistoricProcessInstanceQuery()
              .processInstanceId(task.getProcessInstanceId()).list()));

    }
    return returnList;
  }

  /**
   * 批准
   *
   * @param taskId 任务ID
   */
  @RequestMapping(value = "apply")
  @ResponseBody
  public R apply(String taskId, String comment) {
    try {
      Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
      if (task == null) {
        throw new RuntimeException("流程不存在");
      }

      //添加审批说明
      taskService.addComment(task.getId(), task.getProcessInstanceId(), comment);
      //这里演示 设置runtime变量
      runtimeService.setVariable(task.getExecutionId(), ExpenseContantsEnum.NEXT_PERSON.getCode(),
          ExpenseContantsEnum.ARG_MANAGER_ADUIT.getCode());

      logger.info("======runtimeService.getVariables" + JSONObject
          .toJSONString(runtimeService.getVariables(task.getExecutionId())));
      //通过审核
      HashMap<String, Object> map = new HashMap<>();
      map.put("outcome", "通过");
      taskService.complete(taskId, map);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return R.ok("处理完成");
  }

}
