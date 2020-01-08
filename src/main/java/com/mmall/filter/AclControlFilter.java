package com.mmall.filter;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.mmall.common.ApplicationContextHelper;
import com.mmall.common.JsonData;
import com.mmall.common.RequestHolder;
import com.mmall.model.SysUser;
import com.mmall.service.SysCoreService;
import com.mmall.util.JsonMapper;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 权限过滤器
 * Created by liyue
 * Time 2019/12/22 19:17
 */
@Slf4j
public class AclControlFilter implements Filter {

    private static Set<String> exclusionUrlSet = Sets.newConcurrentHashSet();

    public static final String noAuthUrl = "/sys/user/noAuth.page";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String exclusionUrls = filterConfig.getInitParameter("exclusionUrls");
        List<String> exclusionUrlList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(exclusionUrls);
        exclusionUrlSet = Sets.newConcurrentHashSet(exclusionUrlList);

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        //获取要访问的url
        String servletPath = request.getServletPath();
        //获取请求中的参数map
        Map requestMap = request.getParameterMap();
        //如果是白名单，不进行拦截，直接走过滤链即可
        if (exclusionUrlSet.contains(servletPath)) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }
        //获取当前用户是否登陆
        SysUser sysUser = RequestHolder.getCurrentUser();

        //过滤没有登陆的用户，进行无权限跳转
        if (sysUser == null) {
            log.info("someone visit {}, but no login, parameter:{}", servletPath, JsonMapper.obj2String(requestMap));
            noAuth(request, response);
            return;
        }
        //校验用户是否拥有当前访问的权限点
        SysCoreService sysCoreService = ApplicationContextHelper.popBean(SysCoreService.class);
        if (!sysCoreService.hasUrlAcl(servletPath)) {
            log.info("{} visit {}, but no login, parameter:{}", JsonMapper.obj2String(sysUser), servletPath, JsonMapper.obj2String(requestMap));
            noAuth(request, response);
            return;
        }

        chain.doFilter(servletRequest,servletResponse);
        return;
    }

    /**
     * 处理.json请求或者.page请求的无权限跳转
     */
    private void noAuth(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String servletPath = request.getServletPath();
        if (servletPath.endsWith(".json")) {
            JsonData jsonData = JsonData.fail("没有访问权限，如需要访问，请联系管理员");
            response.setHeader("Content-Type","application/json");
            response.getWriter().print(JsonMapper.obj2String(jsonData));
            return;
        }else {
            clientRedirect(noAuthUrl,response);
            return;
        }
    }

    private void clientRedirect(String url, HttpServletResponse response) throws IOException {
        response.setHeader("Content-Type","text/html");
        response.getWriter().print("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" + "<head>\n" + "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"/>\n"
                + "<title>跳转中...</title>\n" + "</head>\n" + "<body>\n" + "跳转中，请稍候...\n" + "<script type=\"text/javascript\">//<![CDATA[\n"
                + "window.location.href='" + url + "?ret='+encodeURIComponent(window.location.href);\n" + "//]]></script>\n" + "</body>\n" + "</html>\n");
    }

    @Override
    public void destroy() {

    }
}
