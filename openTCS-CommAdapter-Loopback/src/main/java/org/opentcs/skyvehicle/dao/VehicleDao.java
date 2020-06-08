/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.skyvehicle.dao;

import org.opentcs.skyvehicle.bean.Car;
import org.opentcs.testvehicle.util.JedisUtil;
import org.opentcs.testvehicle.util.JsonUtil;
import redis.clients.jedis.Jedis;

/**
 *
 * @author eternal
 */
public class VehicleDao {
  private Jedis jedis = null;
  public void conn() {
    jedis = JedisUtil.getJedis();

    System.out.println("redis lian jie cheng gong ");

  }
    public Car getCar(String key) {
        
       String str = jedis.get(key);
       JsonUtil jsonUtil = new JsonUtil();
       System.out.println("-----car String = " + str);
       return (Car)jsonUtil.getObject(str, new Car());
    }
    
    //
    public void close(){
      jedis.close();
    }
}
