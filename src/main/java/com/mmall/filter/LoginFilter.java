package com.mmall.filter;

import com.mmall.common.RequestHolder;
import com.mmall.model.SysUser;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登陆拦截器
 * 过滤器LoginFilter，将每次的请求，和每次的用户信息存入ThreadLocal中
 * RequestHolder->HttpInterceptor->LoginFilter->web.xml
 * Created by liyue
 * Time 2019/9/22 12:53
 */
@Slf4j
public class LoginFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resq = (HttpServletResponse) response;

        SysUser sysUser = (SysUser) req.getSession().getAttribute("user");
        if (sysUser == null) {
            String path = "/signin.jsp";
            resq.sendRedirect(path);
            return;
        }
        RequestHolder.add(sysUser);
        RequestHolder.add(req);
        filterChain.doFilter(request,response);
        return;

    }

    @Override
    public void destroy() {

    }
}
