/**
 * Title:MqttMessageUtil.java
 * 功能；通信服务
 * Author: star
 * Creation time: 
 * Modification time：2020-5-26 18:20
 * Version： V1.0
 */
package org.opentcs.testvehicle;

import com.yhtos.mqtt.util.MqttUtil;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.opentcs.testvehicle.bean.Car;
import org.opentcs.testvehicle.dao.TcsDao;



/**
 *
 * @author eternal
 */
public class MqttMessageUtil {
    /*private Mqttutil mqttutil = null;
    private String url = "tcp://106.13.118.181:8083";
    private String id = "123456487964526";
    private String userName = "admin";
    private String password = "public";
    private int qos = 0;*/
    private MqttUtil mqttUtil = null;
    public static String paylodString = "";
    private TcsDao tcsDao = null;
  
  public void connect() {
    tcsDao = new TcsDao();
    mqttUtil = new MqttUtil();
    MqttUtil.init("emqx.properties");
    try {
      tcsDao.conn();
      mqttUtil.connect();
    }
    catch (Exception ex) {
      ex.printStackTrace();
      System.out.println("----org.opentcs.testvehicle.MqttMessageUtil.connect()+++shi bai");
    }
    
  }
  
  public void close() {
    tcsDao.close();
  }
  

  public void subscribe(String topics, int qos) {
    try {
      mqttUtil.subscribe(topics, qos);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      System.out.println("----org.opentcs.testvehicle.MqttMessageUtil.subscribe()");
    }
    
  }
    
  public void publish(String topic, String payload){
    
  }
  
  public Car getPaylod (String key) {
    
    TcsDao tcsDao = new TcsDao();
    
    
    return tcsDao.getCar(key);
  }
}
