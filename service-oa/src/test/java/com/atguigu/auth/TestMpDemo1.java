package com.atguigu.auth;


import com.atguigu.auth.mapper.SysRoleMapper;
import com.atguigu.model.system.SysRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class TestMpDemo1 {

    //注入
    @Autowired
    private SysRoleMapper mapper;

    //查询所有记录
    @Test
    public void getAll(){
        List<SysRole> list = mapper.selectList(null);
        System.out.println(list);
    }

    @Test
    public void add(){
        SysRole sysRole = new SysRole();
        sysRole.setRoleName("角色管理员");
        sysRole.setRoleCode("role");
        sysRole.setDescription("角色管理员");
        int rows = mapper.insert(sysRole);
        System.out.println(rows);
        System.out.println(sysRole.getId());
    }

    @Test
    public void update(){

        SysRole role = mapper.selectById(10);

        role.setRoleName("atguigu管理员");

        int rows = mapper.updateById(role);

        System.out.println(rows);
    }

    @Test
    public void deleteId(){
        int rows = mapper.deleteById(10);
    }

    //批量删除
    @Test
    public void testDeleteBatchIds() {
        int result = mapper.deleteBatchIds(Arrays.asList(1, 2));
        System.out.println(result);
    }

    //QueryWrapper ： Entity 对象封装操作类，不是用lambda语法
    @Test
    public void testQuery1(){
        QueryWrapper<SysRole> wrapper = new QueryWrapper<>();
        wrapper.eq("role_name","总经理");
        List<SysRole> list = mapper.selectList(wrapper);
        System.out.println(list);
    }


    //LambdaQueryWrapper ：看名称也能明白就是用于Lambda语法使用的查询Wrapper
    @Test
    public void testQuery2(){
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleName,"总经理");
        List<SysRole> list = mapper.selectList(wrapper);
        System.out.println(list);
    }
}
