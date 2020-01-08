package com.mmall.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import javax.annotation.Resource;

/**
 * redis连接池实例化
 * Created by liyue
 * Time 2019/12/23 20:53
 */
@Service("redisPool")
@Slf4j
public class RedisPool {

    @Resource(name = "shardedJedisPool")
    private ShardedJedisPool shardedJedisPool;

    /**
     * 实例化redis连接池
     * @return
     */
    public ShardedJedis instance() {
        return shardedJedisPool.getResource();
    }

    /**
     * 安全关闭连接池,每次使用完后必须关闭，否则会占用资源导致新的连接超时
     * @param shardedJedis
     */
    public void safeClose(ShardedJedis shardedJedis){
        try {
            if (shardedJedis != null) {
                shardedJedis.close();
            }
        }catch (Exception e){
            log.error("return redis resource exception", e);
        }
    }
}
