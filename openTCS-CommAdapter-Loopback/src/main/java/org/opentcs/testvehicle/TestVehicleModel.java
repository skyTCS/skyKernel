/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.testvehicle;

import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleProcessModel;

/**
 *
 * @author eternal
 */
public class TestVehicleModel extends VehicleProcessModel{

  public TestVehicleModel(Vehicle attachedVehicle) {
    super(attachedVehicle);
  }

}
