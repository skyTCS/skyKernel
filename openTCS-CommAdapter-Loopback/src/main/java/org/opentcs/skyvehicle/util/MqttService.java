/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.skyvehicle.util;

import com.yhtos.mqtt.util.MqttUtil;

/**
 * mqtt数据服务
 * @author eternal
 */
public class MqttService {
  
  private MqttUtil mqttUtil;
  

  public MqttService() {
    mqttUtil = new MqttUtil();
    mqttUtil.init("emqx.properties");
    mqttUtil.connect();
  }
  
  
  
  
  
}
