/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.testvehicle.util;

import org.eclipse.paho.client.mqttv3.*;

/**
 *
 * @author eternal
 */
public class MyMqttCallback implements MqttCallback{
  
  @Override
    public void connectionLost(Throwable thrwbl) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        System.out.println("lian jie diu shi-----------");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        System.out.println("wan cheng xiao xi chuan di。");
    }

    @Override
    public void messageArrived(String string, MqttMessage mm) throws Exception {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        System.out.println("--------------Topic==" + string);
        System.out.println("--------------message==" + mm.toString());
    }
  
}
