package com.mmall.util;

import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;
import org.codehaus.jackson.type.TypeReference;

/**
 * json字符串和object相互转换的工具类
 * Created by liyue
 * Time 2019/9/19 21:18
 */
@Slf4j
public class JsonMapper {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        //config
        objectMapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS,false);
        objectMapper.setFilters(new SimpleFilterProvider().setFailOnUnknownId(false));
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_EMPTY);
    }

    /**
     * 对象转字符串
     * @param src  待转换对象
     * @param <T>
     * @return
     */
    public static <T> String obj2String (T src){
        if (src == null) {
            return null;
        }
        try {
            return src instanceof String ? (String) src : objectMapper.writeValueAsString(src);
        }catch (Exception e){
            log.warn("parse object to String exception, error:{}",e);
            return null;
        }
    }

    /**
     * 字符串转对象
     * @param src
     * @param typeReference
     * @param <T>
     * @return
     */
    public static <T> T string2Obj(String src, TypeReference<T> typeReference){
        if (src == null || typeReference == null) {
            return null;
        }
        try {
            return (T) (typeReference.getType().equals(String.class) ? src : objectMapper.readValue(src,typeReference));
        }catch (Exception e){
            log.warn("parse String to Object exception, String:{}, TypeReference<T>:{}, error:{}",src,typeReference,e);
            return null;
        }

    }
}
