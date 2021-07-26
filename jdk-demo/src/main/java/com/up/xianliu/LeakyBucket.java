package com.up.xianliu;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;

import static java.lang.invoke.MethodHandles.lookup;

/**
 * 限流之漏桶算法
 * 1、一个具有初始容量的漏桶；
 * 1-1、保证先进先出；
 * 1-2、线程安全
 * 2、具有均速转发请求的功能
 */
public class LeakyBucket {
    /**
     * The water that is in the bucket.
     */
    private long left;
    /**
     * The timestamp of the last successful water injection.
     */
    private long lastInjectTime = System.currentTimeMillis();
    /**
     * The bucket capacity.
     */
    private long capacity;
    /**
     * The time required for the bucket to be drained.
     */
    private long duration;
    /**
     * The water leakage rate of the bucket, which is equal to capacity/duration.
     */
    private double velocity;

    public boolean tryAcquire(String key) {
        long now = System.currentTimeMillis();
        // Water in the bucket = Previously left water – Water leaked during the past period of time.
        // Water leaked during the last period of time = (Current time – Last water injection time) × Water leakage rate
        // If the current time is too far from the last water injection time (no water has been injected for a long time), the water left in the bucket is 0 (the bucket is drained).
        left = Math.max(0, left - (long)((now - lastInjectTime) * velocity));
        // If no water overflows after one unit volume of water is injected, access is allowed.
        if (left + 1 <= capacity) {
            lastInjectTime = now;
            left++;
            return true;
        } else {
            return false;
        }
    }
}



