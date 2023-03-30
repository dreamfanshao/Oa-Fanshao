package com.atguigu.wechat.controller;


import com.alibaba.fastjson.JSON;
import com.atguigu.auth.service.SysUserService;
import com.atguigu.common.jwt.JwtHelper;
import com.atguigu.common.result.Result;
import com.atguigu.model.system.SysUser;
import com.atguigu.vo.wechat.BindPhoneVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.ApiOperation;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Controller//进行页面调整,返回数据restcontroller
@RequestMapping("/admin/wechat")
@CrossOrigin//跨域
public class WechatController {
    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private WxMpService wxMpService;

    @Value("${wechat.userInfoUrl}")
    private String userInfoUrl;


    @GetMapping("/authorize")
    public String authorize(@RequestParam("returnUrl") String returnUrl, HttpServletRequest request) {
        //buildAuthorizationUrl:参数1：授权路径，在哪个路径获取微信信息
        //参数2：固定值：授权类型WxConsts.OAuth2Scope.SNSAPI_USERINFO
        //参数3：授权成功之后，跳转路径 'guiguoa'转换成'#'
        String redirectURL = null;
        try {
            redirectURL = wxMpService.getOAuth2Service()
                    .buildAuthorizationUrl(userInfoUrl,
                            WxConsts.OAuth2Scope.SNSAPI_USERINFO,
                            URLEncoder.encode(returnUrl.replace("guiguoa", "#"),"utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return "redirect:" + redirectURL;
    }

    @GetMapping("/userInfo")
    public String userInfo(@RequestParam("code") String code,
                           @RequestParam("state") String returnUrl) throws Exception {
        //获取accessToken
        WxOAuth2AccessToken accessToken = wxMpService.getOAuth2Service().getAccessToken(code);
        //使用accessToken获取openId
        String openId = accessToken.getOpenId();
        System.out.println(openId);
        //获取微信用户信息
        WxOAuth2UserInfo wxMpUser = wxMpService.getOAuth2Service().getUserInfo(accessToken, null);
        System.out.println(JSON.toJSONString(wxMpUser));
        //根据openId查询用户表
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getOpenId,openId);
        SysUser sysUser = sysUserService.getOne(wrapper);
        String token = "";
        //判断openId是否存在
        if(sysUser != null){
            token = JwtHelper.createToken(sysUser.getId(),sysUser.getUsername());
        }

        if(returnUrl.indexOf("?") == -1) {
            return "redirect:" + returnUrl + "?token=" + token + "&openId=" + openId;
        } else {
            return "redirect:" + returnUrl + "&token=" + token + "&openId=" + openId;
        }
    }



    @PostMapping("bindPhone")
    @ResponseBody//返回json数据
    public Result bindPhone(@RequestBody BindPhoneVo bindPhoneVo) {
        //根据手机号查询数据库
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getPhone,bindPhoneVo.getPhone());
        SysUser sysUser = sysUserService.getOne(wrapper);
        //如果存在，更新openId
        if(sysUser!=null){
            sysUser.setOpenId(bindPhoneVo.getOpenId());
            sysUserService.updateById(sysUser);
            String token = JwtHelper.createToken(sysUser.getId(),sysUser.getName());
            return  Result.ok(token);
        }else {
            return  Result.fail("手机号不存在，请联系管理员修改");
        }
    }
}
