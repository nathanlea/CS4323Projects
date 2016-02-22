package com.nathanlea;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * Created by Nathan on 2/8/2016.
 */
public class MemoryManagement {

    public MemoryManagement ( int method ) {
        //Starts the memory management
        this.method = method;
    }
    int method = 0;

    Queue<Job> generatedJobs = new PriorityQueue<Job>();
    Queue<Job> waitingJobs = new PriorityQueue<Job>();
    Job activeJob = null;
    ArrayList<MemoryBlock> memoryBlocks =  new ArrayList<MemoryBlock>();
    ArrayList<Job> completedJobs = new ArrayList<Job>();
    ArrayList<Job> rejectedJobs = new ArrayList<Job>();
    int nextJob = 0;
    boolean holeIsFree = false; //Used for very specific case where there is only one place a item can go and the current job is holding it
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
           /* if(VTU==1000) {
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
                System.out.println("Complete Jobs: " + completedJobs.size());
                System.out.println("Average Hole: " + averageHoleSize());
                System.out.println("Total Fragmented Bytes: " + totalFragmentBytes());
                System.out.println("Storage Utilization: " + storageUtilization());
                System.out.println();System.out.println();
            } */
            if( nextJob == VTU ) {
                //Generate New Job
                generatedJobs.add(genNewJob());
                //Last thing in the new Job case, reset when the next Job comes into play
                nextJob = r.nextInt(10)+1 + VTU;
                //System.out.println(nextJob);
            }
            //Check for jobs that are now completed
            if( activeJob != null ) {
                if ((activeJob.getDuration() + activeJob.getTimeActivated() == VTU) && waitingJobs.size() != 0) {
                    //Completed
                    activeJob.completeJob();
                    completedJobs.add(activeJob);
                    activeJob = waitingJobs.poll();
                    activeJob.activateJob(VTU);

                    if( activeJob.getSize() != activeJob.memoryBlock.getSize() ) {
                        System.out.println("FAILURE");
                    }

                } else if (activeJob.getDuration() + activeJob.getTimeActivated() == VTU) {
                    //Something is waiting on our hole :/
                    activeJob.completeJob();
                    completedJobs.add(activeJob);
                    holeIsFree = true;
                }
            }
             else if( waitingJobs.size() != 0 ){
                //If there is no job add one!
                activeJob = waitingJobs.poll();
                activeJob.activateJob(VTU);
            }

            //Check the Job at the door to see if it can be places in memory
            Job nextPossJob = generatedJobs.peek();

            //Make sure job can fit in the memory
            if( nextPossJob != null && nextPossJob.getSize() > getLargestMemoryBlockLeft() ) {
                //Reject Job
                rejectedJobs.add(generatedJobs.poll());
                nextPossJob = generatedJobs.peek();

            }

            //This is where the options at which block to pick come into play
            if( nextPossJob != null && method == 0 ){
                for(int i = 0; i < memoryBlocks.size() && generatedJobs.size() != 0; i++) {
                    if( !memoryBlocks.get(i).isUsed() ) {
                        if(nextPossJob.getSize() <= memoryBlocks.get(i).getSize()) {
                            //Place job here
                            int leftOverMemory = memoryBlocks.get(i).reActivate(nextPossJob.getSize(), nextPossJob);
                            //Pop off job
                            nextPossJob = generatedJobs.poll();
                            nextPossJob.placeJob(VTU, memoryBlocks.get(i));
                            waitingJobs.add(nextPossJob);
                            //generatedJobs.poll();

                            if( leftOverMemory != 0 ) {
                                MemoryBlock mb = new MemoryBlock(memoryBlocks.get(i).getBase() + nextPossJob.getSize(), leftOverMemory);
                                memoryBlocks.add(i + 1, mb);
                            }

                            //See not about when this is used
                            if( holeIsFree ) {
                                activeJob = waitingJobs.poll();
                                activeJob.activateJob(VTU);
                                holeIsFree = false;
                            }
                            nextPossJob = generatedJobs.peek();
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
        //memoryDump();
        System.out.print("COMPLETED," + completedJobs.size());
        System.out.print(",WAITING," + waitingJobs.size());
        System.out.print(",REJECTED," + rejectedJobs.size());
        System.out.print(",Turnaround," + turnAroundTime());
        System.out.print(",Waiting," + waitingTime());
        System.out.print(",Processing," + processingTime());
        System.out.println();
    }

    public Job genNewJob() {
        int size = (r.nextInt(26)+5)*10;
        int duration = ((r.nextInt(56))+5);
        duration = 5*(Math.round(duration/5));
        Job job = new Job(size, duration);
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

    private double turnAroundTime() {
        int totalTurnAroundTime = 0;
        double avgTurnAroundTime = 0;
        for(int i = 0; i < completedJobs.size(); i++) {
            totalTurnAroundTime += completedJobs.get(i).turnAroundTime();
        }
        avgTurnAroundTime = ( totalTurnAroundTime  * 1.0 ) / completedJobs.size();
        return avgTurnAroundTime;
    }
    private double waitingTime() {
        int totalWaitingTime = 0;
        double avgWaitingTime = 0;
        for(int i = 0; i < completedJobs.size(); i++) {
            totalWaitingTime += completedJobs.get(i).waitingTime();
        }
        avgWaitingTime = ( totalWaitingTime * 1.0 ) / completedJobs.size();
        return avgWaitingTime;
    }
    private double processingTime() {
        int totalProcessingTime = 0;
        double avgProcessingTime = 0;
        for(int i = 0; i < completedJobs.size(); i++) {
            totalProcessingTime += completedJobs.get(i).getDuration();
        }
        avgProcessingTime = ( totalProcessingTime * 1.0 ) / completedJobs.size();
        return avgProcessingTime;
    }


}