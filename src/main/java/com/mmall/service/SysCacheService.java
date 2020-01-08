package com.mmall.service;

import com.google.common.base.Joiner;
import com.mmall.beans.CacheKeyConstants;
import com.mmall.util.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.ShardedJedis;

import javax.annotation.Resource;

/**
 * 缓存服务
 * Created by liyue
 * Time 2019/12/23 20:58
 */
@Service
@Slf4j
public class SysCacheService {

    @Resource(name = "redisPool")
    private RedisPool redisPool;

    public void saveCache(String toSaveValue ,int timeOutSeconds, CacheKeyConstants prefix){
        saveCache(toSaveValue,timeOutSeconds,prefix,null);
    }

    /**
     * 保存到redis
     * @param toSaveValue  要保存的值
     * @param timeOutSeconds 超时时间，秒
     * @param prefix  前缀
     * @param keys
     */
    public void saveCache(String toSaveValue ,int timeOutSeconds, CacheKeyConstants prefix,String... keys){
        if (toSaveValue == null) {
            return;
        }

        ShardedJedis shardedJedis = null;

        try {
            //给传进来的key加上前缀
            String cacheKey = generateCacheKey(prefix, keys);
            //实例化连接池
            shardedJedis = redisPool.instance();
            //进行保存操作
            shardedJedis.setex(cacheKey,timeOutSeconds,toSaveValue);
        }catch (Exception e){
            log.error("save cache exception, prefix:{}, keys:{}", prefix.name(), JsonMapper.obj2String(keys), e);
        } finally {
            //关闭连接
            redisPool.safeClose(shardedJedis);
        }
    }

    /**
     * 从redis中取出value
     */
    public String getFromCache(CacheKeyConstants prefix,String... keys){
        ShardedJedis shardedJedis = null;
        String cacheKey = generateCacheKey(prefix,keys);
        try {
            //连接池实例化
            shardedJedis = redisPool.instance();
            //取值
            String value = shardedJedis.get(cacheKey);
            return value;
        }catch (Exception e){
            log.error("get from cache exception, prefix:{}, keys:{}", prefix.name(), JsonMapper.obj2String(keys), e);
            return null;
        }finally {
            redisPool.safeClose(shardedJedis);
        }
    }

    private String generateCacheKey(CacheKeyConstants prefix,String... keys){
        String key = prefix.name();
        if (keys!=null && keys.length>0) {
            key += "_" + Joiner.on("_").join(keys);
        }
        return key;
    }
}
