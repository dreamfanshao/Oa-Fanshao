package com.atguigu.auth.service.impl;

import com.atguigu.auth.mapper.SysRoleMapper;
import com.atguigu.auth.service.SysRoleService;
import com.atguigu.auth.service.SysUserRoleService;
import com.atguigu.auth.service.SysUserService;
import com.atguigu.model.system.SysRole;
import com.atguigu.model.system.SysUserRole;
import com.atguigu.vo.system.AssginRoleVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole>implements SysRoleService {

    @Autowired
    private SysUserRoleService sysUserRoleService;
    @Override
    public Map<String, Object> findRoleByAdminId(Long userId) {
        //查询所有的角色，返回list集合
        List<SysRole> allRoleList = baseMapper.selectList(null);
        
        //根据userid查询角色用户关系表，查询userid对应所有角色id
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId,userId);
        List<SysUserRole> existUserRoleList = sysUserRoleService.list(wrapper);
        //从查询出的用户id对应角色list集合，获取所有角色id
        List<Long> existRoleIdList =
                existUserRoleList.stream().map(c -> c.getRoleId()).collect(Collectors.toList());
        //根据查询出的所有的角色id，找出对应的角色信息
        //根据角色id到所有的角色的list集合进行比较
        List<SysRole> assignRoleList = new ArrayList<>();
        for(SysRole sysRole : allRoleList){
            //比较
            if(existRoleIdList.contains(sysRole.getId())){
                assignRoleList.add(sysRole);
            }
        }
        //把得到的两部分数据封装map集合
        Map<String, Object> roleMap = new HashMap<>();
        roleMap.put("assginRoleList", assignRoleList);
        roleMap.put("allRolesList", allRoleList);
        return roleMap;

    }

    @Override
    public void doAssign(AssginRoleVo assginRoleVo) {
        //把用户之前分配角色数据删除,根据用户关系表userid删除
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId,assginRoleVo.getUserId());
        sysUserRoleService.remove(wrapper);
        //重新分配
        List<Long> roleIdList = assginRoleVo.getRoleIdList();
        for(Long roleId:roleIdList){
            if(StringUtils.isEmpty(roleId)){
                continue;
            }
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setUserId(assginRoleVo.getUserId());
            sysUserRole.setRoleId(roleId);
            sysUserRoleService.save(sysUserRole);
        }
    }
}
