package com.mmall.beans;

import lombok.Getter;

/**
 * redis的key前缀
 * Created by liyue
 * Time 2019/12/23 20:59
 */
@Getter
public enum CacheKeyConstants {

    SYSTEM_ACLS,

    USER_ACLS;
}
