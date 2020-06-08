/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.zjw.vehicle.ExampleAdapterComponentsFactory;
import com.zjw.vehicle.ExampleCommAdapterFactory;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.opentcs.skyvehicle.MqttAdapterComponentsFactory;
import org.opentcs.skyvehicle.MqttCommunicationAdapterFactory;
import org.opentcs.testvehicle.TestAdapterComponentsFactory;
import org.opentcs.testvehicle.TestCommAdapterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures/binds the loopback communication adapters of the openTCS kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LoopbackCommAdapterModule
    extends KernelInjectionModule {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(LoopbackCommAdapterModule.class);

  @Override
  protected void configure() {
    VirtualVehicleConfiguration configuration
        = getConfigBindingProvider().get(VirtualVehicleConfiguration.PREFIX,
                                         VirtualVehicleConfiguration.class);

    if (!configuration.enable()) {
      LOG.info("Loopback driver disabled by configuration.");
      return;
    }

    bind(VirtualVehicleConfiguration.class)
        .toInstance(configuration);

    //install(new FactoryModuleBuilder().build(LoopbackAdapterComponentsFactory.class));
    
    /**//**/
    //install(new FactoryModuleBuilder().build(TestAdapterComponentsFactory.class));
    //vehicleCommAdaptersBinder().addBinding().to(TestCommAdapterFactory.class);
    
    install(new FactoryModuleBuilder().build(MqttAdapterComponentsFactory.class));
    vehicleCommAdaptersBinder().addBinding().to(MqttCommunicationAdapterFactory.class);
    /**//**/
    
    // tag::documentation_createCommAdapterModule[]
    //vehicleCommAdaptersBinder().addBinding().to(LoopbackCommunicationAdapterFactory.class);
    // end::documentation_createCommAdapterModule[]
    
    //install(new FactoryModuleBuilder().build(ExampleAdapterComponentsFactory.class));
    //    vehicleCommAdaptersBinder().addBinding().to(ExampleCommAdapterFactory.class);
  }

}
