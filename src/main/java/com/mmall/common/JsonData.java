package com.mmall.common;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回的json
 * Created by liyue
 * Time 2019/9/15 21:39
 */
@Getter
@Setter
public class JsonData {

    private boolean ret;
    private Object data;
    private String msg;

    public JsonData(boolean ret) {
        this.ret = ret;
    }

    public static JsonData success(Object object, String msg) {
        JsonData jsonData = new JsonData(true);
        jsonData.data = object;
        jsonData.msg = msg;
        return jsonData;
    }

    public static JsonData success(Object object) {
        JsonData jsonData = new JsonData(true);
        jsonData.data = object;
        return jsonData;
    }

    public static JsonData success(String msg) {
        JsonData jsonData = new JsonData(true);
        jsonData.msg = msg;
        return jsonData;
    }

    public static JsonData success() {
        return new JsonData(true);
    }


    public static JsonData fail(String msg) {
        JsonData jsonData = new JsonData(false);
        jsonData.msg = msg;
        return jsonData;
    }

    public Map<String,Object> toMap(){
        HashMap<String,Object> map = new HashMap();
        map.put("ret",ret);
        map.put("data",data);
        map.put("msg",msg);
        return map;
    }
}
