/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.testvehicle;

import org.opentcs.data.model.Vehicle;

/**
 *
 * @author eternal
 */
public interface TestAdapterComponentsFactory {
  TestCommAdapter createCommAdapter(Vehicle vehicle);
}
