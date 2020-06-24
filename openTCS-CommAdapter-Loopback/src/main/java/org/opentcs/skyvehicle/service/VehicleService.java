/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.skyvehicle.service;

import com.yhtos.mqtt.util.MqttUtil;
import org.opentcs.skyvehicle.bean.Car;
import org.opentcs.skyvehicle.dao.VehicleDao;

/**
 * 车辆服务
 * @author eternal
 */
public class VehicleService {
  
  private MqttUtil mqttUtil = null;
  private VehicleDao dao = null;

  public VehicleService() {
    mqttUtil = new MqttUtil();
    mqttUtil.init("emqx.properties");
    mqttUtil.connect();
    dao = new VehicleDao();

  }
  //启动车辆
  public void vehicleConnect(String topics, int qos) {
    dao.conn();
    mqttUtil.subscribe(topics, qos);
  }
  //返回车辆信息
  public Car getCar(String key){
    return dao.getCar(key);
  }
  
  //车辆作业操作
  public boolean sendWork(String operation,String topic) {
    System.out.println("------gong zuo zhong " + operation);
    mqttUtil.publish("order", operation, 0);
    String datas = "";
    return true;
  }
  
  public boolean sendPath(String path,String topic) {
    
    System.out.println("+++++++send da yin lu jing la la *：："+path);
    mqttUtil.publish("order", path, 0);
    
    return true;
  }
  
  //关车
  public void vehicleClose() {
    dao.close();
  }
  
  
}
