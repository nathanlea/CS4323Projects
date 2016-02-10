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
        this.used = false;
        this.active = false;
    }

    public void setJob( Job job ){
        this.currentJob = job;
        this.used = true;
        this.active = false;
    }

    public void setActive( ) {
        this.used = true;
        this.active = true;
    }

    public void setCompleted() {
        used = false;
        active = false;
        currentJob = null;
    }

    /*
    @Return This method return the left over space that is to claimed by an unused memory block
     */
    public int reActivate( int size, Job newJob ) {
        int ret = this.size;
        this.size = size;
        used = true;
        active = false;
        currentJob = newJob;
        //System.out.println("RET: " + ret + "size: " + size + "Left Over: " + (ret-size));
        return (ret - size);
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
        if(getCurrentJob() != null)
            return "Base: " + getBase() + " Size: " + getSize()+ " Used: " + used + " Active: " + active + " CurrentJobSize: " + getCurrentJob().getSize();
        else
            return "Base: " + getBase() + " Size: " + getSize()+ " Used: " + used + " Active: " + active + " CurrentJobSize: " + 0;
    }
}
