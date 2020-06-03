/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.skyvehicle;

import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;

/**
 *{@link MqttVehicleModel}的可序列化表示。
 * @author eternal
 */
public class MqttVehicleModelTO extends VehicleProcessModelTO {
  
  /**
   * Whether this communication adapter is in single step mode or not (i.e. in automatic mode).
   * 此通信适配器是否处于单步模式（即处于自动模式）。
   */
  private boolean singleStepModeEnabled;
  /**
   * Indicates which operation is a loading operation.
   * 指示哪个操作是加载操作。
   */
  private String loadOperation;
  /**
   * 指示哪个操作是卸载操作。
   * Indicates which operation is an unloading operation.
   */
  private String unloadOperation;
  /**
   * The time needed for executing operations.
   * 执行操作所需的时间。
   */
  private int operatingTime;
  /**
   * The maximum acceleration.最大加速度。
   */
  private int maxAcceleration;
  /**
   * The maximum deceleration.最大减速度。
   */
  private int maxDeceleration;
  /**
   * The maximum forward velocity.最大前进速度。
   */
  private int maxFwdVelocity;
  /**
   * The maximum reverse velocity.最大反向速度。
   */
  private int maxRevVelocity;
  /**
   * Whether the vehicle is paused or not.车辆是否暂停。
   */
  private boolean vehiclePaused;
  
  public boolean isSingleStepModeEnabled() {
    return singleStepModeEnabled;
  }

  public MqttVehicleModelTO setSingleStepModeEnabled(boolean singleStepModeEnabled) {
    this.singleStepModeEnabled = singleStepModeEnabled;
    return this;
  }

  public String getLoadOperation() {
    return loadOperation;
  }

  public MqttVehicleModelTO setLoadOperation(String loadOperation) {
    this.loadOperation = loadOperation;
    return this;
  }

  public String getUnloadOperation() {
    return unloadOperation;
  }

  public MqttVehicleModelTO setUnloadOperation(String unloadOperation) {
    this.unloadOperation = unloadOperation;
    return this;
  }

  public int getOperatingTime() {
    return operatingTime;
  }

  public MqttVehicleModelTO setOperatingTime(int operatingTime) {
    this.operatingTime = operatingTime;
    return this;
  }

  public int getMaxAcceleration() {
    return maxAcceleration;
  }

  public MqttVehicleModelTO setMaxAcceleration(int maxAcceleration) {
    this.maxAcceleration = maxAcceleration;
    return this;
  }

  public int getMaxDeceleration() {
    return maxDeceleration;
  }

  public MqttVehicleModelTO setMaxDeceleration(int maxDeceleration) {
    this.maxDeceleration = maxDeceleration;
    return this;
  }

  public int getMaxFwdVelocity() {
    return maxFwdVelocity;
  }

  public MqttVehicleModelTO setMaxFwdVelocity(int maxFwdVelocity) {
    this.maxFwdVelocity = maxFwdVelocity;
    return this;
  }

  public int getMaxRevVelocity() {
    return maxRevVelocity;
  }

  public MqttVehicleModelTO setMaxRevVelocity(int maxRevVelocity) {
    this.maxRevVelocity = maxRevVelocity;
    return this;
  }

  public boolean isVehiclePaused() {
    return vehiclePaused;
  }

  public MqttVehicleModelTO setVehiclePaused(boolean vehiclePaused) {
    this.vehiclePaused = vehiclePaused;
    return this;
  }
}
