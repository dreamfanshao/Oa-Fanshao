package com.atguigu.process.service.impl;

import com.atguigu.model.process.ProcessTemplate;

import com.atguigu.model.process.ProcessType;
import com.atguigu.process.mapper.OaProcessTemplateMapper;
import com.atguigu.process.service.OaProcessService;
import com.atguigu.process.service.OaProcessTemplateService;
import com.atguigu.process.service.OaProcessTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * 审批模板 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-03-20
 */
@Service
public class OaProcessTemplateServiceImpl extends ServiceImpl<OaProcessTemplateMapper, ProcessTemplate> implements OaProcessTemplateService {

    @Autowired
    private OaProcessTypeService ProcessTypeService;

    @Autowired
    private OaProcessService processService;
    //分页查询审批模板，把审批类型对应名称查询
    @Override
    public IPage<ProcessTemplate> selectPageProcessTemplate(Page<ProcessTemplate> pageParam) {

        //调用mapper方法实现分页查询
        Page<ProcessTemplate> processTemplatePage = baseMapper.selectPage(pageParam, null);

        //从分页数据获取列表list集合
        List<ProcessTemplate> processTemplateList = processTemplatePage.getRecords();
        //遍历list集合，得到每个对象的审批类型id
        for(ProcessTemplate processTemplate : processTemplateList){
            //得到每个对象的审批id
            Long processTypeId = processTemplate.getProcessTypeId();

            //根据审批类型id，查询获取对应名称
            LambdaQueryWrapper<ProcessType> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProcessType::getId,processTypeId);
            ProcessType processType = ProcessTypeService.getOne(wrapper);
            if(processType == null){
                continue;
            }

            //封装
            processTemplate.setProcessTypeName(processType.getName());
        }

        return processTemplatePage;

    }

    @Override
    public void publish(Long id) {
        //修改模板的发布状态，1为发布
        ProcessTemplate processTemplate = baseMapper.selectById(id);
        processTemplate.setStatus(1);
        baseMapper.updateById(processTemplate);

        //流程定义部署
        if(!StringUtils.isEmpty(processTemplate.getProcessDefinitionPath())){
            processService.deployByZip(processTemplate.getProcessDefinitionPath());
        }

    }

}
