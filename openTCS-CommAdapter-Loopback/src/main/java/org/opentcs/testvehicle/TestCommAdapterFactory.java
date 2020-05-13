/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.testvehicle;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author eternal
 */
public class TestCommAdapterFactory implements VehicleCommAdapterFactory{
  
  private static final Logger LOG = LoggerFactory.getLogger(TestCommAdapterFactory.class);
  private TestAdapterComponentsFactory componentsFactory;
  private boolean initialized;
  
 @Inject
  public TestCommAdapterFactory(TestAdapterComponentsFactory componentsFactory) {
    this.componentsFactory = requireNonNull(componentsFactory, "componentsFactory");
  }

  @Override
  @Deprecated
  public String getAdapterDescription() {
    //这是Kernel中显示的驱动名称,中文会乱码，如果要使用中文，请使用配置文件
    return "MyTestAdapter";
    //return ResourceBundle.getBundle("org/opentcs/virtualvehicle/Bundle").getString("AdapterTestFactoryDescription");
  }

  
  @Override
  public VehicleCommAdapterDescription getDescription() {
    return VehicleCommAdapterFactory.super.getDescription(); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean providesAdapterFor(Vehicle vehicle) {
     requireNonNull(vehicle, "vehicle");
    return true;
  }

  @Override
  public VehicleCommAdapter getAdapterFor(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");
    return componentsFactory.createCommAdapter(vehicle);
  }

  @Override
  public void initialize() {
    if (initialized) {
      LOG.debug("Already initialized.");
      return;
    }
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized; 
  }

  @Override
  public void terminate() {
    if (!initialized) {
      LOG.debug("Not initialized.");
      return;
    }
    initialized = false;
  }
  
}
