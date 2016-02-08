package com.nathanlea;

/**
 * Created by Nathan on 2/8/2016.
 */
public class Job implements Comparable{

    private int     size = 0;
    private int     duration = 0;
    private boolean active = false;
    private boolean waiting = false;

    private int     creationTime = 0;
    private int     waitingStarted = 0;
    private int     totalWaiting = 0;
    private int     timeActivated = 0;
    private MemoryBlock memoryBlock;

    private boolean rejected = false;
    private int     rejectedTime = 0;

    private boolean completed = false;

    public Job(int size, int duration, int creationTime) {
        this.size = size;
        this.duration = duration;
        this.creationTime = creationTime;
    }

    public void setWaiting(int waitingTime) {
        this.waitingStarted = waitingTime;
        this.waiting = true;
    }

    public void activateJob(int currentTime, MemoryBlock memoryBlock) {
        this.waiting = false;
        this.active = true;
        totalWaiting  = currentTime-waitingStarted;
        this.timeActivated = currentTime;
        this.memoryBlock = memoryBlock;
        this.memoryBlock.setJob(this);
    }

    public void completeJob(){
        this.completed = true;
        memoryBlock.setCompleted();
        this.memoryBlock = null;
    }

    public void rejectJob(int currentTime) {
        rejected = true;
        this.rejectedTime = currentTime;
    }

    public int getSize() {
        return size;
    }

    public int getDuration() {
        return duration;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isWaiting() {
        return waiting;
    }

    public int getCreationTime() {
        return creationTime;
    }

    public int getWaitingStarted() {
        return waitingStarted;
    }

    public int getTotalWaiting() {
        return totalWaiting;
    }

    public int getTimeActivated() {
        return timeActivated;
    }

    public MemoryBlock getMemoryBlock() {
        return memoryBlock;
    }

    public boolean isRejected() {
        return rejected;
    }

    public int getRejectedTime() {
        return rejectedTime;
    }

    public boolean isCompleted() {
        return completed;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
