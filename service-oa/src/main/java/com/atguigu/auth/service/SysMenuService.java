package com.atguigu.auth.service;


import com.atguigu.model.system.SysMenu;
import com.atguigu.vo.system.AssginMenuVo;
import com.atguigu.vo.system.RouterVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 菜单表 服务类
 * </p>
 *
 * @author atguigu
 * @since 2023-03-08
 */
public interface SysMenuService extends IService<SysMenu> {

    List<SysMenu> findNodes();

    void removeMenuById(Long id);

    List<SysMenu> finMenuByRoleId(Long roleId);

    void doAssign(AssginMenuVo assginMenuVo);

    //4根据用户id获取用户可以操作的菜单列表
    List<RouterVo> findUserMenuListByUserId(Long userId);

    //5根据用户id获取用户可以操作按钮列表
    List<String> findUserPermsByUserId(Long userId);
}
