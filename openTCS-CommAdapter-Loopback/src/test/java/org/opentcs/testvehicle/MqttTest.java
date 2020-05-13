/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.testvehicle;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.*;

/**
 *
 * @author eternal
 */
public class MqttTest {
  
  /**
   *
   */
  @Test
  public void test () {
    MqttMessageUtil messageUtil = new MqttMessageUtil();
    
    
    messageUtil.connect("admin","public");
    messageUtil.subscribe("", 0);
    
    for (int i = 0; i < 10; i++) {
      try {
        Thread.sleep(2000);
        System.out.println("=========" + messageUtil.getPaylod());
      }
      catch (InterruptedException ex) {
        Logger.getLogger(MqttTest.class.getName()).log(Level.SEVERE, null, ex);
      }
      
    }
    System.out.println("-------------------------org.opentcs.testvehicle.MqttTest.test()");
  }
  
  
}
