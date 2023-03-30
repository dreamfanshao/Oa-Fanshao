package com.atguigu.process.service.impl;

import com.atguigu.model.process.ProcessTemplate;
import com.atguigu.model.process.ProcessType;

import com.atguigu.process.mapper.OaProcessTypeMapper;
import com.atguigu.process.service.OaProcessTemplateService;
import com.atguigu.process.service.OaProcessTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-03-20
 */
@Service
public class OaProcessTypeServiceImpl extends ServiceImpl<OaProcessTypeMapper, ProcessType> implements OaProcessTypeService {

    @Autowired
    private OaProcessTemplateService processTemplateService;
    //查询所有审批分类和每个分类所有审批模板
    @Override
    public List<ProcessType> findProcessType() {
        //查询所有审批分类，返回list
        List<ProcessType> processTypeList = baseMapper.selectList(null);
        //遍历list集合
        for(ProcessType processType:processTypeList){
            //得到每个审批分类，根据审批分类id查询对应审批模板
            //审批分类id
            Long typeId = processType.getId();
            //根据审批分类id查询对应审批模板
            LambdaQueryWrapper<ProcessTemplate> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProcessTemplate::getProcessTypeId,typeId);
            List<ProcessTemplate> processTemplateList = processTemplateService.list(wrapper);
            //将审批模板数据list封装到每个审批分类对象里
            processType.setProcessTemplateList(processTemplateList);

        }
        return processTypeList;
    }
}
