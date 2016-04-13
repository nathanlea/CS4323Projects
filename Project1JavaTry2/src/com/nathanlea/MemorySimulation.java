package com.nathanlea;
/*****************************************************
 * Nathan Lea
 * CS4323
 * Simulation Project, Phase 2
 * Sarath Kumar Maddinani
 * 
 * MemorySimulation, the class that performs the
 * memory simulation. Changes the memory placement
 * based on the number of that is passed into the 
 * initializer.
 *
 ****************************************************/

import java.security.SecureRandom;
import java.util.*;

/**
 * <h2>The class is a memory simulation to test and compare first fit/best fit/worst fit memory placement strategies</h2>
 *
 * <i>Developed by Nathan Lea on 2/8/2016 - 3/8/2016 for CS4303 Operating Systems</i>
 */


public class MemorySimulation {
    private final int startingPID = 10000;

    /**
     * The main memory array for all of the jobs to go into
     */
    private int[] memory = new int[180];

    /**
     * Describes what the next id for the job will be,
     * used to keep track of the many jobs going through the simulation
     * this is used in the same way as a linux system where the PIDs
     * increase by ever job
     */
    private int nextJobPID = startingPID;

    /**
     * The Secure Random Object used as the random number generator for the system
     */
    private SecureRandom r = new SecureRandom();

    /**
     * method
     * 0 - First Fit
     * 1 - Best Fit - Current Best Hole
     * 2 - Worst Fit - Current Worst Hole
     * 3 - Best Best Fit - Best Hole Ever
     * 4 - Worst Worst Fit - Worst Hole Ever
     */
    private int method = 0;

    /**
     * Compaction Strategy
     * 0 - 250
     * 1 - 500
     * 2 - Memory Request Denial
     */
    private int compactionStrategy = 0;

    /**
     * used to in the memory placement to tell whether the placement rejected the job
     */
    private boolean addtoMemory = true;

    /**
     *  Count of the number of jobs completed between 1000 and 4000
     */
    private int jobsInRange = 0;

    /**
     * Track data about completed jobs
     */
    private double turnAroundTime = 0, waitingTime = 0, processingTime = 0;

    /**
     * Counter to track the number of VTU's that the CPU has nothing to do
     */
    private int idleTime = 0;

    /**
     * Ready Queue to track jobs that have been inserted into memory and are waiting to be completed by the CPU
     */
    private Queue<Integer> readyQueue = new LinkedList<Integer>();

    /**
     * The current PID of the job that the simulation is working on
     */
    private int currentJob = 9001;

    /**
     * The queue of the rejected jobs that were to big to fit into memory
     */
    private LinkedList<int[]> pendingList = new LinkedList<int[]>();

    /**
     * @param method {@link MemorySimulation#method}
     */
    public MemorySimulation(int method, int compactionStrategy ) {
        //Starts the memory management
        this.method = method;
        this.compactionStrategy = compactionStrategy;

        System.out.println("VTU |Total Fragmented KB|Storage Utilization\t|Average Hole Size|Rejected Jobs");
    }

    /**
     * Starts the simulation of Memory
     */
    public void simulate( ) {
        boolean successfullyRemovedFromMemory = true;

        int nextStatOutput = 1000;
        int[] jobAtDoor = new int[4];

        int firstJobArrives = getNextJobArrival(), nextJobArrival = firstJobArrives; //Get first job arrival Time

        /**
         * Event driven Memory Simulation Loop
         */
        for( int VTU = firstJobArrives; VTU < 5000;) {

            /***************************************************
             * Handle periodic Outputs
             *
             * NOTE: Since this is an event driven simulation
             * this output will never be at 100's exactly
             **************************************************/
            if(VTU > nextStatOutput) {
                outputAt100(nextStatOutput);
                if(nextStatOutput%1000==0) outputAt1000();
                System.out.println();
                if(nextStatOutput==4000) {
                    /*************************************
                     * Finished the Area of Concern
                     * Output some information
                     ************************************/
                    System.out.println("_____________________________________________________________________________");
                    System.out.println();
                    endingOutput();
                    nextStatOutput = 9999; //Stop outputting
                }
                nextStatOutput+=100;
            }

            if(compactionStrategy == 0 && VTU % 250 == 0 ) {
                compactMemory();
            } else if(compactionStrategy == 1 && VTU % 500 == 0 ) {
                compactMemory();
            }

            /*********************
             * Initial First Case
             ********************/
            if( firstJobArrives == VTU ) {
                nextJobPID++;
                jobAtDoor = initializer(nextJobPID, VTU); //Generate New Job
                currentJob = jobAtDoor[0];
            }

            /***********************************
             * There is no next job, move VTU to
             * next event of loading a new job
             **********************************/
            if(!successfullyRemovedFromMemory && readyQueue.isEmpty()) {
                idleTime += (nextJobArrival - VTU);
                VTU = nextJobArrival;
            }

            /**********************************
             * Loader
             * Give preference to memory insertion
             * to the pending list
             * If there is still room check to see
             * if you can insert the new job
             * If you can't it will get rejected
             * in the memory manager
             *********************************/
            int pendingListJob = 0, index = 0;
            while( pendingList.size() > 0 && pendingListJob < pendingList.size() && index < pendingList.size() ) {
                int[] tempJob = pendingList.get(pendingListJob);
                pendingList.remove(pendingListJob);
                if(memoryManager(tempJob, true) && !addtoMemory && pendingList.size() != 1) {
                } else {
                    pendingListJob++;
                }
                if(addtoMemory) {
                    readyQueue.add(tempJob[0]);
                }
                index++;
            }
            while( pendingList.size() < 100 && nextJobArrival <= VTU && memoryManager(jobAtDoor, true) ) {
                if( jobAtDoor[0] != 0 && addtoMemory) {
                    readyQueue.add(jobAtDoor[0]);
                }
                nextJobPID++;
                jobAtDoor = initializer(nextJobPID, VTU);
                nextJobArrival += getNextJobArrival();
            }

            /***********************
             * CPU
             **********************/
            if( currentJob != 9001 ) {
                int remainingJobDuration = getRemainingDurationOfJob(currentJob);
                if( remainingJobDuration > 5 ) {
                    VTU += 5;
                    decreaseRemainingDurationOfJob(currentJob);
                    if(readyQueue.size() > 1) {
                        int tempJob = readyQueue.poll();
                        readyQueue.add(tempJob);
                        currentJob = readyQueue.peek();
                    }
                }
                /*******************************
                 * Dispatcher
                 ******************************/
                else {
                    VTU += 5;
                    //Kick job out
                    readyQueue.poll(); //Remove job from list
                    successfullyRemovedFromMemory = CPU(VTU);
                    if(readyQueue.size()>0)
                        currentJob = readyQueue.peek(); //Get next job from top of list
                    else {
                        VTU=nextJobArrival; //If no next job move VTU to next job arrival
                        idleTime += (nextJobArrival - VTU);
                        currentJob++;
                    }
                }
            }
        }
        /*************************************
         * Final print of rejected jobs
         ************************************/
        System.out.println("Rejected Jobs @ 5000: " + pendingList.size());
    }

    /**
     * Used to get a random number to add to the VTU,
     * to get the next job arrival
     *
     * @return Random number 0-10
     * @see Random
     */
    private int getNextJobArrival( ) {
        return r.nextInt(10)+1;
    }

    /**
     * Method to simulation a CPU completed work on a task and calling
     * the {@link MemorySimulation#memoryManager(int[], boolean)} to remove
     * the job from {@link MemorySimulation#memory}
     *
     * @param VTU Current Virtual Time Unit of the simulation
     * @return Success of removing the current job from memory
     */
    private boolean CPU(int VTU) {
        int durationOfJob = getDurationOfJob(currentJob);  //Get the duration of the job that is being removed
        int jobTime = getJobTime(currentJob);              //Get the time that the job was inserted in memory

        /************************************************************
         * If successfullyRemovedFromMemory is
         * true the job was found in memory and removed
         *
         * If not there was an error but this should
         * never happen merely protection to not through an error
         * like in the event of the CPU is stalling
         * or waiting on a new job to come
         ************************************************************/

        boolean successfullyRemovedFromMemory = memoryManager(new int[]{currentJob,0,0,0}, false); //Remove from memory

        /************************************************************
         * If we are in the simulation period get some data from the removed job
         ***********************************************************/

        if( successfullyRemovedFromMemory && ( VTU>=1000 && VTU<=4000 ) ) {
            waitingTime += VTU - jobTime;
            processingTime += durationOfJob;
            turnAroundTime += ( VTU - jobTime ) + durationOfJob;
            jobsInRange++;
        }
        return successfullyRemovedFromMemory;
    }

    /**
     * Calls the {@link Random} to generate the size, duration of the
     * new job to the system
     *
     * @param nextJobPID the next pid to set to the generated job
     * @param VTU Current Virtual Time Unit of the simulation
     * @return An int array with the information about a job
     */
    public int[] initializer(int nextJobPID, int VTU) {
        int size = (r.nextInt(26) + 5) * 10;
        int duration = ((r.nextInt(56)) + 5);
        duration = 5 * (Math.round(duration / 5));
        return new int[]{nextJobPID, size, duration, VTU};
    }

    public void compactMemory() {
        for(int i = 0; i < memory.length; i++) {
            if(memory[i] == 0){
                for(int j=i; j < memory.length; j++){
                    if(memory[j] != 0) {
                        memory[i] = memory[j];
                        memory[j]=0;
                        break;
                    }
                }
            }
        }
    }


    /**
     * Places or removes a job into memory
     * If placing looks at method and calls the proper placement algorithm
     * If removed calls the dispatched to free memory block
     *
     * @param job The array of information about the job to be placed or removed in memory
     * @param place True is placing a job in memory, False if removing job from memory
     * @return success of the function, either placing or removing
     * @see MemorySimulation#placeFirstFit(int, int, int, int)
     * @see MemorySimulation#placeBestFit(int, int, int, int)
     * @see MemorySimulation#placeWorstFit(int, int, int, int)
     * @see MemorySimulation#departure(int)
     */
    private boolean memoryManager(int[] job, boolean place) {
        int PID      = job[0];
        int size     = job[1];
        int duration = job[2];
        int genTime  = job[3];

        if( PID == 0 ) return true; //The is a catch for the first time through the VTU loop

        /************************************************************
         * If we are placing use the appropriate memory algorithm
         ***********************************************************/
        if( place ) {
            if (method == 0) {
                return placeFirstFit(PID, size, duration, genTime);
            } else if (method == 1 || method == 3) {
                return placeBestFit(PID, size, duration, genTime);
            } else if (method == 2 || method == 4) {
                return placeWorstFit(PID, size, duration, genTime);
            } else { //Error Case, reject Job
                pendingList.add(job);
                addtoMemory = false;
                return true;
            }
        } else {
            /************************************************************
             * If we are removing the job, call the departure with the PID
             ***********************************************************/
            return departure(PID);
        }
    }

    /**
     * The memory placement algorithm that find the first hole and places
     * the job in the first hole big enough to hold the job
     *
     * @param PID The Job Identifier
     * @param size The size of the Job
     * @param duration The duration that the job takes to complete
     * @param genTime What time did the job get put in memory
     * @return True, if job was placed False, if job either was not placed.
     * The global addtoMemory tells the program if the job was reject or not
     */
    private boolean placeFirstFit(int PID, int size, int duration, int genTime) {
        for (int i = 0; i < memory.length - 1; i++) {
            if (memory[i] == memory[i + 1] && memory[i] == 0) {
                /**
                 * There is a similar piece of memory here, check if it is a hole
                 */
                if (memory[i] == 0) {
                    /**
                     * There hole here, count the size of the hole
                     */
                    int sizeOfHole = 1;
                    int j = i;
                    while (memory.length > j + 1 && (memory[j] == memory[j + 1] && memory[j] == 0)) {
                        sizeOfHole++;
                        j++;
                    }
                    if ((size / 10) <= sizeOfHole) {
                        /**
                         * The job can fit!!
                         * Place the job in memory
                         */
                        memory[i] = PID;
                        memory[i + 1] = duration;
                        memory[i + 2] = duration;
                        memory[i + 3] = genTime;
                        memory[i + 4] = PID;
                        for (int m = i + 5; m < (size / 10) + (i); m++) {
                            memory[m] = PID;
                        }
                        addtoMemory = true;
                        return true;
                    } else {
                        /**
                         * The hole isn't big enough, try next hole
                         */
                    }
                }
            }
        }

        /**
         * No current hole, check if there will ever be a hole
         */
        for( int i = 0; i< memory.length - 1; i++) {
            if (memory[i] == memory[i + 1] && memory[i] == 0) {
                /**
                 * Similar piece of memory, count the size
                 */
                int sizeOfHole = 1;
                int j = i;
                while (memory.length > j + 1 && (memory[j] == memory[j + 1])) {
                    sizeOfHole++;
                    j++;
                }
                /**
                 * If the hole is currently occupied, add the memory header to the size of the spot
                 */
                if( memory[i] > 0 ) { sizeOfHole+=4;  }
                if( ( size / 10 ) <= sizeOfHole ) {
                    /**
                     * The job will eventually fit in memory, wait until that job completes
                     */
                    addtoMemory = true;
                    return false;
                }
            }
        }
        /**
         * No possible place for it to go
         * Reject the job, and return true to get next job
         */
        pendingList.add(new int[]{PID, size, duration, genTime});
        addtoMemory = false;
        return true;
    }

    /**
     * The memory strategy that find the best current hole and places the job in that hole
     * that creates the smallest left over hole
     *
     * If method == 3 then this method is also used for best best fit, which places a job in the best
     * possible hole that will ever be. This is used to expand on and see best fit blow up to create a
     * bigger difference
     *
     * @param PID The Job Identifier
     * @param size The size of the Job
     * @param duration The duration that the job takes to complete
     * @param genTime What time did the job get put in memory
     * @return True, if job was placed False, if job either was not placed.
     * The global addtoMemory tells the program if the job was reject or not
     */
    private boolean placeBestFit(int PID, int size, int duration, int genTime) {
        int bestSize = 999;
        int bestSizeLocation = 0;
        boolean bestLocationHole = true;
        boolean currentHole;

        for (int i = 0; i < memory.length - 1; i++) {
            if (memory[i] == memory[i + 1] && memory[i] == 0) {
                /**
                 * There is a similar piece of memory here, count the size
                 */
                int sizeOfHole = 1;
                int j = i;
                while (memory.length > j + 1 && (memory[j] == memory[j + 1] && memory[j] == 0)) {
                    sizeOfHole++;
                    j++;
                }
                /**
                 * We Found a hole with a size that could fit the job
                 * Is it the best sized hole that we have found
                 *
                 * Only if it is best best to we want to look at occupied memory
                 */
                if( method == 1 && memory[i] > 0 ) { i=j; continue; } //If it is best and not current hole, ignore

                if( memory[i] > 0 ) {
                    sizeOfHole+=4;
                    currentHole = false;
                } else {
                    currentHole = true;
                }

                /**
                 * Check if it is the best hole so far, if so
                 * update the location and size of the best hole
                 */
                if ((size / 10) <= sizeOfHole) {
                    int extraRoom = sizeOfHole - (size / 10);
                    int bestSoFarExtraRoom = bestSize - (size / 10);

                    if (extraRoom < bestSoFarExtraRoom) {
                        bestSize = sizeOfHole;
                        bestSizeLocation = i;
                        bestLocationHole = currentHole;
                    }
                }
                i=j;
            }
        }
        if( bestLocationHole && bestSize != 999 ) {
            /**
             * Best hole found, place job
             */
            memory[bestSizeLocation] = PID;
            memory[bestSizeLocation + 1] = duration;
            memory[bestSizeLocation + 2] = duration;
            memory[bestSizeLocation + 3] = genTime;
            memory[bestSizeLocation + 4] = PID;
            for (int m = bestSizeLocation + 5; m < (size / 10) + (bestSizeLocation); m++) {
                memory[m] = PID;
            }
            addtoMemory = true;
            return true;
        } else if ( !bestLocationHole ) {
            /**
             * The best possible hole is not yet free
             * Wait until free
             */
            addtoMemory = true;
            return false;
        } else if ( bestSize == 999 ) {
            /**
             * A hole was not found that could fit job.
             * Check if it will ever fit
             * If not reject
             */
            for( int i = 0; i < memory.length - 1; i++) {
                if (memory[i] == memory[i + 1] && memory[i] == 0) {
                    /**
                     * Count the memory hole, and see if it is big enough
                     */
                    int sizeOfHole = 1;
                    int j = i;
                    while (memory.length > j + 1 && (memory[j] == memory[j + 1])) {
                        sizeOfHole++;
                        j++;
                    }
                    if( memory[i] > 0 ) { sizeOfHole+=4;  }
                    if( ( size / 10 ) <= sizeOfHole ) {
                        /**
                         * The job will eventually fit
                         */
                        addtoMemory = true;
                        return false;
                    }
                }
            }
            /**
             * Unable to fit into memory
             * Reject job!
             */
            pendingList.add(new int[]{PID, size, duration, genTime});
            addtoMemory = false;
            return true;
        } else {
            /**
             * No possible place for it to go
             * Reject the job, and return true to get next job
             */
            pendingList.add(new int[]{PID, size, duration, genTime});
            addtoMemory = false;
            return true;
        }
    }

    /**
     * The memory strategy that find the worst current hole and places the job in that hole
     * that creates the largest left over hole
     *
     * If method == 4 then this method is also used for worst worst fit, which places a job in the worst
     * possible hole that will ever be. This is used to expand on and see worst fit blow up to create a
     * bigger difference
     *
     * @param PID The Job Identifier
     * @param size The size of the Job
     * @param duration The duration that the job takes to complete
     * @param genTime What time did the job get put in memory
     * @return True, if job was placed False, if job either was not placed.
     * The global addtoMemory tells the program if the job was reject or not
     */
    private boolean placeWorstFit(int PID, int size, int duration, int genTime) {
        int worstSize = 0;
        int worstSizeLocation = 0;
        boolean worstLocationHole = true;
        boolean currentHole;

        for (int i = 0; i < memory.length - 1; i++) {
            if (memory[i] == memory[i + 1] && memory[i] == 0) {
                /**
                 * There is a similar piece of memory here, count the size
                 */
                int sizeOfHole = 1;
                int j = i;
                while (memory.length > j + 1 && (memory[j] == memory[j + 1] && memory[j] == 0)) {
                    sizeOfHole++;
                    j++;
                }
                /**
                 * We Found a hole with a size that could fit the job
                 * Is it the worst sized hole that we have found
                 *
                 * Only if it is worst worst to we want to look at occupied memory
                 */
                if( method == 2 && memory[i] > 0 ) { i=j; continue; }

                if( memory[i] > 0 ) {
                    sizeOfHole+=4;
                    currentHole = false;
                } else { currentHole = true; }

                /**
                 * Check if it is the worst hole so far, if so
                 * update the location and size of the worst hole
                 */
                if ((size / 10) <= sizeOfHole) {
                    int extraRoom = sizeOfHole - (size / 10);
                    int worstSoFarExtraRoom = worstSize - (size / 10);

                    if (extraRoom > worstSoFarExtraRoom) {
                        worstSize = sizeOfHole;
                        worstSizeLocation = i;
                        worstLocationHole = currentHole;
                    }
                }
                i=j;
            }
        }
        if( worstLocationHole && worstSize != 0 ) {
            /**
             * Worst hole found, place job
             */
            memory[worstSizeLocation] = PID;
            memory[worstSizeLocation + 1] = duration;
            memory[worstSizeLocation + 2] = duration;
            memory[worstSizeLocation + 3] = genTime;
            memory[worstSizeLocation + 4] = PID;

            for (int m = worstSizeLocation + 5; m < (size / 10) + (worstSizeLocation); m++) {
                memory[m] = PID;
            }
            addtoMemory = true;
            return true;
        } else if ( !worstLocationHole ) {
            /**
             * The worst possible hole is not yet free
             * Wait until free
             */
            addtoMemory = true;
            return false;
        } else if ( worstSize == 0 ) {
            /**
             * A hole was not found that could fit job.
             * Check if it will ever fit
             * If not reject
             */
            for (int i = 0; i < memory.length - 1; i++) {
                if (memory[i] == memory[i + 1] && memory[i] == 0) {
                    /**
                     * Will it fit! Somewhere
                     */
                    int sizeOfHole = 1;
                    int j = i;
                    while (memory.length > j + 1 && (memory[j] == memory[j + 1])) {
                        sizeOfHole++;
                        j++;
                    }
                    if (memory[i] > 0) {
                        sizeOfHole += 4;
                    }
                    if ((size / 10) <= sizeOfHole) {
                        /**
                         * it will eventually fit
                         */
                        addtoMemory = true;
                        return false;
                    }
                }
            }
            /**
             * No possible place for it to go
             * Reject the job, and return true to get next job
             */
            pendingList.add(new int[]{PID, size, duration, genTime});
            addtoMemory = false;
            return true;
        } else {
            /**
             * Not ever going to fit
             * Reject the job
             */
            pendingList.add(new int[]{PID, size, duration, genTime});
            addtoMemory = false;
            return true;
        }
    }

    /**
     * Frees memory block occupied with the given PID
     *
     * @param PID The PID of the job looking to be freed from memory
     * @return True if memory freeing was successful, False if job was not found in memory
     */
    private boolean departure(int PID) {
        for(int i = 0; i < memory.length ; i++) {
            if(memory[i] ==  PID) {
                memory[i] = 0;
                memory[i+1] = 0;
                memory[i+2] = 0;
                memory[i+3] = 0;
                for( int j = i+4; memory.length > j && memory[j]==PID; j++ ) {
                    memory[j] = 0;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Searches through memory and finds the memory block with
     * the given PID and return the remaining duration of the job
     *
     * @param PID PID of job to find the duration of
     * @return the duration of the job with the given PID
     */
    private void decreaseRemainingDurationOfJob(int PID){
        //Linear Search
        for( int i = 0; i < memory.length; i++) {
            if( memory[i] == PID ) {
                memory[i+1]-=5;
                return;
            }
        }
        return;
    }

    /**
     * Searches through memory and finds the memory block with
     * the given PID and return the remaining duration of the job
     *
     * @param PID PID of job to find the duration of
     * @return the duration of the job with the given PID
     */
    private int getRemainingDurationOfJob(int PID){
        //Linear Search
        for( int i = 0; i < memory.length; i++) {
            if( memory[i] == PID ) {
                return (memory[i+1]);
            }
        }
        return -1;
    }

    /**
     * Searches through memory and finds the memory block with
     * the given PID and return the duration of the job
     *
     * @param PID PID of job to find the duration of
     * @return the duration of the job with the given PID
     */
    private int getDurationOfJob(int PID){
        //Linear Search
        for( int i = 0; i < memory.length; i++) {
            if( memory[i] == PID ) {
                return (memory[i+2]);
            }
        }
        return -1;
    }

    /**
     * Searches through memory and finds the memory block with
     * the given PID and return the duration of the job
     *
     * @param PID PID of job to find the time of
     * @return the time of the job with the given PID
     */
    private int getJobTime(int PID){
        //Linear Search
        for( int i = 0; i < memory.length; i++) {
            if( memory[i] == PID ) {
                return (memory[i+3]);
            }
        }
        return -1;
    }

    /**
     * Loops through all of memory and averages the current holes
     *
     * @return the Average Size of the Holes in memory
     */
    private String averageHoleSize() {
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
        return String.format("%04.2f",(float) ((total*1.0)/(count*1.0)));
    }

    /**
     * Loops through all of memory and adds the holes less than 5 blocks
     *
     * @return The number of blocks in hole less than 5, unusable memory
     */
    private float totalFragmentBytes() {
        int total = 0;
        for(int i = 0; i < memory.length - 1; i++) {
            if( memory[i] == memory[i+1] || memory[i] == 0 ) {
                if (memory[i] < 0 || memory[i] == 0) {
                    int sizeOfHole = 1;
                    int j = i;
                    while (memory.length > j + 1 && ((memory[j] == memory[j + 1]) || memory[j] == 0)) {
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

    /**
     * Calculates the total used memory and dives that by the total memory
     *
     * @return Used memory / total Memory
     */
    private String storageUtilization() {
        float total = 0;
        for(int m : memory) {
            if(m > 0) {
                total++;
            }
        }
        return String.format("%03.2f",(float) ((total/180.0) * 100.0));
    }

    /**
     * Output data about the simulation that just finished
     * <ul>
     * <li>Completed Jobs</li>
     * <li>Waiting Jobs</li>
     * <li>Rejected Jobs</li>
     * <li>Turnaround Time</li>
     * <li>Waiting Time</li>
     * <li>Processing Time</li>
     * <li>Idle Time</li> </ul>
     */
    private void endingOutput( ) {
        waitingTime    = waitingTime    / (jobsInRange);
        turnAroundTime = turnAroundTime / (jobsInRange);
        processingTime = processingTime / (jobsInRange);
        System.out.println("Performance Measurement:");
        System.out.println("Turnaround Time\t" + String.format("%02.2f",turnAroundTime));
        System.out.println("Waiting Time\t" + String.format("%02.2f",waitingTime));
        System.out.println("Processing Time\t" + String.format("%02.2f",processingTime));
        System.out.println("Completed Jobs\t" + (currentJob - 10000));
    }

    /**
     * Outputs
     * <ul>
     * <li>VTU</li>
     * <li>Total Fragmented Bytes</li>
     * <li>Storage Utilization</li>
     * <li>Average Hole Size</li>
     * <li>Rejected Jobs</li></ul>
     *
     */
    private void outputAt100( int VTU ) {
        System.out.print(VTU + "|        " + totalFragmentBytes()+"      |\t"+storageUtilization()+"\t\t|      "+averageHoleSize()+"      |");
    }

    /**
     * Outputs the number of current Rejected jobs
     */
    private void outputAt1000( ) {
        System.out.print("    " + pendingList.size());
    }

    /**
     * Prints a memory dump of the memory array
     * Used for debug purposes
     */
    private void memoryDump( ) {
        for(int i = 0; i < 180; i++) {
            System.out.print(memory[i]+ " || ");
        }
        System.out.println();
    }
}
