package org.opentcs.skyvehicle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkArgument;

public class VelocityHistory {

    /**
     * This class's Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(VelocityHistory.class);
    /**
     * The actual ring buffer, containing the velocity values.包含速度值的实际环形缓冲区。
     * 
     */
    private final int[] velocities;
    /**
     * The ring buffer's capacity.环形缓冲区的容量。
     */
    private final int velocityQueueCapacity;
    /**
     * A divisor for selecting the values that are actually written to the ring
     * buffer.除数，用于选择实际写入环形缓冲区的值。
     */
    private final int divisor;
    /**
     * A counter for the number of velocity values that have been given to this
     * listener so far.迄今已提供给此侦听器的速度值数量的计数器。
     */
    private int valueCounter;
    /**
     * The index of element in the ring buffer to receive the next value.
     * 环形缓冲区中元素的索引以接收下一个值。
     */
    private int writeIndex;

    /**
     * Creates a new instance of VelocityQueueListener.
     *创建一个VelocityQueueListener的新实例。
     * @param queueCapacity This listener's queue capacity.此侦听器的队列容量。
     * @param newDivisor A divisor for selecting values actually
     * written to the ring buffer. (I.e. only every <em>n</em>th value provided
     * via {@link #addVelocityValue(int) addVelocityValue()} will be written
     * to the ring buffer, where <em>n</em> = <code>newDivisor</code>.
     * 除数，用于选择实际写入环形缓冲区的值。 （即，仅将通过{@link #addVelocityValue（int）addVelocityValue（）}
     * 提供的每个<em> n </ em>个值写入环形缓冲区，其中<em> n </ em> = <code> newDivisor </ code>。
     */
    public VelocityHistory(int queueCapacity, int newDivisor) {
        checkArgument(queueCapacity >= 1,
                "queueCapacity is less than 1: %s",
                queueCapacity);
        checkArgument(newDivisor >= 1, "newDivisor is less than 1: %s", newDivisor);

        velocityQueueCapacity = queueCapacity;
        divisor = newDivisor;
        velocities = new int[queueCapacity];
    }

    /**
     * Adds a new velocity value to this history.
     *向此历史记录添加新的速度值。
     * @param newValue The value to be added.
     */
    public void addVelocityValue(int newValue) {
        LOG.debug("method entry");
        synchronized (velocities) {
            if (valueCounter % divisor == 0) {
                velocities[writeIndex] = newValue;
                writeIndex = (writeIndex + 1) % velocityQueueCapacity;
            }
            valueCounter++;
        }
    }

    /**
     * Returns a copy of this listener's ring buffer.
     *返回此侦听器的环形缓冲区的副本。
     * @return A copy of this listener's ring buffer.
     */
    public int[] getVelocities() {
        LOG.debug("method entry");
        int[] result = new int[velocityQueueCapacity];
        synchronized (velocities) {
            int firstCount = velocityQueueCapacity - writeIndex;
            System.arraycopy(velocities, writeIndex, result, 0, firstCount);
            if (firstCount != velocityQueueCapacity) {
                int secondCount = velocityQueueCapacity - firstCount;
                System.arraycopy(velocities, 0, result, firstCount, secondCount);
            }
        }
        return result;
    }

    /**
     * Returns this listener's queue capacity.
     *返回此侦听器的队列容量。
     * @return This listener's queue capacity.
     */
    public int getQueueSize() {
        LOG.debug("method entry");
        return velocityQueueCapacity;
    }

    /**
     * Clears this <code>VelocityQueueListener</code>'s queue, i.e. sets all
     * values in the queue to 0.清除此<code> VelocityQueueListener </ code>的队列，即将队列中的所有值设置为0。
     */
    public void clear() {
        LOG.debug("method entry");
        synchronized (velocities) {
            for (int i = 0; i < velocities.length; i++) {
                velocities[i] = 0;
            }
        }
    }
}