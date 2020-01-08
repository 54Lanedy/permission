package com.mmall.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.beans.LogType;
import com.mmall.common.RequestHolder;
import com.mmall.dao.SysLogMapper;
import com.mmall.dao.SysRoleAclMapper;
import com.mmall.model.SysLogWithBLOBs;
import com.mmall.model.SysRoleAcl;
import com.mmall.util.IpUtil;
import com.mmall.util.JsonMapper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 角色权限管理service
 * Created by liyue
 * Time 2019/12/21 11:27
 */
@Service
public class SysRoleAclService {

    @Resource
    private SysRoleAclMapper sysRoleAclMapper;
    @Resource
    private SysLogMapper sysLogMapper;

    public void changeRoleAcls(Integer roleId, List<Integer> aclIdList){
        //先获取当前角色的所有旧的权限
        List<Integer> originAclIdList = sysRoleAclMapper.getAclIdListByRoleIdList(Lists.newArrayList(roleId));
        //检查修改前后的权限是否相同，长度相等才有可能相同
        if (originAclIdList.size() == aclIdList.size()) {
            //长度相等后去重
            Set<Integer> originAclIdSet = Sets.newHashSet(originAclIdList);
            Set<Integer> aclIdSet = Sets.newHashSet(aclIdList);
            //两个set相减，检查是否为null,若为null则视为没有任何修改
            originAclIdSet.removeAll(aclIdSet);
            if (CollectionUtils.isEmpty(originAclIdSet)) {
                return;
            }
        }
        //执行更新
        updateRoleAcls(roleId,aclIdList);
        saveRoleAclLog(roleId, originAclIdList, aclIdList);
    }

    @Transactional
    public void updateRoleAcls(Integer roleId, List<Integer> aclIdList) {
        //检查结果不相同，先删除旧的再新增
        sysRoleAclMapper.deleteByRoleId(roleId);
        if (CollectionUtils.isEmpty(aclIdList)) {
            return;
        }
        //遍历新的绑定list
        List<SysRoleAcl> roleAclList = Lists.newArrayList();
        for (Integer aclId : aclIdList) {
            SysRoleAcl roleAcl = SysRoleAcl.builder().roleId(roleId).aclId(aclId)
                    .operator(RequestHolder.getCurrentUser().getUsername())
                    .operatorIp(IpUtil.getUserIP(RequestHolder.getCurrentRequest()))
                    .operatorTime(new Date()).build();
            roleAclList.add(roleAcl);
        }
        //执行批量插入
        sysRoleAclMapper.batchInsert(roleAclList);
    }

    private void saveRoleAclLog(int roleId, List<Integer> before, List<Integer> after) {
        SysLogWithBLOBs sysLog = new SysLogWithBLOBs();
        sysLog.setType(LogType.TYPE_ROLE_ACL);
        sysLog.setTargetId(roleId);
        sysLog.setOldValue(before == null ? "" : JsonMapper.obj2String(before));
        sysLog.setNewValue(after == null ? "" : JsonMapper.obj2String(after));
        sysLog.setOperator(RequestHolder.getCurrentUser().getUsername());
        sysLog.setOperatorIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        sysLog.setOperatorTime(new Date());
        sysLog.setStatus(1);
        sysLogMapper.insertSelective(sysLog);
    }
}
