package io.renren.modules.flowable.contants;

import lombok.Getter;

/**
 * 出差报销流程 常量定义
 *
 * @Author: Jack Zhou
 * @Date: 2019/11/6 8:12 AM
 */
@Getter
public enum ExpenseContantsEnum {
  FLOW_NAME("Expense", "出差报销流程ID"),//对应 ExpenseProcess.bpmn20.xml 里面的process id
  ARG_TASK_USER("taskUser", "出差报销节点 分配人"),
  ARG_MONEY("money","出差金额"),
  ARG_MANAGER_ADUIT("manager","经理审批节点 审批人"),
  ARG_BOSS_ADUIT("boss","老板审批节点 审批人"),
  NEXT_PERSON("NEXT_PERSON","下一节点审批参数名"),
  ARG_OUTCOME("outcome","通过与否标记")
    ;


  private String code;
  private String desc;

  ExpenseContantsEnum(String code, String desc) {
    this.code = code;
    this.desc = desc;
  }

}
