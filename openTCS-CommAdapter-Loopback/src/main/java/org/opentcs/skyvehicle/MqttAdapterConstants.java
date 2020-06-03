/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.skyvehicle;

/**
 *
 * @author eternal
 */
public interface MqttAdapterConstants {
    public static final String PROPKEY_INITIAL_POSITION = "mqtt:initialPosition";
    public static final String PROPKEY_OPERATING_TIME = "mqtt:operatingTime";
    public static final String PROPKEY_LOAD_OPERATION = "mqtt:loadOperation";
    public static final String PROPVAL_LOAD_OPERATION_DEFAULT = "Load cargo";
    public static final String PROPKEY_UNLOAD_OPERATION = "mqtt:unloadOperation";
    public static final String PROPVAL_UNLOAD_OPERATION_DEFAULT = "Unload cargo";
    public static final String PROPKEY_ACCELERATION = "mqtt:acceleration";
    public static final String PROPKEY_DECELERATION = "mqtt:deceleration";
}
