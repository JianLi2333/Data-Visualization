package io.dataease.auth.service.impl;

import io.dataease.auth.api.dto.CurrentRoleDto;
import io.dataease.auth.filter.entity.SysUserEntity;
import io.dataease.base.domain.SysUser;
import io.dataease.base.mapper.SysUserMapper;
import io.dataease.base.mapper.ext.AuthMapper;
import io.dataease.auth.service.AuthUserService;
import io.dataease.commons.constants.AuthConstants;
import io.dataease.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthUserServiceImpl implements AuthUserService {


    @Resource
    private AuthMapper authMapper;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private DynamicMenuServiceImpl dynamicMenuService;

    /**
     * 此处需被F2CRealm登录认证调用 也就是说每次请求都会被调用 所以最好加上缓存
     * @param userId
     * @return
     */

    //这个注解是用来标记这个方法需要被缓存的，value属性指定缓存的名字，key属性指定缓存的key，这里的key是SpEL表达式，#userId表示方法的参数userId
    @Cacheable(value = AuthConstants.USER_CACHE_NAME,  key = "'user' + #userId" )
    @Override
    public SysUserEntity getUserById(Long userId){
        return authMapper.findUser(userId);
    }

    @Override
    public SysUserEntity getUserByName(String username) {
        return authMapper.findUserByName(username);
    }

    @Override
    public List<String> roles(Long userId){
        return authMapper.roleCodes(userId);
    }

    /**
     * 此处需被F2CRealm登录认证调用 也就是说每次请求都会被调用 所以最好加上缓存
     * @param userId
     * @return
     */
    @Cacheable(value = AuthConstants.USER_PERMISSION_CACHE_NAME,  key = "'user' + #userId" )
    @Override
    public List<String> permissions(Long userId){
        try {
            // 用户登录获取菜单权限时同时更新插件菜单表
            dynamicMenuService.syncPluginMenu();
        }catch (Exception e){
            LogUtil.error(e);
            //ignore
        }
        List<String> permissions;
        SysUser sysUser = sysUserMapper.selectByPrimaryKey(userId);
        if(sysUser.getIsAdmin()!=null&&sysUser.getIsAdmin()){
            permissions = authMapper.permissionsAll();
        }else{
            permissions = authMapper.permissions(userId);
        }
        return Optional.ofNullable(permissions).orElse(new ArrayList<>()).stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
    }

    /**
     * 此处需被F2CRealm登录认证调用 也就是说每次请求都会被调用 所以最好加上缓存
     * @param userId
     * @return
     */
    @Cacheable(value = AuthConstants.USER_ROLE_CACHE_NAME,  key = "'user' + #userId" )
    @Override
    public List<CurrentRoleDto> roleInfos(Long userId) {
        return authMapper.roles(userId);
    }


    /**
     * 一波清除3个缓存
     * @param userId
     */
    @Caching(evict = {
            @CacheEvict(value = AuthConstants.USER_CACHE_NAME, key = "'user' + #userId"),
            @CacheEvict(value = AuthConstants.USER_ROLE_CACHE_NAME, key = "'user' + #userId"),
            @CacheEvict(value = AuthConstants.USER_PERMISSION_CACHE_NAME, key = "'user' + #userId")
    })
    @Override
    public void clearCache(Long userId) {
        LogUtil.info("正在清除用户缓存【{}】",userId);
    }

}
