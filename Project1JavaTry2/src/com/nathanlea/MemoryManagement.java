package com.nathanlea;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

/**
 * Created by Nathan on 2/8/2016.
 */



public class MemoryManagement {

    final int startingPID = 10000;
    Random r = new Random();

    public MemoryManagement ( int method ) {
        //Starts the memory management
        this.method = method;
    }

    int method = 0;

    int[] memory = new int[180];

    Queue<Integer> readyQueue = new PriorityQueue<Integer>();
    Integer currentJob = 9001;
    Queue<Integer> rejectedJobs = new PriorityQueue<Integer>();

    int nextJobPID = startingPID;

    public void start( ) {
        //Main loop

        //Init memory Block

        //Get first job arrival Time
        int firstJobArrives = r.nextInt(10)+1;
        int nextJobArrival = firstJobArrives;
        int[] jobAtDoor = new int[4];
        //System.out.println(nextJob);

        //Each loop is a VTU
        for( int VTU = firstJobArrives; VTU < 5000; ) {
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
            //Initial First Case
            if( firstJobArrives == VTU ) {
                //Generate New Job
                nextJobPID++;
                int[] job = loader(nextJobPID, VTU);
                //Place job in memory
                memoryManager(job);
                //add PID to readyQueue to get activated
                readyQueue.add(job[0]);
            }

            if( currentJob != 9001 ) {
                //Completion
                //Remove from memory
                removeJobFromMemory(currentJob);
                currentJob++;
                nextJobPID = currentJob;
            }

            while( nextJobArrival <= VTU && memoryManager(jobAtDoor) ) {

                if( jobAtDoor[0] != 0 ) {
                    readyQueue.add(jobAtDoor[0]);
                }
                nextJobPID++;
                jobAtDoor = loader(nextJobPID, VTU);
                nextJobArrival += r.nextInt(10)+1;
            }

            if( readyQueue.size() != 0 ) {
                //Start job
                //Dispatcher
                currentJob = readyQueue.poll();
                VTU += getDurationOfJob(currentJob);
            }
            System.out.println(VTU);
        }
        //Finished
        //Output data
        //memoryDump();
        //System.out.print("COMPLETED," + completedJobs.size());
        //System.out.print(",WAITING," + waitingJobs.size());
        //System.out.print(",REJECTED," + rejectedJobs.size());
        //System.out.print(",Turnaround," + turnAroundTime());
        //System.out.print(",Waiting," + waitingTime());
        //System.out.print(",Processing," + processingTime());
        System.out.println();
    }

    private int getDurationOfJob(int PID){
        //Linear Search
        for( int i = 0; i < memory.length; i++) {
            if( memory[i] == PID ) {
                return (memory[i+2] ^ PID);
            }
        }
        //EXCEPTION
        //TODO
        return -1;
    }

    private boolean memoryManager(int[] job) {
        int PID      = job[0];
        int size     = job[1];
        int duration = job[2];
        int genTime  = job[3];

        int curMemoryPID = memory[0];
        int activeJobPID = currentJob;

        if(PID == 0) return true;

        //Find a hole for the job to go
        for( int i = 0; i< memory.length; i++) {
            if( memory[i] == memory[i+1] || memory[i] == 0 ) {
                //Empty job Found
                //Hole?
                int offset = readyQueue.size() == 0 ? readyQueue.size() - 1 : readyQueue.size();
                if( memory[i] < ( activeJobPID + offset) ) {
                    //Hole!!
                    //Will it fit!
                    if( memory.length > i+(size/10)+1 && ( memory[i] == memory[i+(size/10)] || memory[i+(size/10)] == 0)) {
                        //It will fit!
                        //Place job
                        memory[i]   = PID;
                        memory[i+1] = size ^ PID;
                        memory[i+2] = duration ^ PID;
                        memory[i+3] = genTime ^ PID;
                        memory[i+4] = PID;
                        for(int m = i+5; m < ( size / 10 ) + ( i ); m++) {
                            memory[m] = PID;
                        }
                        return true;
                    } else {
                        //It won't fit, find next hole
                        curMemoryPID = memory[i];
                    }
                }
            }
        }
        //No current hole, will there ever be a hole?
        int lastHoleLocation = 5;
        for( int i = 5; i< memory.length; i++) {
            if (curMemoryPID != memory[i]) {
                if( ( size / 10 ) <= ( i - lastHoleLocation + 5 ) ) {
                    //it will eventually fit
                    return false;
                } else {
                    i+=5;
                    lastHoleLocation = i;
                }
            }
        }
        //No possible place for it to go
        rejectedJobs.add(PID);
        return true;
    }

    private void removeJobFromMemory(Integer currentJob) {
        for(int i = 0; i < memory.length; i++) {
            if(memory[i] ==  currentJob) {
                memory[i+1] = currentJob;
                memory[i+2] = currentJob;
                memory[i+3] = currentJob;
                break;
            }
        }
    }

    public int[] loader(int nextJobPID, int VTU) {
        int size = (r.nextInt(26)+5)*10;
        int duration = ((r.nextInt(56))+5);
        duration = 5*(Math.round(duration/5));
        return new int[] {nextJobPID, size, duration, VTU};
    }
    /*
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
    */

}
