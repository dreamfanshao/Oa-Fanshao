package com.atguigu.auth.activiti;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class ProcessTest {

    //注入repositoryService
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    //查询个人的代办任务
    @Test
    public void findTaskList(){
        String assgin = "zhangsan";
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee(assgin).list();

    }

    //启动流程实例
    @Test
    public void startProcess(){
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("qingjia");
        System.out.println("流程定义id:"+processInstance.getProcessDefinitionId());
        System.out.println("流程实例id:"+processInstance.getId());
        System.out.println("流程活动id:"+processInstance.getActivityId());
    }


    //单个文件部署
    @Test
    public void deployProcess(){
        //流程部署
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("process/qingjia.bpmn20.xml")
                .addClasspathResource("process/qingjia.png")
                .name("请假申请流程")
                .deploy();
        System.out.println(deploy.getId());
        System.out.println(deploy.getName());
    }
}
