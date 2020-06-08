package org.opentcs.skyvehicle;

public interface VelocityListener {

    /**
     * Called when a new velocity value (in mm/s) has been computed.
     *在计算出新的速度值（以毫米/秒为单位）时调用。
     * @param velocityValue The new velocity value that has been computed.已计算的新速度值。
     */
    void addVelocityValue(int velocityValue);
}
