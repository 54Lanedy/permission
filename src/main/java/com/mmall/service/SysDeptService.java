package com.mmall.service;

import com.google.common.base.Preconditions;
import com.mmall.common.RequestHolder;
import com.mmall.dao.SysDeptMapper;
import com.mmall.dao.SysUserMapper;
import com.mmall.exception.ParamException;
import com.mmall.model.SysDept;
import com.mmall.param.DeptParam;
import com.mmall.util.BeanValidator;
import com.mmall.util.IpUtil;
import com.mmall.util.LevelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by liyue
 * Time 2019/9/20 21:48
 */
@Service
public class SysDeptService {

    @Resource
    private SysDeptMapper sysDeptMapper;

    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysLogService sysLogService;

    public void save(DeptParam param){
        BeanValidator.check(param);
        if (checkExist(param.getParentId(),param.getName(),param.getId())) {
            throw new ParamException("同一层级下存在相同名称的部门");
        }
        SysDept dept = SysDept.builder().name(param.getName()).parentId(param.getParentId())
                .seq(param.getSeq()).remark(param.getRemark()).build();
        dept.setLevel(LevelUtil.calculateLevel(getLevel(param.getParentId()),param.getParentId()));
        //bug：ThreadLocal获取不到user。解决：web.xml中未配置登陆拦截器loginFilter
        dept.setOperator(RequestHolder.getCurrentUser().getUsername());//TODO
        dept.setOperatorIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));//TODO
        dept.setOperatorTime(new Date());
        sysDeptMapper.insertSelective(dept);
        sysLogService.saveDeptLog(null,dept);
    }

    public void update(DeptParam param){
        BeanValidator.check(param);
        if (checkExist(param.getParentId(),param.getName(),param.getId())) {
            throw new ParamException("同一层级下存在相同名称的部门");
        }
        SysDept before = sysDeptMapper.selectByPrimaryKey(param.getId());
        Preconditions.checkNotNull(before,"待更新的部门不存在");
        if (checkExist(param.getParentId(),param.getName(),param.getId())) {
            throw new ParamException("同一层级下存在相同名称的部门");
        }
        SysDept after = SysDept.builder().id(param.getId()).name(param.getName()).parentId(param.getParentId())
                .seq(param.getSeq()).remark(param.getRemark()).build();
        after.setLevel(LevelUtil.calculateLevel(getLevel(param.getParentId()),param.getParentId()));
        after.setOperator(RequestHolder.getCurrentUser().getUsername());//TODO
        after.setOperatorIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));//TODO
        after.setOperatorTime(new Date());
        updateWithChild(before,after);
        sysLogService.saveDeptLog(before,after);
    }

    /**
     * 级联更新部门
     */
    @Transactional
    public void updateWithChild(SysDept before,SysDept after){
        String newLevelPrefix = after.getLevel();
        String oldLevelPrefix = before.getLevel();
        //不相同则更新
        if (!after.getLevel().equals(before.getLevel())) {
            //所有需要更新的子部门列表
            List<SysDept> deptList = sysDeptMapper.getChildDeptListByLevel(before.getLevel());
            if (CollectionUtils.isNotEmpty(deptList)) {
                for (SysDept dept : deptList) {
                    String level = dept.getLevel();
                    if (level.indexOf(oldLevelPrefix) == 0) {
                        //拼接新的level
                        level = newLevelPrefix + level.substring(oldLevelPrefix.length());
                        dept.setLevel(level);
                    }
                }
                sysDeptMapper.batchUpdateLevel(deptList);
            }

        }
        sysDeptMapper.updateByPrimaryKey(after);

    }

    /**
     * 检查部门是否重复
     * @param parentId
     * @param deptName
     * @param deptId
     * @return
     */
    private boolean checkExist(Integer parentId,String deptName,Integer deptId){
        return sysDeptMapper.countByNameAndParentId(parentId, deptName, deptId) > 0;
    }

    /**
     * 获取部门等级
     */
    public String getLevel(Integer deptId){
        SysDept dept = sysDeptMapper.selectByPrimaryKey(deptId);
        if (dept == null) {
            return null;
        }
        return dept.getLevel();
    }

    /**
     * 删除部门
     * @param deptId
     */
    public void delete(int deptId) {
        //检查要删除的部门是否存在
        SysDept dept = sysDeptMapper.selectByPrimaryKey(deptId);
        Preconditions.checkNotNull(dept,"待删除的部门不存在，无法删除");
        //检查是否有子部门
        if (sysDeptMapper.countByParentId(dept.getId()) > 0) {
            throw new ParamException("当前部门下面有子部门，无法删除");
        }
        //检查此部门下是否有用户
        if (sysUserMapper.countByDeptId(dept.getId()) > 0){
            throw new ParamException("当前部门下面有用户，无法删除");
        }
        //通过检查，执行删除
        sysDeptMapper.deleteByPrimaryKey(deptId);
    }
}
