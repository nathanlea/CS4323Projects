package com.nathanlea;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

/**
 * Created by Nathan on 2/8/2016.
 */
public class MemoryManagement {

    public MemoryManagement ( int method ) {
        //Starts the memory management
        this.method = method;
    }
    int method = 0;

    Queue<Job> waitingJobs = new PriorityQueue<Job>();
    ArrayList<Job> activeJobs = new ArrayList<Job>();
    ArrayList<MemoryBlock> memoryBlocks =  new ArrayList<MemoryBlock>();
    ArrayList<Job> completedJobs = new ArrayList<Job>();
    ArrayList<Job> rejectedJobs = new ArrayList<Job>();
    int nextJob = 0;
    Random r = new Random();

    public void start( ) {
        //Main loop

        //Init memory Block
        MemoryBlock initialBlock = new MemoryBlock(200, 1800);
        memoryBlocks.add(initialBlock);

        //Get first job
        nextJob = r.nextInt(10)+1;
        //System.out.println(nextJob);

        //Each loop is a VTU
        for( int VTU = 0; VTU < 5000; VTU++ ) {
            if(VTU==1000) {
                System.out.println(rejectedJobs.size());
            }
            if(VTU==2000) {
                System.out.println(rejectedJobs.size());
            }
            if(VTU==3000) {
                System.out.println(rejectedJobs.size());
            }
            if(VTU==4000) {
                System.out.println(rejectedJobs.size());
            }
            if(VTU%100==0) {
                System.out.println("Current VTU: " + VTU);
                System.out.println("Average Hole: " + averageHoleSize());
                System.out.println("Total Fragmented Bytes: " + totalFragmentBytes());
                System.out.println("Storage Utilization: " + storageUtilization());
                System.out.println();System.out.println();
            }
            if( nextJob == VTU ) {
                //Generate New Job
                waitingJobs.add(genNewJob(VTU));
                //Last thing in the new Job case, reset when the next Job comes into play
                nextJob = r.nextInt(10)+1 + VTU;
                //System.out.println(nextJob);
            }
            //Check for jobs that are now completed
            for(int a = 0; a < activeJobs.size(); a++) {
                Job tempJob = activeJobs.get(a);
                if(tempJob.getDuration() + tempJob.getCreationTime() == VTU) {
                    //Completed
                    tempJob.getMemoryBlock().setCompleted();
                    activeJobs.remove(a);
                    completedJobs.add(tempJob);
                    //If we need to remove start the pointer over as the length is now smaller by 1
                    a--;
                }
            }
            //Check the waiting Job for job that can be activated
            Job nextPossJob = waitingJobs.peek();
            //Make sure job can fit in the memory
            if( nextPossJob != null && nextPossJob.getSize() > getLargestMemoryBlockLeft()) {
                //Reject Job
                rejectedJobs.add(waitingJobs.poll());
                nextPossJob = waitingJobs.peek();
            }

            //This is where the options at which block to pick come into play
            if( nextPossJob != null && method == 0 ){
                for(int i = 0; i < memoryBlocks.size() && waitingJobs.peek() != null; i++) {
                    if( !memoryBlocks.get(i).isUsed() ) {
                        if(nextPossJob.getSize() <= memoryBlocks.get(i).getSize()) {
                            //Place job here
                            int leftOverMemory = memoryBlocks.get(i).reActivate(nextPossJob.getSize(), nextPossJob);
                            if(leftOverMemory!=0) {
                                MemoryBlock mb = new MemoryBlock(memoryBlocks.get(i).getBase() + nextPossJob.getSize(), leftOverMemory);
                                memoryBlocks.add(i + 1, mb);
                            }

                            //Pop off job
                            nextPossJob = waitingJobs.poll();
                            nextPossJob.activateJob(VTU, memoryBlocks.get(i));
                            activeJobs.add(nextPossJob);
                        }
                    }
                }

            }
            else if( method == 1 ) {

            }
            else if( method == 2) {

            }
        }
        //Finished
        //Output data
        memoryDump();
        System.out.println(waitingJobs.size());
        System.out.println(completedJobs.size());
        System.out.println(rejectedJobs.size());
    }

    public Job genNewJob(int currentVTU) {
        int size = (r.nextInt(26)+5)*10;
        int duration = (r.nextInt(56)+5);
        Job job = new Job(size, duration, currentVTU);
        job.setWaiting(currentVTU);
        return job;
    }

    public MemoryBlock[] getAvailableMemory() {
        ArrayList<MemoryBlock> availableMemory = new ArrayList<MemoryBlock>();

        for(int i = 0; i < memoryBlocks.size(); i++) {
            if( !memoryBlocks.get(i).isUsed() ) {
                availableMemory.add(memoryBlocks.get(i));
            }
        }
        return (MemoryBlock[])availableMemory.toArray();
    }

    public int getLargestMemoryBlockLeft() {
        int largestMemory = 0;
        for(int i = 0; i < memoryBlocks.size(); i++) {
            if( memoryBlocks.get(i).getSize() > largestMemory ) {
                largestMemory = memoryBlocks.get(i).getSize();
            }
        }
        return largestMemory;
    }
    private void memoryDump() {
        for(int i = 0; i < memoryBlocks.size(); i++) {
            System.out.println(i+": "+ memoryBlocks.get(i));
        }
    }
    private float averageHoleSize() {
        int total = 0;
        int count = 0;
        for(int i = 0; i < memoryBlocks.size(); i++) {
            if(!memoryBlocks.get(i).isUsed()) {
                total+=memoryBlocks.get(i).getSize();
                count++;
            }
        }
        return (float) ((total*1.0)/(count*1.0));
    }
    private float totalFragmentBytes() {
        int total = 0;
        for(int i = 0; i < memoryBlocks.size(); i++) {
            if(memoryBlocks.get(i).getSize()<50) {
                total+=memoryBlocks.get(i).getSize();
            }
        }
        return total;
    }
    private float storageUtilization() {
        float total = 0;
        for(int i = 0; i < memoryBlocks.size(); i++) {
            if(memoryBlocks.get(i).isUsed()) {
                total+=memoryBlocks.get(i).getSize();
            }
        }
        return (float) ((total/1800.0) * 100.0);
    }


}
