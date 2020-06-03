/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.skyvehicle;

import javax.annotation.Nonnull;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.virtualvehicle.Parsers;
import org.opentcs.virtualvehicle.VelocityController;
import org.opentcs.virtualvehicle.VelocityHistory;
import org.opentcs.virtualvehicle.VelocityListener;

/**
 *虚拟车辆及其通讯适配器属性的可观察模型。
 * @author eternal
 */
public class MqttVehicleModel 
    extends VehicleProcessModel
    implements VelocityListener {
  
   /**
   * Indicates whether this communication adapter is in single step mode or not (i.e. in automatic
   * mode).指示此通信适配器是否处于单步模式（即处于自动模式）。
   */
  private boolean singleStepModeEnabled;
  /**
   * Indicates which operation is a loading operation.指示哪个操作是加载操作。
   */
  private final String loadOperation;
  /**
   * Indicates which operation is an unloading operation.指示哪个操作是卸载操作。
   */
  private final String unloadOperation;
  /**
   * The time needed for executing operations.执行操作所需的时间。
   */
  private int operatingTime;
  /**
   * The velocity controller for calculating the simulated vehicle's velocity and current position.
   * 速度控制器，用于计算模拟车辆的速度和当前位置。
   */
  private final VelocityController velocityController;
  /**
   * Keeps a log of recent velocity values.记录最近的速度值。
   */
  private final VelocityHistory velocityHistory = new VelocityHistory(100, 10);


  public MqttVehicleModel(Vehicle attachedVehicle) {
    super(attachedVehicle);
    this.velocityController = new VelocityController(parseDeceleration(attachedVehicle),
                                                     parseAcceleration(attachedVehicle),
                                                     attachedVehicle.getMaxReverseVelocity(),
                                                     attachedVehicle.getMaxVelocity());
    this.operatingTime = parseOperatingTime(attachedVehicle);
    this.loadOperation = extractLoadOperation(attachedVehicle);
    this.unloadOperation = extractUnloadOperation(attachedVehicle);
  }
  //获得加载操作
  public String getLoadOperation() {
    return this.loadOperation;
  }
  //获得卸载操作
  public String getUnloadOperation() {
    return this.unloadOperation;
  }

  /**
   * Sets this communication adapter's <em>single step mode</em> flag.
   *设置此通信适配器的<em>“单步模式” </ em>标志。
   * @param mode If <code>true</code>, sets this adapter to single step mode,
   * otherwise sets this adapter to flow mode.
   * 如果<code> true </ code>，则将该适配器设置为单步模式，否则将该适配器设置为流模式。
   */
  public synchronized void setSingleStepModeEnabled(final boolean mode) {
    boolean oldValue = singleStepModeEnabled;
    singleStepModeEnabled = mode;

    getPropertyChangeSupport().firePropertyChange(Attribute.SINGLE_STEP_MODE.name(),
                                                  oldValue,
                                                  mode);
  }

  /**
   * Returns this communication adapter's <em>single step mode</em> flag.
   * 返回此通信适配器的<em>单步模式</ em>标志。
   * @return <code>true</code> if, and only if, this adapter is currently in
   * single step mode.
   */
  public synchronized boolean isSingleStepModeEnabled() {
    return singleStepModeEnabled;
  }

  /**
   * Returns the default operating time.
   *
   * @return The default operating time
   */
  public synchronized int getOperatingTime() {
    return operatingTime;
  }

  /**
   * Sets the default operating time.
   *设置默认操作时间。
   * @param defaultOperatingTime The new default operating time
   */
  public synchronized void setOperatingTime(int defaultOperatingTime) {
    int oldValue = this.operatingTime;
    this.operatingTime = defaultOperatingTime;

    getPropertyChangeSupport().firePropertyChange(Attribute.OPERATING_TIME.name(),
                                                  oldValue,
                                                  defaultOperatingTime);
  }

  /**
   * Returns the maximum deceleration.
   *返回最大减速度。
   * @return The maximum deceleration
   */
  public synchronized int getMaxDecceleration() {
    return velocityController.getMaxDeceleration();
  }

  /**
   * Sets the maximum deceleration.
   *
   * @param maxDeceleration The new maximum deceleration
   */
  public synchronized void setMaxDeceleration(int maxDeceleration) {
    int oldValue = velocityController.getMaxDeceleration();
    velocityController.setMaxDeceleration(maxDeceleration);

    getPropertyChangeSupport().firePropertyChange(Attribute.DECELERATION.name(),
                                                  oldValue,
                                                  maxDeceleration);
  }

  /**
   * Returns the maximum acceleration.
   *
   * @return The maximum acceleration
   */
  public synchronized int getMaxAcceleration() {
    return velocityController.getMaxAcceleration();
  }

  /**
   * Sets the maximum acceleration.
   *
   * @param maxAcceleration The new maximum acceleration
   */
  public synchronized void setMaxAcceleration(int maxAcceleration) {
    int oldValue = velocityController.getMaxAcceleration();
    velocityController.setMaxAcceleration(maxAcceleration);

    getPropertyChangeSupport().firePropertyChange(Attribute.ACCELERATION.name(),
                                                  oldValue,
                                                  maxAcceleration);
  }

  /**
   * Returns the maximum reverse velocity.
   *
   * @return The maximum reverse velocity.
   */
  public synchronized int getMaxRevVelocity() {
    return velocityController.getMaxRevVelocity();
  }

  /**
   * Sets the maximum reverse velocity.
   *
   * @param maxRevVelocity The new maximum reverse velocity
   */
  public synchronized void setMaxRevVelocity(int maxRevVelocity) {
    int oldValue = velocityController.getMaxRevVelocity();
    velocityController.setMaxRevVelocity(maxRevVelocity);

    getPropertyChangeSupport().firePropertyChange(Attribute.MAX_REVERSE_VELOCITY.name(),
                                                  oldValue,
                                                  maxRevVelocity);
  }

  /**
   * Returns the maximum forward velocity.
   *
   * @return The maximum forward velocity.
   */
  public synchronized int getMaxFwdVelocity() {
    return velocityController.getMaxFwdVelocity();
  }

  /**
   * Sets the maximum forward velocity.
   *
   * @param maxFwdVelocity The new maximum forward velocity.
   */
  public synchronized void setMaxFwdVelocity(int maxFwdVelocity) {
    int oldValue = velocityController.getMaxFwdVelocity();
    velocityController.setMaxFwdVelocity(maxFwdVelocity);

    getPropertyChangeSupport().firePropertyChange(Attribute.MAX_FORWARD_VELOCITY.name(),
                                                  oldValue,
                                                  maxFwdVelocity);
  }

  /**
   * Returns whether the vehicle is paused.
   *
   * @return paused
   */
  public synchronized boolean isVehiclePaused() {
    return velocityController.isVehiclePaused();
  }

  /**
   * Pause the vehicle (i.e. set it's velocity to zero).
   *暂停车辆（即将其速度设置为零）。
   * @param pause True, if vehicle shall be paused. False, otherwise.
   */
  public synchronized void setVehiclePaused(boolean pause) {
    boolean oldValue = velocityController.isVehiclePaused();
    velocityController.setVehiclePaused(pause);

    getPropertyChangeSupport().firePropertyChange(Attribute.VEHICLE_PAUSED.name(),
                                                  oldValue,
                                                  pause);
  }

  /**
   * Returns the virtual vehicle's velocity controller.
   *
   * @return The virtual vehicle's velocity controller.
   */
  @Nonnull
  public VelocityController getVelocityController() {
    return velocityController;
  }

  /**
   * Returns a log of recent velocity values of the vehicle.
   *
   * @return A log of recent velocity values.
   */
  @Nonnull
  public VelocityHistory getVelocityHistory() {
    return velocityHistory;
  }
  
  
  /**//**//**/

  @Override
  public void addVelocityValue(int velocityValue) {
    // Store the new value in the history...将新值存储在历史记录中...
    velocityHistory.addVelocityValue(velocityValue);
    // ...and let all observers know about it....并让所有观察者知道这一点。
    getPropertyChangeSupport().firePropertyChange(Attribute.VELOCITY_HISTORY.name(),
                                                  null,
                                                  velocityHistory);
  }
  
  
  private int parseOperatingTime(Vehicle vehicle) {
    String opTime = vehicle.getProperty(MqttAdapterConstants.PROPKEY_OPERATING_TIME);
    // Ensure it's a positive value.
    return Math.max(Parsers.tryParseString(opTime, 5000), 1);
  }

  /**
   * Gets the maximum acceleration. If the user did not specify any, 1000(m/s²) is returned.
   *
   * @param vehicle the vehicle
   * @return the maximum acceleration.
   */
  private int parseAcceleration(Vehicle vehicle) {
    String acceleration = vehicle.getProperty(MqttAdapterConstants.PROPKEY_ACCELERATION);
    // Ensure it's a positive value.
    return Math.max(Parsers.tryParseString(acceleration, 500), 1);
  }

  /**
   * Gets the maximum decceleration. If the user did not specify any, 1000(m/s²) is returned.
   *
   * @param vehicle the vehicle
   * @return the maximum decceleration.
   */
  private int parseDeceleration(Vehicle vehicle) {
    String deceleration = vehicle.getProperty(MqttAdapterConstants.PROPKEY_DECELERATION);
    // Ensure it's a negative value.
    return Math.min(Parsers.tryParseString(deceleration, -500), -1);
  }

  private static String extractLoadOperation(Vehicle attachedVehicle) {
    String result = attachedVehicle.getProperty(MqttAdapterConstants.PROPKEY_LOAD_OPERATION);
    if (result == null) {
      result = MqttAdapterConstants.PROPVAL_LOAD_OPERATION_DEFAULT;
    }
    return result;
  }

  private static String extractUnloadOperation(Vehicle attachedVehicle) {
    String result = attachedVehicle.getProperty(MqttAdapterConstants.PROPKEY_UNLOAD_OPERATION);
    if (result == null) {
      result = MqttAdapterConstants.PROPVAL_UNLOAD_OPERATION_DEFAULT;
    }
    return result;
  }
  
  /**
   * Notification arguments to indicate some change.通知参数指示一些更改。
   */
  public static enum Attribute {
    /**
     * Indicates a change of the virtual vehicle's single step mode setting.
     * 指示虚拟车辆单步模式设置的更改。
     */
    SINGLE_STEP_MODE,
    /**
     * Indicates a change of the virtual vehicle's default operating time.
     * 指示虚拟车辆的默认运行时间的更改。
     */
    OPERATING_TIME,
    /**
     * Indicates a change of the virtual vehicle's maximum acceleration.
     * 指示虚拟车辆的最大加速度的变化。
     */
    ACCELERATION,
    /**
     * Indicates a change of the virtual vehicle's maximum deceleration.
     * 指示虚拟车辆的最大减速度的变化。
     */
    DECELERATION,
    /**
     * Indicates a change of the virtual vehicle's maximum forward velocity.
     * 指示虚拟车辆的最大前进速度的变化。
     */
    MAX_FORWARD_VELOCITY,
    /**
     * Indicates a change of the virtual vehicle's maximum reverse velocity.
     * 指示虚拟车辆的最大后退速度的变化。
     */
    MAX_REVERSE_VELOCITY,
    /**
     * Indicates a change of the virtual vehicle's paused setting.指示虚拟车辆的暂停设置的更改。
     */
    VEHICLE_PAUSED,
    /**
     * Indicates a change of the virtual vehicle's velocity history.
     * 指示虚拟车辆的速度历史记录的更改。
     */
    VELOCITY_HISTORY,
  }
}
