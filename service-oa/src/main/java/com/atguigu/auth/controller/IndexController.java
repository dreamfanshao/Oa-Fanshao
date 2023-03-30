package com.atguigu.auth.controller;


import com.atguigu.auth.service.SysMenuService;
import com.atguigu.auth.service.SysUserService;
import com.atguigu.common.config.exception.GuiguException;
import com.atguigu.common.jwt.JwtHelper;
import com.atguigu.common.result.Result;
import com.atguigu.common.util.MD5;
import com.atguigu.model.system.SysUser;
import com.atguigu.vo.system.LoginVo;
import com.atguigu.vo.system.RouterVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "后台登录管理")//表明是knife4j资源
@RestController//@Controller + @ResponseBody。控制器类，返回json数据
@RequestMapping("/admin/system/index")//标识类：设置映射请求的请求路径的初始信息，与前端保持一至
public class IndexController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysMenuService sysMenuService;
    //post
    //1. 提交用户注册信息。
    //2. 提交修改的用户信息。
    //login
    @PostMapping("login")
    public Result login(@RequestBody LoginVo loginVo){
//        Map<String,Object> map = new HashMap<>();
//        map.put("token","admin-token");
//        return Result.ok(map);
        //1获取输入用户名和密码
        //2根据用户名查数据库
        String username = loginVo.getUsername();
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername,username);
        SysUser sysUser = sysUserService.getOne(wrapper);

        //3判断用户信息是否存在
        if(sysUser==null){
            throw new GuiguException(201,"用户不存在");
        }

        //4判断密码
        //数据库存储的密码
        String password_db = sysUser.getPassword();
        //获取输入的密码
        String password_input = MD5.encrypt(loginVo.getPassword());

        if(!password_db.equals(password_input)){
            throw new GuiguException(201,"密码错误");
        }
        //5判断用户是否被禁用   1 可用  0 禁用
        if(sysUser.getStatus().intValue()==0){
            throw  new GuiguException(201,"用户被禁用，请联系管理员。");
        }
        //6使用jwt根据用户id和用户名称生成token字符串
        String token = JwtHelper.createToken(sysUser.getId(), sysUser.getUsername());

        //7返回
        Map<String,Object> map = new HashMap<>();
        map.put("token",token);
        return Result.ok(map);
    }
    //get
    //1. 登录时GET获取服务器数据库用户名和密码进行验证。
    //2.下载文本、图片、音视频等时获取服务器资源。
    //info信息
    @GetMapping("info")
    public Result info(HttpServletRequest request){
        //1从请求头获取用户信息（获取请求头token字符串）
        String token = request.getHeader("token");
        //2从token字符串获取用户id 或 用户名称
        Long userId = JwtHelper.getUserId(token);
        //3根据用户id查询数据库，把用户信息获取出来
        SysUser sysUser = sysUserService.getById(userId);
        //4根据用户id获取用户可以操作的菜单列表
        //查询数据库动态构建路由结构，进行显示
        List<RouterVo> routerList = sysMenuService.findUserMenuListByUserId(userId);
        //5根据用户id获取用户可以操作按钮列表
        List<String> permsList = sysMenuService.findUserPermsByUserId(userId);
        //6返回响应的数据
        Map<String, Object> map = new HashMap<>();
        map.put("roles","[admin]");
        map.put("name",sysUser.getName());
        map.put("avatar","https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
        //返回用户可以操作菜单
        map.put("routers",routerList);
        //返回用户可以操作按钮
        map.put("buttons",permsList);
        return Result.ok(map);
    }

    //logout
    @PostMapping("logout")
    public Result logout(){
        return Result.ok();
    }
}
