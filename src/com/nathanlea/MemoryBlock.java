package com.nathanlea;

/**
 * Created by Nathan on 2/8/2016.
 */
public class MemoryBlock {

    private int base = 0;
    private int size = 0;
    private boolean used;
    private boolean active;
    private Job currentJob;

    public MemoryBlock(int base, int size) {
        this.base = base;
        this.size = size;
    }

    public void setJob( Job job ){
        this.currentJob = job;
        this.used = true;
        this.active = true;
    }

    public void setCompleted() {
        used = false;
        active = false;
    }

    /*
    @Return This method return the left over space that is to claimed by an unused memory block
     */
    public int reActivate( int size, Job newJob ) {
        int ret = this.size - size;
        this.size = size;
        used = true;
        active = true;
        currentJob = newJob;
        return ret;
    }

    public int getBase() {
        return base;
    }

    public int getSize() {
        return size;
    }

    public boolean isUsed() {
        return used;
    }

    public boolean isActive() {
        return active;
    }

    public Job getCurrentJob() {
        return currentJob;
    }

    public String toString() {
        return "Base: " + getBase() + " Size: " + getSize()+ " Active: " + active + " CurrentJob: " + (getCurrentJob() != null);
    }
}
