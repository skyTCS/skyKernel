/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.testvehicle.util;

import org.eclipse.paho.client.mqttv3.*;
import org.opentcs.testvehicle.MqttMessageUtil;

/**
 *
 * @author eternal
 */
public class MyMessageListener implements IMqttMessageListener {
  @Override
    public void messageArrived(String string, MqttMessage mm) throws Exception {
      MqttMessageUtil.paylodString = mm.toString();
        //接口实现接收数据
        System.out.println("-+-+Interface implementation to receive data:::" + mm.toString());
    }
}
