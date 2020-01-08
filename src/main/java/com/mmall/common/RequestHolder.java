package com.mmall.common;

import com.mmall.model.SysUser;

import javax.servlet.http.HttpServletRequest;

/**
 * 使用ThreadLocal来将当前的用户信息存入一个session，需要的时候，直接从全局的ThreadLocal中取出来。
 * 请求处理，多进程编程（ThreadLocal），从当前进程拿到进程中的对象，
 * 从而从多个进程中分离数据，避免冲突，高并发时各自
 * 处理各自的数据，互相间不会有影响
 * RequestHolder->HttpInterceptor->LoginFilter->web.xml
 * Created by liyue
 * Time 2019/9/22 12:37
 */
public class RequestHolder {

    public static final ThreadLocal<SysUser> userHolder = new ThreadLocal<>();

    public static final ThreadLocal<HttpServletRequest> requestHolder  = new ThreadLocal<>();

    /** 新增 **/
    public static void add(SysUser sysUser){
        userHolder.set(sysUser);
    }

    public static void add(HttpServletRequest request){
        requestHolder.set(request);
    }

    /** 获取 **/
    public static SysUser getCurrentUser() {
        return userHolder.get();
    }

    public static HttpServletRequest getCurrentRequest() {
        return requestHolder.get();
    }

    /** 移除，释放内存 **/
    public static void remove(){
        userHolder.remove();
        requestHolder.remove();
    }
}
