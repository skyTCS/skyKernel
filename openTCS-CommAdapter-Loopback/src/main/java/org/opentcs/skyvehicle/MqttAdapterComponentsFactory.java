package org.opentcs.skyvehicle;

import org.opentcs.data.model.Vehicle;

/**
 * 用于各种回送特定实例的工厂。
 * @author eternal
 */
public interface MqttAdapterComponentsFactory {
  
  /**
   * 为车辆创建一个新的MqttCommunicationAdapter
   * @param vehicle
   * @return 
   */
  MqttCommunicationAdapter createMqttCommAdapter(Vehicle vehicle);
  
}
