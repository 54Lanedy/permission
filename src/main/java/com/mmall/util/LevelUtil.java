package com.mmall.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by liyue
 * Time 2019/9/20 21:54
 */
public class LevelUtil {
    
    public static final String SEPARATOR = ".";

    public static final String ROOT = "0";

    //部门级别生成方法
    //0
    //0.1
    //0.1.2
    //0.1.3
    //0.4
    public static String calculateLevel(String parentLevel,int parentId){
        if (StringUtils.isBlank(parentLevel)) {
            return ROOT;
        } else {
            //StringUtils.join 拼接字符串
            return StringUtils.join(parentLevel,SEPARATOR,parentId);
        }
    }
}
