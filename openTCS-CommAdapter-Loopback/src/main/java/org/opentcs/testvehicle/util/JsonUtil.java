package org.opentcs.testvehicle.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonUtil<T> {

    public Object getObject(String str, T t){
        //Class<?> sClass = (T)Class.forName(t.getClass());

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(str, t.getClass());
            
        }
        catch (JsonProcessingException ex) {
            ex.printStackTrace();
            System.out.println(JsonUtil.class.getName()+"********");
            //Logger.getLogger(JsonUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
