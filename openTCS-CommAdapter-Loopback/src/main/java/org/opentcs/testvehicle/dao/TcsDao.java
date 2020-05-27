package org.opentcs.testvehicle.dao;
/**
 * Title:TcsDao.java
 * 功能；存储操作
 * Author: star
 * Creation time: 2020-5-26 18:10
 * Modification time：
 * Version： V1.0
 */
import com.yhtos.mqtt.redisUtil.JedisUtil;
import org.opentcs.testvehicle.bean.Car;
import org.opentcs.testvehicle.util.JsonUtil;
import redis.clients.jedis.Jedis;

public class TcsDao {
  private Jedis jedis = null;
  public void conn() {
    jedis = JedisUtil.getJedis();
        //jedis.connect);//连接
        //jedis.disconnect();//断开连接
        System.out.println("lian jie cheng gong ");
        //查看服务是否运行
        //System.out.println("zheng zai yun xing : "+jedis.ping());
  }
    public Car getCar(String key) {
        
       String str = jedis.get(key);
       JsonUtil jsonUtil = new JsonUtil();
       System.out.println("-----car String = " + str);
       return (Car)jsonUtil.getObject(str, new Car());
       
    }
    
    public void close(){
      jedis.close();
    }

}
