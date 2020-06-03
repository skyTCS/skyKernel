package org.opentcs.skyvehicle;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;

/**
 * 环回通信适配器（虚拟车辆）的工厂。
 * @author eternal
 */
public class MqttCommunicationAdapterFactory 
    implements VehicleCommAdapterFactory{

  //适配器组件工厂
  private final MqttAdapterComponentsFactory adapterFactory;
  //指示此组件是否已初始化。
  private boolean initialized;

  /**
   * 创建一个新的工厂
   * @param componentsFactory 适配器组件工厂。
   */
  @Inject
  public MqttCommunicationAdapterFactory(MqttAdapterComponentsFactory componentsFactory) {
    this.adapterFactory = requireNonNull(componentsFactory, "componentsFactory");
  }

  /**
   * 返回描述工厂/提供的适配器的字符串。这应该是一个短字符串，可以显示为例如菜单项，以便在车辆的多个工厂/适配器类型之间进行选择。
   * @return 
   */
  @Override
  public String getAdapterDescription() {
    //这是Kernel中显示的驱动名称,中文会乱码，如果要使用中文，请使用配置文件
    return "MqttAdapter";
  }

  /**
   * 埋坑
   * @return 
   */
  @Override
  public VehicleCommAdapterDescription getDescription() {
    return VehicleCommAdapterFactory.super.getDescription(); //To change body of generated methods, choose Tools | Templates.
  }

  /**
   * 检查该工厂是否可以为给定的车辆提供通信适配器。
   * @param vhcl
   * @return 
   */
  @Override
  public boolean providesAdapterFor(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");
    return true;
  }

  /**
   * 返回用于控制给定车辆的通信适配器。
   * @param vehicle
   * @return 
   */
  @Override
  public MqttCommunicationAdapter getAdapterFor(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");
    return adapterFactory.createMqttCommAdapter(vehicle);
  }

  /**
   * （重新）在使用该组件之前将其初始化。
   */
  @Override
  public void initialize() {
    if (initialized) {
       //System.out.println("org.opentcs.skyvehicle.MqttCommunicationAdapterFactory.initialize()");
       System.out.println("----chu shi hua + Already initialized.");
      return;
    }
    initialized = true;
  }

  /**
   * 检查此组件是否已初始化。
   * @return 
   */
  @Override
  public boolean isInitialized() {
     return initialized; 
  }

  /**
   * 终止实例并释放资源。
   */
  @Override
  public void terminate() {
    if (!initialized) {
      System.out.println("----wei chu shi hua + Not initialized.");
      return;
    }
    initialized = false;
  }
  
}
