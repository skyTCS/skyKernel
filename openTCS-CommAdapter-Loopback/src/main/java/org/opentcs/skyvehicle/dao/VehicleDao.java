package org.opentcs.skyvehicle.dao;

import org.opentcs.skyvehicle.bean.Car;
import org.opentcs.testvehicle.util.JedisUtil;
import org.opentcs.testvehicle.util.JsonUtil;
import redis.clients.jedis.Jedis;

/**
 * 车辆数据提取
 * @author eternal
 */
public class VehicleDao {
  private Jedis jedis = null;
  //连接数据库
  public void conn() {
    jedis = JedisUtil.getJedis();

    System.out.println("redis lian jie cheng gong ");

  }
  /**
   * 取出当前车辆信息
   * @param key
   * @return 
   */
    public Car getCar(String key) {
        
       String str = jedis.get(key);
       JsonUtil jsonUtil = new JsonUtil();
      // System.out.println("-----car String = " + str);
       return (Car)jsonUtil.getObject(str, new Car());
    }
    
    //关闭数据库
    public void close(){
      jedis.close();
    }
}
