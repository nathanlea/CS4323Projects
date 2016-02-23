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
    int method = 0;

    boolean notRejected = true;
    int nextJobPID = startingPID;
    int jobsInRange = 0;
    double turnAroundTime = 0;
    double waitingTime = 0;
    double processingTime = 0;

    int[] memory = new int[180];

    Queue<Integer> readyQueue = new PriorityQueue<Integer>();
    Integer currentJob = 9001;
    Queue<Integer> rejectedJobs = new PriorityQueue<Integer>();

    public MemoryManagement ( int method ) {
        //Starts the memory management
        this.method = method;
    }

    public void start( ) {
        //Main loop
        boolean a = true;

        int nextStatOutput = 100;
        int endVTUTIME = 0;

        int[] jobAtDoor = new int[4];

        //Get first job arrival Time
        int firstJobArrives = r.nextInt(10)+1;
        int nextJobArrival = firstJobArrives;

        //Event Generated Loop

        for( int VTU = firstJobArrives; VTU < 5000; ) {
           /* if(VTU > nextStatOutput) {
                if(nextStatOutput%1000==0) {
                    System.out.print("Rejected Job Num: ");
                    System.out.println(rejectedJobs.size());
                    System.out.println();
                }
                System.out.println("Current VTU: " + VTU);
                System.out.println("Complete Jobs: " + (currentJob - 10000));
                System.out.println("Average Hole: " + averageHoleSize());
                System.out.println("Total Fragmented Bytes: " + totalFragmentBytes());
                System.out.println("Storage Utilization: " + storageUtilization());
                System.out.println();System.out.println();
                nextStatOutput+=100;
            }   */
            //Initial First Case
            if( firstJobArrives == VTU ) {
                //Generate New Job
                nextJobPID++;
                int[] job = loader(nextJobPID, VTU);

                //Place job in memory
                memoryManager(job);

                //add PID to readyQueue to get activated
                readyQueue.add(job[0]);
                nextJobPID++;
            }

            if( currentJob != 9001 ) {
                //System
                //Completion
                //Remove from memory

                int b = getDurationOfJob(currentJob);
                int c = getJobTime(currentJob);
                a = removeJobFromMemory(currentJob);
                if( a && ( VTU>=1000 && VTU<=4000 ) ) {
                    /*System.out.println("PID COMPLETE: " + currentJob + " @ " + VTU);
                    System.out.println("WAITING: " + ( VTU - c ));
                    System.out.println("Processing Time: " + b);   */
                    waitingTime += VTU - c;
                    processingTime += b;
                    turnAroundTime += ( VTU - c ) + b;
                    jobsInRange++;
                }
            }

            if(!a && readyQueue.isEmpty()) {
                //We are in idle so lets hurry this along...
                VTU = nextJobArrival;
            }

            while( nextJobArrival <= VTU && memoryManager(jobAtDoor) ) {

                if( jobAtDoor[0] != 0 && notRejected ) {
                    readyQueue.add(jobAtDoor[0]);
                    nextJobPID++;
                }
                jobAtDoor = loader(nextJobPID, VTU);
                nextJobArrival += r.nextInt(10)+1;
            }

            if( readyQueue.size() != 0 ) {
                //Start job
                //Dispatcher
                currentJob = readyQueue.poll();
                VTU += getDurationOfJob(currentJob);
                //System.out.println(VTU);
            }
            endVTUTIME = VTU;
        }
        //Finished
        //Output data
        //memoryDump();
        waitingTime    = waitingTime    / (jobsInRange);
        turnAroundTime = turnAroundTime / (jobsInRange);
        processingTime = processingTime / (jobsInRange);
        System.out.print("COMPLETED," + (currentJob - 10000));
        System.out.print(",WAITING," + ( nextJobPID - currentJob ));
        System.out.print(",REJECTED," + rejectedJobs.size());
        System.out.print(",Turnaround," + turnAroundTime);
        System.out.print(",Waiting," + waitingTime);
        System.out.print(",Processing," + processingTime);
        System.out.print(",Ending Time," + endVTUTIME);
        System.out.println();
    }

    private int getDurationOfJob(int PID){
        //Linear Search
        //System.out.println("PID: " + PID);
        for( int i = 0; i < memory.length; i++) {
            if( memory[i] == PID ) {
                //System.out.println("Duration: " + (memory[i+2]));
                return (memory[i+2]);
            }
        }
        //EXCEPTION
        //TODO
        return -1;
    }

    private int getJobTime(int PID){
        //Linear Search
        for( int i = 0; i < memory.length; i++) {
            if( memory[i] == PID ) {
                return (memory[i+3]);
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

        if(PID == 0) return true;

        //Find a hole for the job to go
        if( method == 0 ) { //FIRST FIT
            for (int i = 0; i < memory.length - 1; i++) {
                if (memory[i] == memory[i + 1] || memory[i] == 0) {
                    //Empty job Found
                    //Hole?
                    //Negative means hole
                    if (memory[i] < 0 || memory[i] == 0) {
                        //Hole!!
                        //Will it fit!
                        int sizeOfHole = 1;
                        int j = i;
                        while (memory.length > j + 1 && (memory[j] == memory[j + 1] || memory[j] == 0)) {
                            sizeOfHole++;
                            j++;
                        }
                        if ((size / 10) <= sizeOfHole) {
                            //It will fit!
                            //Place job
                            //System.out.println("Job: " + PID + " Size: " + size + " Duration: " + duration + " genTime: " + genTime);
                            memory[i] = PID;
                            memory[i + 1] = size;// ^ PID;
                            memory[i + 2] = duration;// ^ PID;
                            memory[i + 3] = genTime;// ^ PID;
                            memory[i + 4] = PID;
                            for (int m = i + 5; m < (size / 10) + (i); m++) {
                                memory[m] = PID;
                            }
                            notRejected = true;
                            return true;
                        } else {
                            //It won't fit, find next hole
                        }
                    }
                }
            }

            //No current hole, will there ever be a hole?
            for( int i = 0; i< memory.length - 1; i++) {
                if (memory[i] == memory[i + 1] || memory[i] == 0) {
                    //Will it fit! Somewhere
                    int sizeOfHole = 1;
                    int j = i;
                    while (memory.length > j + 1 && (memory[j] == memory[j + 1])) {
                        sizeOfHole++;
                        j++;
                    }
                    if( ( size / 10 ) <= sizeOfHole ) {
                        //it will eventually fit
                        notRejected = true;
                        return false;
                    }
                }
            }
            //No possible place for it to go
            rejectedJobs.add(PID);
            notRejected = false;
            return true;

        } else if ( method == 1 ) {  //BEST FIT
            int bestSize = 999;
            int bestSizeLocation = 0;
            boolean bestLocationHole = true;
            boolean currentHole = true;

            for (int i = 0; i < memory.length - 1; i++) {
                if (memory[i] == memory[i + 1] || memory[i] == 0) {
                    //Hole!!
                    //Will it fit!
                    int sizeOfHole = 1;
                    int j = i;
                    while (memory.length > j + 1 && (memory[j] == memory[j + 1] || memory[j] == 0)) {
                        sizeOfHole++;
                        j++;
                    }
                    // We Found a hole with a size that could fit the job
                    // Is it the best sized hole that we have found

                    if( memory[i] > 0 ) {
                        sizeOfHole+=4;
                        currentHole = false;
                    } else { currentHole = true; }

                    if ((size / 10) <= sizeOfHole) {
                        int extraRoom = sizeOfHole - (size / 10);
                        int bestSoFarExtraRoom = bestSize - (size / 10);

                        if (extraRoom < bestSoFarExtraRoom) {
                            bestSize = sizeOfHole;
                            bestSizeLocation = i;
                            bestLocationHole = currentHole;
                            //System.out.println("Size: "+ bestSize + " Location: " + bestSizeLocation + " Hole: " + bestLocationHole);
                        }
                    }
                    i=j;
                }
            }
            if( bestLocationHole && bestSize != 999 ) {
                //Place job
                memory[bestSizeLocation] = PID;
                memory[bestSizeLocation + 1] = size;// ^ PID;
                memory[bestSizeLocation + 2] = duration;// ^ PID;
                memory[bestSizeLocation + 3] = genTime;// ^ PID;
                memory[bestSizeLocation + 4] = PID;
                for (int m = bestSizeLocation + 5; m < (size / 10) + (bestSizeLocation); m++) {
                    memory[m] = PID;
                }
                notRejected = true;
                return true;
            } else if ( !bestLocationHole ) {
                //The best possible hole is not yet free
                //Wait until free
                notRejected = true;
                return false;
            } else {
            //Not ever going to fit
            //Reject the job
            rejectedJobs.add(PID);
            notRejected = false;
            return true;
            }
        } else if ( method == 2 ) { //Worst Fit
            int worstSize = 0;
            int worstSizeLocation = 0;
            boolean worstLocationHole = true;
            boolean currentHole = true;

            for (int i = 0; i < memory.length - 1; i++) {
                if (memory[i] == memory[i + 1] || memory[i] == 0) {
                    //Hole!!
                    //Will it fit!
                    int sizeOfHole = 1;
                    int j = i;
                    while (memory.length > j + 1 && (memory[j] == memory[j + 1] || memory[j] == 0)) {
                        sizeOfHole++;
                        j++;
                    }
                    // We Found a hole with a size that could fit the job
                    // Is it the best sized hole that we have found

                    if( memory[i] > 0 ) {
                        sizeOfHole+=4;
                        currentHole = false;
                    } else { currentHole = true; }

                    if ((size / 10) <= sizeOfHole) {
                        int extraRoom = sizeOfHole - (size / 10);
                        int worstSoFarExtraRoom = worstSize - (size / 10);

                        if (extraRoom > worstSoFarExtraRoom) {
                            worstSize = sizeOfHole;
                            worstSizeLocation = i;
                            worstLocationHole = currentHole;
                            //System.out.println("Size: "+ bestSize + " Location: " + bestSizeLocation + " Hole: " + bestLocationHole);
                        }
                    }
                    i=j;
                }
            }
            if( worstLocationHole && worstSize != 0 ) {
                //Place job
                memory[worstSizeLocation] = PID;
                memory[worstSizeLocation + 1] = size;// ^ PID;
                memory[worstSizeLocation + 2] = duration;// ^ PID;
                memory[worstSizeLocation + 3] = genTime;// ^ PID;
                memory[worstSizeLocation + 4] = PID;
                for (int m = worstSizeLocation + 5; m < (size / 10) + (worstSizeLocation); m++) {
                    memory[m] = PID;
                }
                notRejected = true;
                return true;
            } else if ( !worstLocationHole ) {
                //The best possible hole is not yet free
                //Wait until free
                notRejected = true;
                return false;
            } else {
                //Not ever going to fit
                //Reject the job
                rejectedJobs.add(PID);
                notRejected = false;
                return true;
            }
        }

        //It should never ever get here, but if it does
        //Reject the job
        rejectedJobs.add(PID);
        notRejected = false;
        return true;
    }

    private boolean removeJobFromMemory(Integer currentJob) {
        for(int i = 0; i < memory.length ; i++) {
            if(memory[i] ==  currentJob) {
                int size = memory[i+1];
                for( int j = i; j < ( size / 10) + i; j++ ) {
                    memory[j] = -1 * currentJob;
                }
                return true;
            }
        }
        return false;
    }

    public int[] loader(int nextJobPID, int VTU) {
        int size = (r.nextInt(26)+5)*10;
        int duration = ((r.nextInt(56))+5);
        duration = 5*(Math.round(duration/5));
        return new int[] {nextJobPID, size, duration, VTU};
    }

    private float averageHoleSize() {
        int total = 0;
        int count = 0;
        for(int i = 0; i < memory.length - 1; i++) {
            if( memory[i] == memory[i+1] || memory[i] == 0 ) {
                if (memory[i] < 0 || memory[i] == 0) {
                    int sizeOfHole = 1;
                    int j = i;
                    while (memory.length > j + 1 && (memory[j] == memory[j + 1] || memory[j] == 0)) {
                        sizeOfHole++;
                        j++;
                    }
                    i=j;
                    total+=sizeOfHole;
                    count++;
                }
            }
        }
        total*=10;
        return (float) ((total*1.0)/(count*1.0));
    }

    private float totalFragmentBytes() {
        int total = 0;
        for(int i = 0; i < memory.length - 1; i++) {
            if( memory[i] == memory[i+1] || memory[i] == 0 ) {
                if (memory[i] < 0 || memory[i] == 0) {
                    int sizeOfHole = 1;
                    int j = i;
                    while (memory.length > j + 1 && (memory[j] == memory[j + 1] || memory[j] == 0)) {
                        sizeOfHole++;
                        j++;
                    }
                    if(sizeOfHole < 5)
                        total+=sizeOfHole;
                    i=j;
                }
            }
        }
        return total*10;
    }

    private float storageUtilization() {
        float total = 0;
        for(int i = 0; i < memory.length; i++) {
            if(memory[i] > 0) {
                total++;
            }
        }
        return (float) ((total/180.0) * 100.0);
    }
}
