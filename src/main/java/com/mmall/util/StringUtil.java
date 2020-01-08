package com.mmall.util;

import com.google.common.base.Splitter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 将以逗号分隔的字符串转换成list
 * Created by liyue
 * Time 2019/12/21 11:37
 */
public class StringUtil {

    public static List<Integer> splitToListInt(String str){
        List<String> strList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(str);
        return strList.stream().map(strItem -> Integer.parseInt(strItem)).collect(Collectors.toList());
    }
}
