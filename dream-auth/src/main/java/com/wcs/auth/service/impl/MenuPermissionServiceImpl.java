package com.wcs.auth.service.impl;

import cn.hutool.core.util.PageUtil;
import com.wcs.auth.service.MenuPermissionService;
import com.wcs.common.cache.constant.CacheNames;
import com.wcs.common.dto.PageDTO;
import com.wcs.common.response.ResponseResult;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author: wcs
 * @create: 2023-01-09 9:49
 * @description:
 */
@Service
public class MenuPermissionServiceImpl implements MenuPermissionService {
//
//    @Autowired
//    private MenuPermissionMapper menuPermissionMapper;
//
//
//    @Override
//    public PageVO<MenuPermissionVO> page(PageDTO pageDTO, Integer sysType) {
//        return PageUtil.doPage(pageDTO, () -> menuPermissionMapper.list(sysType));
//    }
//
//    @Override
//    public MenuPermissionVO getByMenuPermissionId(Long menuPermissionId) {
//        return menuPermissionMapper.getByMenuPermissionId(menuPermissionId);
//    }
//
//    @Override
//    @CacheEvict(cacheNames = CacheNames.URI_PERMISSION_KEY, key = "#menuPermission.bizType")
//    public ResponseResult<Void> save(MenuPermission menuPermission) {
//
//        MenuPermission dbMenuPermission = menuPermissionMapper.getByPermission(menuPermission.getPermission(), AuthUserContext.get().getSysType());
//        if (dbMenuPermission != null) {
//            return ResponseResult.showFailMsg("权限编码已存在，请勿重复添加");
//        }
//        menuPermissionMapper.save(menuPermission);
//        return ResponseResult.success();
//    }
//
//    @Override
//    @CacheEvict(cacheNames = CacheNames.URI_PERMISSION_KEY, key = "#menuPermission.bizType")
//    public ResponseResult<Void> update(MenuPermission menuPermission) {
//        MenuPermission dbMenuPermission = menuPermissionMapper.getByPermission(menuPermission.getPermission(),AuthUserContext.get().getSysType());
//        if (dbMenuPermission != null && !Objects.equals(menuPermission.getMenuPermissionId(), dbMenuPermission.getMenuPermissionId())) {
//            return ResponseResult.showFailMsg("权限编码已存在，请勿重复添加");
//        }
//        menuPermissionMapper.update(menuPermission);
//        return ResponseResult.success();
//    }
//
//    @Override
//    @CacheEvict(cacheNames = CacheNames.URI_PERMISSION_KEY, key = "#sysType")
//    public void deleteById(Long menuPermissionId, Integer sysType) {
//        menuPermissionMapper.deleteById(menuPermissionId,sysType);
//    }
//
//    @Override
//    @Caching(evict = {
//            @CacheEvict(cacheNames = CacheNames.USER_PERMISSIONS_KEY, key = "#sysType + ':' + #userId"),
//            @CacheEvict(cacheNames = CacheNames.MENU_ID_LIST_KEY, key = "#userId")
//    })
//    public void clearUserPermissionsCache(Long userId, Integer sysType) {
//    }
//
//    @Override
//    public List<String> listUserPermissions(Long userId, Integer sysType, boolean isAdmin) {
//        MenuPermissionServiceImpl menuPermissionService = (MenuPermissionServiceImpl) AopContext.currentProxy();
//        List<String> permsList;
//
//        // 系统管理员，拥有最高权限
//        if (isAdmin) {
//            permsList = menuPermissionService.listAllPermission(sysType);
//        }
//        else {
//            permsList = menuPermissionService.listPermissionByUserIdAndSysType(userId, sysType);
//        }
//        return permsList;
//    }
//
//    @Override
//    @Cacheable(cacheNames = CacheNames.URI_PERMISSION_KEY, key = "#sysType")
//    public List<UriPermissionBO> listUriPermissionInfo(Integer sysType) {
//        return menuPermissionMapper.listUriPermissionInfo(sysType);
//    }
//
//    @Override
//    public List<MenuPermissionVO> listByMenuId(Long menuId) {
//        return  menuPermissionMapper.listByMenuId(menuId);
//    }
//
//    /**
//     * 获取某个类型用户的所有权限列表（有缓存）
//     * @param sysType 系统类型
//     * @return 权限列表
//     */
//    @Cacheable(cacheNames = CacheNames.PERMISSIONS_KEY, key = "#sysType")
//    public List<String> listAllPermission(Integer sysType) {
//        return menuPermissionMapper.listAllPermissionBySysType(sysType);
//    }
//
//    /**
//     * 获取某个类型用户的所有权限列表（有缓存）
//     * @param sysType 系统类型
//     * @return 权限列表
//     */
//    @Cacheable(cacheNames = CacheNames.USER_PERMISSIONS_KEY, key = "#sysType + ':' + #userId")
//    public List<String> listPermissionByUserIdAndSysType(Long userId, Integer sysType) {
//        return menuPermissionMapper.listPermissionByUserIdAndSysType(userId, sysType);
//    }

}

