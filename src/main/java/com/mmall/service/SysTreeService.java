package com.mmall.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mmall.dao.SysAclMapper;
import com.mmall.dao.SysAclModuleMapper;
import com.mmall.dao.SysDeptMapper;
import com.mmall.dto.AclDto;
import com.mmall.dto.AclModuleLevelDto;
import com.mmall.dto.DeptLevelDto;
import com.mmall.model.SysAcl;
import com.mmall.model.SysAclModule;
import com.mmall.model.SysDept;
import com.mmall.util.LevelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by liyue
 * Time 2019/9/21 11:03
 */
@Service
public class SysTreeService {

    @Resource
    private SysDeptMapper sysDeptMapper;

    @Resource
    private SysAclModuleMapper sysAclModuleMapper;

    @Resource
    private SysCoreService sysCoreService;

    @Resource
    private SysAclMapper sysAclMapper;

    /**
     * 获取用户拥有的权限树
     */
    public List<AclModuleLevelDto> userAclTree(int userId) {
        // 1、当前用户已分配的权限点
        List<SysAcl> userAclList = sysCoreService.getUserAclList(userId);
        List<AclDto> aclDtoList = Lists.newArrayList();
        //处理权限点：设置当前用户拥有的权限点和当前角色拥有的权限点
        for (SysAcl acl : userAclList) {
            AclDto dto = AclDto.adapt(acl);
            dto.setHasAcl(true);
            dto.setChecked(true);
            aclDtoList.add(dto);
        }
        //转换成权限树
        return aclListToTree(aclDtoList);
    }

    /**
     * 获取角色下[权限模块和权限点]的树
     */
    public List<AclModuleLevelDto> roleTree(int roleId) {
        // 1、当前用户已分配的权限点
        List<SysAcl> userAclList = sysCoreService.getCurrentUserAclList();
        // 2、当前角色分配的权限点
        List<SysAcl> roleAclList = sysCoreService.getRoleAclList(roleId);
        // 3、当前系统所有权限点（已处理）
        List<AclDto> aclDtoList = Lists.newArrayList();

        //将用户已有权限点和角色可操作权限点的id抽取出来(去重)
        Set<Integer> userAclIdSet = userAclList.stream().map(sysAcl -> sysAcl.getId()).collect(Collectors.toSet());
        Set<Integer> roleAclIdSet = roleAclList.stream().map(sysAcl -> sysAcl.getId()).collect(Collectors.toSet());

        //获取系统全部权限点
        List<SysAcl> allAclList = sysAclMapper.getAll();
        //处理权限点：设置当前用户拥有的权限点和当前角色拥有的权限点
        for (SysAcl acl : allAclList) {
            AclDto dto = AclDto.adapt(acl);
            if (userAclIdSet.contains(acl.getId())){
                dto.setHasAcl(true);
            }
            if (roleAclIdSet.contains(acl.getId())){
                dto.setChecked(true);
            }
            aclDtoList.add(dto);
        }
        //转换成权限树
        return aclListToTree(aclDtoList);
    }

    public List<AclModuleLevelDto> aclListToTree(List<AclDto> aclDtoList){
        if (CollectionUtils.isEmpty(aclDtoList)) {
            return Lists.newArrayList();
        }
        //权限模块
        List<AclModuleLevelDto> aclModuleLevelList = aclModuleTree();
        //Multimap<Integer, AclDto>将权限模块id作为key，相同权限模块id的权限点归成一个集合作为value存储，数据格式：aclModuleId -> [acl1, acl2, ...]
        Multimap<Integer, AclDto> moduleIdAclMap = ArrayListMultimap.create();
        for (AclDto acl : aclDtoList) {
            //只处理有效的权限点
            if (acl.getStatus() == 1) {
                moduleIdAclMap.put(acl.getAclModuleId(), acl);
            }
        }
        //权限点排序并绑定到权限模块下
        bindAclsWithOrder(aclModuleLevelList, moduleIdAclMap);
        return aclModuleLevelList;
    }

    public void bindAclsWithOrder(List<AclModuleLevelDto> aclModuleLevelList,Multimap<Integer, AclDto> moduleIdAclMap){
        if (CollectionUtils.isEmpty(aclModuleLevelList)) {
            return;
        }
        for (AclModuleLevelDto dto : aclModuleLevelList) {
            List<AclDto> aclDtoList = (List<AclDto>) moduleIdAclMap.get(dto.getId());
            if (CollectionUtils.isNotEmpty(aclDtoList)) {
                //对每个权限模块下的权限点进行排序
                Collections.sort(aclDtoList, aclSeqComparator);
                dto.setAclList(aclDtoList);
            }
            //递归处理
            bindAclsWithOrder(dto.getAclModuleList(), moduleIdAclMap);
        }
    }

    /**
     * 准备权限模块数据
     */
    public List<AclModuleLevelDto> aclModuleTree(){
        List<SysAclModule> aclModuleList = sysAclModuleMapper.getAllAclModule();
        List<AclModuleLevelDto> dtoList = Lists.newArrayList();
        for (SysAclModule aclModule : aclModuleList) {
            dtoList.add(AclModuleLevelDto.adept(aclModule));
        }
        return aclModuleListToTree(dtoList);
    }

    /**
     * 开始转换为树形结构数据
     */
    private List<AclModuleLevelDto> aclModuleListToTree(List<AclModuleLevelDto> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return Lists.newArrayList();
        }
        // Multimap：可以很好地表示每个层级下的所有权限模块
        // level -> [aclmodule1, aclmodule2, ...] Map<String, List<Object>>
        Multimap<String,AclModuleLevelDto> levelAclModuleMap = ArrayListMultimap.create();
        List<AclModuleLevelDto> rootList = Lists.newArrayList();

        for (AclModuleLevelDto dto : dtoList) {
            levelAclModuleMap.put(dto.getLevel(),dto);
            if (LevelUtil.ROOT.equals(dto.getLevel())) {
                rootList.add(dto);
            }
        }
        //排序
        Collections.sort(rootList,aclModuleSeqComparator);
        //递归生成权限模块树
        transformAclModuleTree(rootList, LevelUtil.ROOT, levelAclModuleMap);
        return rootList;
    }

    /**
     * 递归生成权限模块树形结构数据
     */
    private void transformAclModuleTree(List<AclModuleLevelDto> dtoList, String level, Multimap<String, AclModuleLevelDto> levelAclModuleMap) {
        for (int i = 0; i < dtoList.size(); i++) {
            AclModuleLevelDto dto = dtoList.get(i);
            //获取下一个层级level
            String nextLevel = LevelUtil.calculateLevel(level, dto.getId());
            //取出下一个层级的所有权限模块
            List<AclModuleLevelDto> tempList = (List<AclModuleLevelDto>) levelAclModuleMap.get(nextLevel);
            //如果下一层级不为空，进行递归
            if (CollectionUtils.isNotEmpty(dtoList)) {
                //对当前层级进行排序
                Collections.sort(tempList,aclModuleSeqComparator);
                //放入dto中
                dto.setAclModuleList(tempList);
                //递归
                transformAclModuleTree(tempList,nextLevel,levelAclModuleMap);
            }

        }

    }

    /**
     * 准备部门数据
     * @return
     */
    public List<DeptLevelDto> deptTree(){
        List<SysDept> deptList = sysDeptMapper.getAllDept();

        List<DeptLevelDto> dtoList = Lists.newArrayList();
        for (SysDept dept : deptList) {
            DeptLevelDto dto = DeptLevelDto.adept(dept);
            dtoList.add(dto);
        }
        return deptListToTree(dtoList);
    }

    /**
     * 组装dept树形结构数据
     * @return
     */
    public List<DeptLevelDto> deptListToTree(List<DeptLevelDto> deptLevelList){
        if (CollectionUtils.isEmpty(deptLevelList)) {
            return Lists.newArrayList();
        }
        // level-->[dept1,dept2,...]
        Multimap<String, DeptLevelDto> levelMultimap = ArrayListMultimap.create();
        List<DeptLevelDto> rootList = Lists.newArrayList();

        for (DeptLevelDto dto : deptLevelList) {
            levelMultimap.put(dto.getLevel(),dto);
            if (LevelUtil.ROOT.equals(dto.getLevel())) {
                rootList.add(dto);
            }
        }
        //按照seq从小到大排序
        Collections.sort(rootList, new Comparator<DeptLevelDto>() {
            @Override
            public int compare(DeptLevelDto o1, DeptLevelDto o2) {
                return o1.getSeq() - o2.getSeq();
            }
        });
        //递归生成树
        transformDeptTree(rootList, LevelUtil.ROOT, levelMultimap);
        return rootList;
    }

    /**
     * 进行递归排序
     * root level:0, 0, all 0-->0.1,0.2
     *    1 level:0.1
     *    2 level:0.2
     */
    public void transformDeptTree(List<DeptLevelDto> deptLevelList, String level, Multimap<String, DeptLevelDto> levelMultimap){
        for (int i = 0; i < deptLevelList.size(); i++) {
            //遍历该层的每个元素
            DeptLevelDto deptLevelDto = deptLevelList.get(i);
            //处理当前层级的数据
            String nextLevel = LevelUtil.calculateLevel(level, deptLevelDto.getId());
            //处理下一层
            List<DeptLevelDto> tempDeptList = (List<DeptLevelDto>) levelMultimap.get(nextLevel);
            if (CollectionUtils.isNotEmpty(tempDeptList)) {
                //排序
                Collections.sort(tempDeptList, deptSeqComparator);
                //设置下一层部门
                deptLevelDto.setDeptList(tempDeptList);
                //进入下一层处理
                transformDeptTree(tempDeptList, nextLevel, levelMultimap);
            }
        }
    }

    public Comparator<DeptLevelDto> deptSeqComparator = new Comparator<DeptLevelDto>() {
        @Override
        public int compare(DeptLevelDto o1, DeptLevelDto o2) {
            return o1.getSeq() - o2.getSeq();
        }
    };

    public Comparator<AclModuleLevelDto> aclModuleSeqComparator = new Comparator<AclModuleLevelDto>() {
        public int compare(AclModuleLevelDto o1, AclModuleLevelDto o2) {
            return o1.getSeq() - o2.getSeq();
        }
    };
    public Comparator<AclDto> aclSeqComparator = new Comparator<AclDto>() {
        public int compare(AclDto o1, AclDto o2) {
            return o1.getSeq() - o2.getSeq();
        }
    };

}
