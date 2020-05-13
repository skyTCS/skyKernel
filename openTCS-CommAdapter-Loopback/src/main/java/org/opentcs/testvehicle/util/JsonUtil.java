/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.testvehicle.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.testvehicle.bean.Car;

/**
 *
 * @author eternal
 */
public class JsonUtil {
  
  public static Object getObject(String str, Class<?> sClass){
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(str, sClass);
    }
    catch (JsonProcessingException ex) {
      Logger.getLogger(JsonUtil.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }
  
}
