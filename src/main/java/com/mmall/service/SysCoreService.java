package com.mmall.service;

import com.fasterxml.classmate.types.ResolvedRecursiveType;
import com.google.common.collect.Lists;
import com.mmall.beans.CacheKeyConstants;
import com.mmall.common.RequestHolder;
import com.mmall.dao.SysAclMapper;
import com.mmall.dao.SysRoleAclMapper;
import com.mmall.dao.SysRoleUserMapper;
import com.mmall.model.SysAcl;
import com.mmall.model.SysUser;
import com.mmall.util.JsonMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by liyue
 * Time 2019/12/15 13:25
 */
@Service
public class SysCoreService {


    @Resource
    private SysAclMapper sysAclMapper;
    @Resource
    private SysRoleUserMapper sysRoleUserMapper;
    @Resource
    private SysRoleAclMapper sysRoleAclMapper;
    @Resource
    private SysCacheService sysCacheService;

    /**
     * 获取当前用户的权限点
     * @return
     */
    public List<SysAcl> getCurrentUserAclList(){
        Integer userId = RequestHolder.getCurrentUser().getId();
        return getUserAclList(userId);
    }

    public List<SysAcl> getRoleAclList(int roleId){
        List<Integer> aclIdList = sysRoleAclMapper.getAclIdListByRoleIdList(Lists.<Integer>newArrayList(roleId));
        if (CollectionUtils.isEmpty(aclIdList)) {
            return Lists.newArrayList();
        }
        return sysAclMapper.getByIdList(aclIdList);
    }

    public List<SysAcl> getUserAclList(int userId) {
        if (isSuperAdmin()) {
            return sysAclMapper.getAll();
        }
        //根据用户id获取该用户角色id集合
        List<Integer> userRoleIdList = sysRoleUserMapper.getRoleIdListByUserId(userId);
        if (CollectionUtils.isEmpty(userRoleIdList)) {
            return Lists.newArrayList();
        }
        //通过角色id集合获取权限点id集合
        List<Integer> userAclIdList = sysRoleAclMapper.getAclIdListByRoleIdList(userRoleIdList);
        if (CollectionUtils.isEmpty(userAclIdList)) {
            return Lists.newArrayList();
        }
        //最终获取用户的权限点
        return sysAclMapper.getByIdList(userAclIdList);
    }

    private boolean isSuperAdmin() {
        // 这里是我自己定义了一个假的超级管理员规则，实际中要根据项目进行修改
        // 可以是配置文件获取，可以指定某个用户，也可以指定某个角色
        SysUser sysUser = RequestHolder.getCurrentUser();
        if (sysUser.getMail().contains("admin")) {
            return true;
        }
        return false;
    }

    /**
     * 检查用户有无权限访问当前点击的链接url
     * @param url
     * @return
     */
    public boolean hasUrlAcl(String url) {
        if (isSuperAdmin()) {
            return true;
        }
        //获取权限点，使用list接收是因为一个url可能会被配置成多个权限点（不同的人使用管理员操作添加权限点时引起）
        List<SysAcl> aclList = sysAclMapper.getByUrl(url);
        if (CollectionUtils.isEmpty(aclList)) {
            //权限管理里无此url的权限点，说明该url的权限系统不控制，此时返回true即可
            return true;
        }

        //当前用户拥有的权限点和权限点idSet
        List<SysAcl> userAclList = getCurrentUserAclListFromCache();
        Set<Integer> userAclIdSet = userAclList.stream().map(acl -> acl.getId()).collect(Collectors.toSet());

        boolean hasValidAcl = false;
        //规则：只要有一个权限点有权限，那么我们就认为有权限访问(一个url可能会被配置成多个权限点)
        for (SysAcl acl : aclList) {
            //如果权限点不存在或者无效，继续执行其他权限点的校验
            if (acl ==null || acl.getStatus()!=1){
                continue;
            }
            hasValidAcl = true;
            //如果用户拥有的权限点中与要访问的url的权限点相同，则判定用户肯定拥有权限访问
            if (userAclIdSet.contains(acl.getId())){
                return true;
            }
        }
        //如果没有一个权限点是需要校验的（所有需要校验的权限点都无效），说明该url的权限系统不控制
        if (!hasValidAcl) {
            return true;
        }
        return false;
    }

    /**
     * 缓存用户的权限点list到redis
     * @return
     */
    public List<SysAcl> getCurrentUserAclListFromCache(){
        Integer userId = RequestHolder.getCurrentUser().getId();
        //从redis中获取已缓存的权限点
        String cacheValue = sysCacheService.getFromCache(CacheKeyConstants.USER_ACLS, String.valueOf(userId));
        //如果redis中的权限点为null，从数据库中获取并缓存到redis中
        if (StringUtils.isBlank(cacheValue)) {
            List<SysAcl> aclList = getCurrentUserAclList();
            if (CollectionUtils.isNotEmpty(aclList)) {
                sysCacheService.saveCache(JsonMapper.obj2String(aclList),600,CacheKeyConstants.USER_ACLS,String.valueOf(userId));
            }
            return aclList;
        }
        //如果redis中已缓存有当前用户的权限点，则直接返回
        return JsonMapper.string2Obj(cacheValue, new TypeReference<List<SysAcl>>() {
        });
    }
}
