/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.testvehicle;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.opentcs.testvehicle.util.Mqttutil;
import org.opentcs.testvehicle.util.MyMessageListener;
import org.opentcs.testvehicle.util.MyMqttCallback;

/**
 *
 * @author eternal
 */
public class MqttMessageUtil {
  private Mqttutil mqttutil = null;
    private String url = "tcp://106.13.118.181:8083";
    private String id = "123456487964526";
    private String userName = "admin";
    private String password = "public";
    private int qos = 0;
    
    public static String paylodString = "--";

  public MqttMessageUtil() {
  }

  public MqttMessageUtil(String url, String id) {
    this.url = url;
    this.id = id;
  }
  
  public void connect(String userName,String password) {
    
    mqttutil=  new Mqttutil( url,  id,  userName,  password,  qos);
    try {
      mqttutil.connect(userName,password);
    }
    catch (MqttException ex) {
      System.out.println("+++++++++++++org.opentcs.testvehicle.MqttMessageUtil.connect()");
      Logger.getLogger(MqttMessageUtil.class.getName()).log(Level.SEVERE, null, ex);
    }
    
  }
  

  public void subscribe(String topics, int qos) {
    try {
      mqttutil.subscribe("testka",new MyMessageListener());
    }
    catch (MqttException ex) {
      Logger.getLogger(MqttMessageUtil.class.getName()).log(Level.SEVERE, null, ex);
    }
    
  }
    
  public void publish(String topic, String payload){
    
  }
  
  public String getPaylod () {
    return paylodString;
  }
}
