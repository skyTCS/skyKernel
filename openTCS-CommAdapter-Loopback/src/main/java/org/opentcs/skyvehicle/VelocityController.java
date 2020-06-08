package org.opentcs.skyvehicle;

import org.opentcs.data.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class VelocityController {
    /**
     * This class's Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(VelocityController.class);
    /**
     * The maximum deceleration of the vehicle (in mm/s<sup>2</sup>).
     * 车辆的最大减速度（毫米/秒<sup> 2 </ sup>）。
     */
    private int maxDeceleration;
    /**
     * The maximum acceleration of the vehicle (in mm/s<sup>2</sup>).
     * 车辆的最大加速度（单位为mm / s <sup> 2 </ sup>）。
     */
    private int maxAcceleration;
    /**
     * The maximum reverse velocity of the vehicle (in mm/s).
     * 车辆的最大后退速度（毫米/秒）。
     */
    private int maxRevVelocity;
    /**
     * The maximum forward velocity of the vehicle (in mm/s).
     * 车辆的最大前进速度（毫米/秒）。
     */
    private int maxFwdVelocity;
    /**
     * The current acceleration (in mm/s<sup>2</sup>).当前加速度（单位为mm / s <sup> 2 </ sup>）。
     */
    private int currentAcceleration;
    /**
     * The current velocity (in mm/s).当前速度（毫米/秒）。
     */
    private int currentVelocity;
    /**
     * The current position (in mm from the beginning of the current way entry).
     * 当前位置（距当前航路条目开始处的毫米）。
     */
    private int currentPosition;
    /**
     * The current time, relative to the point of time at which this velocity
     * controller was created.      当前时间，相对于创建此速度控制器的时间点。
     */
    private long currentTime;
    /**
     * This controller's processing queue.该控制器的处理队列。
     */
    private final Queue<WayEntry> wayEntries = new LinkedList<>();
    /**
     * A set of velocity listeners.一组速度监听器。
     */
    private final Set<VelocityListener> velocityListeners = new HashSet<>();
    /**
     * True, if the vehicle has been paused, e.g. via the kernel gui
     * or a by a client message.      True，如果车辆已经暂停，例如 通过内核gui或通过客户端消息。
     */
    private boolean paused;

    /**
     * Creates a new VelocityController.
     *创建一个新的VelocityController
     * @param maxDecel The maximum deceleration of the vehicle (in
     * mm/s<sup>2</sup>).车辆最大减速度
     * @param maxAccel The maximum acceleration of the vehicle (in
     * mm/s<sup>2</sup>).车辆的最大加速度
     * @param maxRevVelo The maximum reverse velocity of the vehicle (in mm/s).车辆的最大倒车速度
     * @param maxFwdVelo The maximum forward velocity of the vehicle (in mm/s).车辆的最大前进速度
     */
    public VelocityController(int maxDecel,
                              int maxAccel,
                              int maxRevVelo,
                              int maxFwdVelo) {
        maxDeceleration = maxDecel;
        maxAcceleration = maxAccel;
        maxRevVelocity = maxRevVelo;
        maxFwdVelocity = maxFwdVelo;
        paused = false;
    }

    /**
     * Returns the maximum deceleration.
     *返回最大减速度。
     * @return The maximum deceleration
     */
    public int getMaxDeceleration() {
        return maxDeceleration;
    }

    /**
     * Sets the maximum deceleration.
     *设置最大减速度。
     * @param maxDeceleration The new maximum deceleration
     */
    public void setMaxDeceleration(int maxDeceleration) {
        this.maxDeceleration = maxDeceleration;
    }

    /**
     * Returns the maximum acceleration.
     *返回最大加速度。
     * @return The maximum acceleration
     */
    public int getMaxAcceleration() {
        return maxAcceleration;
    }

    /**
     * Sets the maximum acceleration.
     *设置最大加速度。
     * @param maxAcceleration The new maximum acceleration
     */
    public void setMaxAcceleration(int maxAcceleration) {
        this.maxAcceleration = maxAcceleration;
    }

    /**
     * Returns the maximum reverse velocity.
     *返回最大反向速度。
     * @return The maximum reverse velocity
     */
    public int getMaxRevVelocity() {
        return maxRevVelocity;
    }

    /**
     * Sets the maximum reverse velocity.
     *设置最大反向速度。
     * @param maxRevVelocity The new maximum reverse velocity
     */
    public void setMaxRevVelocity(int maxRevVelocity) {
        this.maxRevVelocity = maxRevVelocity;
    }

    /**
     * Returns the maximum forward velocity.
     *返回最大前进速度。
     * @return The maximum forward velocity
     */
    public int getMaxFwdVelocity() {
        return maxFwdVelocity;
    }

    /**
     * Sets the maximum forward velocity.
     *设置最大前进速度。
     * @param maxFwdVelocity The new maximum forward velocity
     */
    public void setMaxFwdVelocity(int maxFwdVelocity) {
        this.maxFwdVelocity = maxFwdVelocity;
    }

    /**
     * Returns whether the vehicle is paused.
     *返回车辆是否暂停。
     * @return paused
     */
    public boolean isVehiclePaused() {
        return paused;
    }

    /**
     * Pause the vehicle (i.e. set it's velocity to zero).
     *暂停车辆（即将其速度设置为零）。
     * @param pause True, if vehicle shall be paused. False, otherwise.
     */
    public void setVehiclePaused(boolean pause) {
        paused = pause;
    }

    /**
     * Adds a velocity listener to this vehicle controller's set of listeners.
     *将速度侦听器添加到此车辆控制器的侦听器集中。
     * @param listener The velocity listener to be added.
     */
    public void addVelocityListener(VelocityListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener is null");
        }
        velocityListeners.add(listener);
    }

    /**
     * Removes a velocity listener from this vehicle controller's set of listeners.
     *从此车辆控制器的侦听器集中删除速度侦听器。
     * @param listener The velocity listener to be removed.
     */
    public void removeVelocityListener(VelocityListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }
        velocityListeners.remove(listener);
    }

    /**
     * Returns this controller's current velocity.
     *返回此控制器的当前速度。
     * @return This controller's current velocity.
     */
    public int getCurrentVelocity() {
        return currentVelocity;
    }

    /**
     * Returns the vehicle's current position (in mm from the beginning of the
     * current way entry.
     * 返回车辆的当前位置（距离当前路口开始处的毫米）。
     * @return The vehicle's current position (in mm from the beginning of the
     * current way entry.
     */
    public int getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Returns the current time, relative to to the point of time at which this
     * controller was started.
     * 返回相对于此控制器启动时间的当前时间。
     * @return The current time, relative to to the point of time at which this
     * controller was started.
     */
    public long getCurrentTime() {
        return currentTime;
    }

    /**
     * Adds a way entry to this vehicle controller's processing queue.
     * 向该车辆控制器的处理队列添加路标。
     * @param newEntry The way entry to add.
     */
    public void addWayEntry(WayEntry newEntry) {
        if (newEntry == null) {
            throw new NullPointerException("newEntry is null");
        }
        wayEntries.add(newEntry);
    }

    /**
     * Returns the way entry this velocity controller is currently processing.
     * 返回此速度控制器当前正在处理的输入方式。
     * @return The way entry this velocity controller is currently processing. If
     * the processing queue is currently empty, <code>null</code> is returned.
     */
    public WayEntry getCurrentWayEntry() {
        return wayEntries.peek();
    }

    /**
     * Returns <code>true</code> if, and only if, there are way entries to be
     * processed in this velocity controller's queue.
     * 当且仅当在此速度控制器的队列中存在要处理的方法条目时，才返回<code> true </ code>。
     * @return <code>true</code> if, and only if, there are way entries to be
     * processed in this velocity controller's queue.
     */
    public boolean hasWayEntries() {
        return !wayEntries.isEmpty();
    }

    /**
     * Increase this controller's current time by the given value and simulate
     * the events that would happen in this time frame.
     * 将控制器的当前时间增加给定值，并模拟在此时间范围内将发生的事件。
     * @param dt The time by which to advance this controller (in milliseconds).
     * Must be at least 1.  推进此控制器的时间（以毫秒为单位）。必须至少为1。
     */
    public void advanceTime(int dt) {
        if (dt < 1) {
            throw new IllegalArgumentException("dt is less than 1");
        }
        final int oldPosition = currentPosition;
        final int oldVelocity = currentVelocity;
        final Iterator<WayEntry> wayEntryIter = wayEntries.iterator();
        final WayEntry curWayEntry
                = wayEntryIter.hasNext() ? wayEntryIter.next() : null;
        final int targetVelocity;
        final long accelerationDistance;
        if (curWayEntry == null || paused) {
            targetVelocity = 0;
            accelerationDistance = 1;
            currentAcceleration = 0;
            currentVelocity = 0;
        }
        else {
            final int maxVelocity;
            final Vehicle.Orientation orientation = curWayEntry.vehicleOrientation;
            switch (orientation) {
                case FORWARD:
                    maxVelocity = maxFwdVelocity;
                    break;
                case BACKWARD:
                    maxVelocity = maxRevVelocity;
                    break;
                default:
                    LOG.warn("Unhandled orientation: {}, assuming forward.", orientation);
                    maxVelocity = maxFwdVelocity;
            }
            targetVelocity = Math.min(curWayEntry.targetVelocity, maxVelocity);
            // Accelerate as quickly as possible.尽快加速。
            accelerationDistance = 10;
            // Recompute the acceleration to reach/keep the desired velocity.重新计算加速度以达到/保持所需的速度。
            currentAcceleration
                    = (currentVelocity == targetVelocity) ? 0
                    : suitableAcceleration(targetVelocity, accelerationDistance);
            // Recompute current velocity.重新计算当前速度。
            currentVelocity = oldVelocity + currentAcceleration * dt / 1000;
            // Recompute current position.重新计算当前位置。
            currentPosition = oldPosition + oldVelocity * dt / 1000
                    + currentAcceleration * dt * dt / 1000000 / 2;
            // Check if we have left the way entry and entered the next.检查我们是否已离开道路入口并进入下一个道路。
            if (currentPosition >= curWayEntry.length) {
                currentPosition -= curWayEntry.length;
                wayEntries.poll();
            }
        }
        // The given time has now passed.
        currentTime += dt;
        // Let the listeners know about the new velocity value.
        for (VelocityListener curListener : velocityListeners) {
            curListener.addVelocityValue(currentVelocity);
        }
    }

    /**
     * Returns the acceleration (in mm/s<sup>2</sup>) needed for reaching a given
     * velocity exactly after travelling a given distance (respecting the current
     * velocity).
     *
     * @param targetVelocity The desired velocity (in mm/s).
     * @param travelDistance The distance after which the desired velocity is
     * supposed to be reached (in mm). Must be a positive value.
     * @return The acceleration needed for reaching the given velocity after
     * travelling the given distance.
     */
    int suitableAcceleration(final int targetVelocity, final long travelDistance) {
        if (travelDistance < 1) {
            throw new IllegalArgumentException("travelDistance is less than 1");
        }
        final double v_current = currentVelocity;
        final double v_target = targetVelocity;
        final double s = travelDistance;
        // Compute travelling time.
        // XXX Divide by zero if (v_current == -v_target), especially if both are 0!
        final double t = s / (v_current + (v_target - v_current) / 2);
        LOG.debug("t = " + t
                + "; s = " + s
                + "; v_current = " + v_current
                + "; v_target = " + v_target);
        // Compute acceleration.
        int result = (int) ((v_target - v_current) / t);
        LOG.debug("result = " + result);
        if (result > maxAcceleration) {
            result = maxAcceleration;
        }
        else if (result < maxDeceleration) {
            result = maxDeceleration;
        }
        return result;
    }

    /**
     * An entry in a vehicle controller's processing queue.
     * 车辆控制器的处理队列中的条目。
     */
    public static class WayEntry
            implements Serializable {

        /**
         * The length of the way to drive (in mm).驱动方式的长度（以毫米为单位）。
         */
        private final long length;
        /**
         * The target velocity on this way (in mm/s).这样的目标速度（单位：mm / s）。
         */
        private final int targetVelocity;
        /**
         * The name of the destination point.目的点的名称。
         */
        private final String destPointName;
        /**
         * The vehicle's orientation on this way.车辆以这种方式定向。
         */
        private final Vehicle.Orientation vehicleOrientation;

        /**
         * Creates a new WayEntry.创建一个新的WayEntry。
         *
         * @param length The length of the way to drive (in mm).驱动方式的长度（以毫米为单位）。
         * @param maxVelocity The maximum velocity on this way (in mm/s).这样最大速度
         * @param destPointName The name of the destination point.目的点的名称。
         * @param orientation The vehicle's orientation on this way.车辆以这种方式定向。
         */
        public WayEntry(long length,
                        int maxVelocity,
                        String destPointName,
                        Vehicle.Orientation orientation) {
            checkArgument(length > 0, "length is not > 0 but %s", length);
            this.length = length;
            if (maxVelocity < 1) {
                LOG.warn("maxVelocity is zero or negative, setting to 100");
                this.targetVelocity = 100;
            }
            else {
                this.targetVelocity = maxVelocity;
            }
            this.destPointName = requireNonNull(destPointName, "destPointName");
            this.vehicleOrientation = requireNonNull(orientation, "vehicleOrientation");
        }

        /**
         * Returns the name of the destination point.
         *
         * @return The name of the destination point.
         */
        public String getDestPointName() {
            return destPointName;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof WayEntry) {
                WayEntry other = (WayEntry) o;
                return other.length == length
                        && other.targetVelocity == targetVelocity
                        && destPointName.equals(other.destPointName)
                        && vehicleOrientation.equals(other.vehicleOrientation);
            }
            else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return (int) (length ^ (length >>> 32))
                    ^ targetVelocity
                    ^ destPointName.hashCode()
                    ^ vehicleOrientation.hashCode();
        }
    }
}
