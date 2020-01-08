package com.mmall.common;

import ch.qos.logback.classic.PatternLayout;
import com.mmall.exception.ParamException;
import com.mmall.exception.PermissionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 接口请求全局异常处理
 * Created by liyue
 * Time 2019/9/15 21:47
 */
@Slf4j
public class SpringExceptionResolver implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String url = request.getRequestURL().toString();
        ModelAndView mv;
        String defaultMsg = "System error";
        //.json  .page
        //项目中所有请求json数据，都是用.json结尾
        if (url.endsWith(".json")) {
            if (ex instanceof PermissionException || ex instanceof ParamException) {
                JsonData result = JsonData.fail(ex.getMessage());
                mv=new ModelAndView("jsonView",result.toMap());
            }else {
                log.error("unknow json exception,url:" + url, ex);
                JsonData result = JsonData.fail(defaultMsg);
                mv=new ModelAndView("jsonView",result.toMap());
            }
        }
        //项目中所有请求page页面，都是用.page结尾
        else if (url.endsWith(".page")){
            log.error("unknow page exception,url:" + url, ex);
            JsonData result = JsonData.fail(defaultMsg);
            mv=new ModelAndView("exception",result.toMap());
        }else {
            JsonData result = JsonData.fail(defaultMsg);
            mv=new ModelAndView("jsonView",result.toMap());
        }
        return mv;
    }
}
