package com.mmall.service;

import com.google.common.base.Preconditions;
import com.mmall.beans.PageQuery;
import com.mmall.beans.PageResult;
import com.mmall.common.RequestHolder;
import com.mmall.dao.SysUserMapper;
import com.mmall.exception.ParamException;
import com.mmall.model.SysUser;
import com.mmall.param.UserParam;
import com.mmall.util.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 用户管理业务层
 * Created by liyue
 * Time 2019/9/22 13:45
 */
@Service
public class SysUserService {

    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysLogService sysLogService;

    public void save(UserParam param){
        BeanValidator.check(param);
        if (checkTelephoneExist(param.getTelephone(), param.getId())) {
            throw new ParamException("电话已被占用");
        }
        if (checkEmailExist(param.getMail(), param.getId())) {
            throw new ParamException("邮箱已被占用");
        }
        String password = PasswordUtil.randomPassword();
        //TODO 此值只是为了方便测试登陆
        password = "123456";
        String encryptedPassword = MD5Util.encrypt(password);
        SysUser user = SysUser.builder().username(param.getUsername()).telephone(param.getTelephone()).mail(param.getMail())
                .password(encryptedPassword).deptId(param.getDeptId()).status(param.getStatus()).remark(param.getRemark()).build();
        user.setOperator(RequestHolder.getCurrentUser().getUsername());//TODO
        user.setOperatorIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));//TODO
        user.setOperatorTime(new Date());
        //TODO : sendEmail
//        MailUtil.send();

        sysUserMapper.insertSelective(user);
        sysLogService.saveUserLog(null, user);
    }

    public void update(UserParam param) {
        BeanValidator.check(param);
        if (checkTelephoneExist(param.getTelephone(), param.getId())) {
            throw new ParamException("电话已被占用");
        }
        if (checkEmailExist(param.getMail(), param.getId())) {
            throw new ParamException("邮箱已被占用");
        }
        //取出待更新的用户
        SysUser before = sysUserMapper.selectByPrimaryKey(param.getId());
        //空校验
        Preconditions.checkNotNull(before,"待更新的用户不存在");
        SysUser after = SysUser.builder().id(param.getId()).username(param.getUsername()).telephone(param.getTelephone()).mail(param.getMail())
                .deptId(param.getDeptId()).status(param.getStatus()).remark(param.getRemark()).build();
        after.setOperator(RequestHolder.getCurrentUser().getUsername());//TODO
        after.setOperatorIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));//TODO
        after.setOperatorTime(new Date());
        sysUserMapper.updateByPrimaryKeySelective(after);
        sysLogService.saveUserLog(before, after);

    }

    public boolean checkEmailExist(String mail, Integer userId){
        return sysUserMapper.countByMail(mail,userId)>0;
    }
    public boolean checkTelephoneExist(String telephone, Integer userId){
        return sysUserMapper.countByTelephone(telephone,userId)>0;
    }

    public SysUser findByKeyword(String username) {
        return sysUserMapper.findByKeyword(username);
    }

    public PageResult<SysUser> getPageByDeptId(int deptId, PageQuery page) {
        BeanValidator.check(page);
        int count = sysUserMapper.countByDeptId(deptId);
        if (count>0) {
            List<SysUser> list  =  sysUserMapper.getPageByDeptId(deptId,page);
            return PageResult.<SysUser>builder().total(count).data(list).build();
        }
        return PageResult.<SysUser>builder().build();
    }

    public List<SysUser> getAll() {
        return sysUserMapper.getAll();
    }
}
