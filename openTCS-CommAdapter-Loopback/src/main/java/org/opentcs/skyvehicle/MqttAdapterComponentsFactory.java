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
  
  /**
   * Creates a new panel for the given comm adapter.
   *为给定的通讯适配器创建一个新面板。
   * @param commAdapter The comm adapter to create a panel for.用于为其创建面板的通讯适配器。
   * @return A new panel for the given comm adapter.给定通讯适配器的新面板。
   */
  @Deprecated
  MqttCommunicationAdapterPanel createPanel(MqttCommunicationAdapter commAdapter);
}
