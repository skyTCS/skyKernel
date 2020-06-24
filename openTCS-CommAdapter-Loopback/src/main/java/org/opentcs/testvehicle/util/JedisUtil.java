/**
 * Title:JedisUtil.java
 * 功能；redis连接池
 * Author: star
 * Creation time: 2020-5-24 21:30
 * Modification time：
 * Version： V1.0
 */
package org.opentcs.testvehicle.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisUtil {
    private static JedisPool jedisPool;
 
    static {
        // 配置连接池
        JedisPoolConfig config = new JedisPoolConfig();
        // 设置最大连接数
        

        // 设置最大阻塞时间，记住是毫秒数milliseconds
        

        // 设置空间连接
        config.setMaxIdle(10);

 
        // 创建连接池
        jedisPool = new JedisPool(config, "localhost");
        //jedisPool = new JedisPool(config, "212.64.70.83");
    }
 
    /**
     * 获取redis连接
     */
    public static Jedis getJedis() {
        return jedisPool.getResource();
    }
}
