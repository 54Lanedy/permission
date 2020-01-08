package com.mmall.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 前端传过来的查询参数
 */
@Getter
@Setter
@ToString
public class SearchLogParam {

    private Integer type; // LogType

    private String beforeSeg;

    private String afterSeg;

    private String operator;

    private String fromTime;//yyyy-MM-dd HH:mm:ss

    private String toTime;
}
