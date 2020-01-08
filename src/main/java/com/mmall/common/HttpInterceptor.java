package com.mmall.common;

import com.mmall.util.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 自定义请求拦截器,监听http请求。
 * 拦截器用于移除对应的ThreadLocal对象，防止内存泄漏。
 * RequestHolder->HttpInterceptor->LoginFilter->web.xml
 * Created by liyue
 * Time 2019/9/19 21:57
 */
@Slf4j
public class HttpInterceptor extends HandlerInterceptorAdapter {

    public static final String START_TIME = "requestStartTime";
    /**
     * 请求之前处理的方法
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String url = request.getRequestURI();
        Map parameterMap = request.getParameterMap();
        long start = System.currentTimeMillis();
        log.info("request start. url:{}, params:{}",url,JsonMapper.obj2String(parameterMap));
        request.setAttribute(START_TIME,start);
        return true;
    }

    /**
     * 请求正常结束后的处理方法
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        String url = request.getRequestURI();
//        Long start = (Long) request.getAttribute(START_TIME);
//        long end = System.currentTimeMillis();
//        log.info("request finished. request start. url:{}, cost:{}",url,end-start);
//        RequestHolder.remove();
    }

    /**
     * 请求结束之后响应的方法，任何情况下都会调用
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String url = request.getRequestURI();
        Long start = (Long) request.getAttribute(START_TIME);
        long end = System.currentTimeMillis();
        log.info("request complete. request start. url:{}, cost:{}",url,end-start);
        RequestHolder.remove();
    }
}
