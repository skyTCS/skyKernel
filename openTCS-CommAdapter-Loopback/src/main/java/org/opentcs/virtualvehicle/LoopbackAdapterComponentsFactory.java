/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import org.opentcs.data.model.Vehicle;

/**
 * A factory for various loopback specific instances.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface LoopbackAdapterComponentsFactory {

  /**
   * Creates a new LoopbackCommunicationAdapter for the given vehicle.
   *
   * @param vehicle The vehicle.
   * @return A new LoopbackCommunicationAdapter for the given vehicle.
   */
  LoopbackCommunicationAdapter createLoopbackCommAdapter(Vehicle vehicle);

  /**
   * Creates a new panel for the given comm adapter.
   *为给定的通讯适配器创建一个新面板。
   * @param commAdapter The comm adapter to create a panel for.用于为其创建面板的通讯适配器。
   * @return A new panel for the given comm adapter.给定通讯适配器的新面板。
   */
  @Deprecated
  LoopbackCommunicationAdapterPanel createPanel(LoopbackCommunicationAdapter commAdapter);
}
