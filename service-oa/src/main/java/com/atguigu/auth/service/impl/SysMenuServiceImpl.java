package com.atguigu.auth.service.impl;


import com.atguigu.auth.mapper.SysMenuMapper;
import com.atguigu.auth.service.SysMenuService;
import com.atguigu.auth.service.SysRoleMenuService;
import com.atguigu.auth.utils.MenuHelper;
import com.atguigu.common.config.exception.GuiguException;
import com.atguigu.model.system.SysMenu;
import com.atguigu.model.system.SysRoleMenu;
import com.atguigu.vo.system.AssginMenuVo;
import com.atguigu.vo.system.MetaVo;
import com.atguigu.vo.system.RouterVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-03-08
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {
    @Autowired
    private SysRoleMenuService sysRoleMenuService;
    @Override
    public List<SysMenu> findNodes() {
        //查询所有菜单数据
        List<SysMenu> sysMenuList = baseMapper.selectList(null);
        //构建成树形结构
        List<SysMenu> resultList = MenuHelper.buildTree(sysMenuList);
        return resultList;
    }

    //删除菜单
    @Override
    public void removeMenuById(Long id) {
        //判断当前菜单是否有下一层菜单
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getParentId,id);
        Integer count = baseMapper.selectCount(wrapper);
        if (count > 0){
            throw new GuiguException(201,"菜单不能删除");
        }
        baseMapper.deleteById(id);
    }

    @Override
    public List<SysMenu> finMenuByRoleId(Long roleId) {
        //查询所有菜单-条件status=1
        LambdaQueryWrapper<SysMenu> wrapperSysMenu = new LambdaQueryWrapper<>();
        wrapperSysMenu.eq(SysMenu::getStatus,1);
        List<SysMenu> allSysMenuList = baseMapper.selectList(wrapperSysMenu);
        //根据角色id roleId查询 角色菜单关系表里面 角色id对应所有的菜单id
        LambdaQueryWrapper<SysRoleMenu> wrapperSysRoleMenu = new LambdaQueryWrapper<>();
        wrapperSysRoleMenu.eq(SysRoleMenu::getRoleId,roleId);
        List<SysRoleMenu> sysRoleMenuList = sysRoleMenuService.list(wrapperSysRoleMenu);
        //根据获取的菜单id，获取对应菜单对象
        List<Long> menuIdList = sysRoleMenuList.stream().map(c -> c.getMenuId()).collect(Collectors.toList());
        //用菜单id和所有菜单id集合比较，相同即封装
        allSysMenuList.stream().forEach(item->{
            if(menuIdList.contains(item.getId())){
                item.setSelect(true);
            }else {
                item.setSelect(false);
            }
        });
        //返回规定树形显示格式菜单列表
        List<SysMenu> sysMenuList = MenuHelper.buildTree(allSysMenuList);
        return sysMenuList;
    }

    @Override
    public void doAssign(AssginMenuVo assginMenuVo) {
        //根据角色id 删除菜单角色表里分配的数据
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId,assginMenuVo.getRoleId());
        sysRoleMenuService.remove(wrapper);
        //从参数中获取角色新分配的菜单id列表，进行遍历，把每个id数据添加菜单角色表里
        List<Long> menuIdList = assginMenuVo.getMenuIdList();
        for(Long menuId:menuIdList){
            if(StringUtils.isEmpty(menuId)){
                continue;
            }
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setMenuId(menuId);
            sysRoleMenu.setRoleId(assginMenuVo.getRoleId());
            sysRoleMenuService.save(sysRoleMenu);
        }
    }

    //4根据用户id获取用户可以操作的菜单列表
    @Override
    public List<RouterVo> findUserMenuListByUserId(Long userId) {
        List<SysMenu> sysMenuList = null;
        //1判断当前用户是否是管理员   userId=1是管理员
        //如果是管理员，查询所有菜单列表
        if(userId.longValue()==1){
            LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getStatus,1);
            wrapper.orderByAsc(SysMenu::getSortValue);
            sysMenuList = baseMapper.selectList(wrapper);
        }else {
            //如果不是管理员，根据userId查询可以操作的菜单列表
            //多表关联查询：用户角色关系表，角色菜单关系表，菜单表
            sysMenuList = baseMapper.findMenuListByUserId(userId);
        }

        //2把查询出的数据列表-构建成框架要求的路由数据结构
        //使用菜单操作工具类构建树形结构
        List<SysMenu> sysMenuTreeList = MenuHelper.buildTree(sysMenuList);
        //构建成框架要求的路由结构
        List<RouterVo> routerList = this.buildRouter(sysMenuTreeList);
        return routerList;
    }
    //构建成框架要求的路由结构
    private List<RouterVo> buildRouter(List<SysMenu> menus) {
        //创建list集合，存储最终数据
        List<RouterVo> routers = new ArrayList<>();
        //遍历menus
        for(SysMenu menu:menus){
            RouterVo router = new RouterVo();
            router.setHidden(false);
            router.setAlwaysShow(false);
            router.setPath(getRouterPath(menu));
            router.setComponent(menu.getComponent());
            router.setMeta(new MetaVo(menu.getName(), menu.getIcon()));
            //下一层数据部分
            List<SysMenu> children = menu.getChildren();
            if (menu.getType().intValue()==1){
                //加载下面隐藏路由
                List<SysMenu> hiddenMenuList = children.stream()
                        .filter(item -> !StringUtils.isEmpty(item.getComponent())).collect(Collectors.toList());
                for(SysMenu hiddenMenu : hiddenMenuList){
                    RouterVo hiddenRouter = new RouterVo();
                    //true隐藏路由
                    hiddenRouter.setHidden(true);
                    hiddenRouter.setAlwaysShow(false);
                    hiddenRouter.setPath(getRouterPath(hiddenMenu));
                    hiddenRouter.setComponent(hiddenMenu.getComponent());
                    hiddenRouter.setMeta(new MetaVo(hiddenMenu.getName(), hiddenMenu.getIcon()));
                    routers.add(hiddenRouter);
                }
            }else {
//                if(CollectionUtils.isEmpty(children)){
//                    //递归
//                    router.setChildren(buildRouter(children));
//                }
                if (!CollectionUtils.isEmpty(children)) {
                    if(children.size() > 0) {
                        router.setAlwaysShow(true);
                    }
                    // 递归
                    router.setChildren(buildRouter(children));
                }
            }
            routers.add(router);
        }
        return routers;
    }

    public String getRouterPath(SysMenu menu) {
        String routerPath = "/" + menu.getPath();
        if(menu.getParentId().intValue() != 0) {
            routerPath = menu.getPath();
        }
        return routerPath;
    }

    //5根据用户id获取用户可以操作按钮列表
    @Override
    public List<String> findUserPermsByUserId(Long userId) {
        //1判断当前用户是否是管理员   userId=1是管理员
        List<SysMenu> sysMenuList = null;
        if(userId.longValue()==1){
            //如果是管理员，查询所有按钮列表
            LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getStatus,1);

            sysMenuList = baseMapper.selectList(wrapper);
        }else {
            //如果不是管理员，根据userId查询可以操作的按钮列表
            //多表关联查询：用户角色关系表，角色菜单关系表，菜单表
            sysMenuList = baseMapper.findMenuListByUserId(userId);
        }
        //从查询出来的数据里，获取可以操作按钮值的list集合，返回
        List<String> permsList = sysMenuList.stream().filter(item -> item.getType() == 2)
                .map(item -> item.getPerms()).collect(Collectors.toList());
        return permsList;
    }
}
