package com.nathanlea;
/*****************************************************
 * Nathan Lea
 * CS4323
 * Simulation Project, Phase 2
 * Sarath Kumar Maddinani
 * 
 * This is the Main function for the Memory Simulation
 * The creates a new MemorySimulation object and starts
 * the simulation
 ****************************************************/
public class Main {

    public static void main(String[] args) {


        /**
         * method
         * 0 - First Fit
         * 1 - Best Fit - Current Best Hole
         * 2 - Worst Fit - Current Worst Hole
         * 3 - Best Best Fit - Best Hole Ever
         * 4 - Worst Worst Fit - Worst Hole Ever
         */
        /**
         * Compaction Scenario
         * 0 - Every 250
         * 1 - Every 500
         * 2 - Every Memory Denial
         */
       System.out.println("====================================== First Fit ===============================");
       new MemorySimulation(0, 0).simulate();
       System.out.println("====================================== First Fit ===============================");
       new MemorySimulation(0, 1).simulate();
       System.out.println("====================================== First Fit ===============================");
       new MemorySimulation(0, 2).simulate();
       System.out.println("\n================================= Best Fit =====================================");
       new MemorySimulation(1, 0).simulate();
       System.out.println("\n================================= Best Fit =====================================");
       new MemorySimulation(1, 1).simulate();
       System.out.println("\n================================= Best Fit =====================================");
       new MemorySimulation(1, 2).simulate();
       System.out.println("\n================================= Worst Fit ====================================");
       new MemorySimulation(2, 0).simulate();
       System.out.println("\n================================= Worst Fit ====================================");
       new MemorySimulation(2, 1).simulate();
       System.out.println("\n================================= Worst Fit ====================================");
       new MemorySimulation(2, 2).simulate();
       System.out.println("\n================================= Best Best Fit ================================");
       new MemorySimulation(3, 0).simulate();
       System.out.println("\n================================= Best Best Fit ================================");
       new MemorySimulation(3, 1).simulate();
       System.out.println("\n================================= Best Best Fit ================================");
       new MemorySimulation(3, 2).simulate();
       System.out.println("\n================================= Worst Worst Fit ==============================");
       new MemorySimulation(4, 0).simulate();
       System.out.println("\n================================= Worst Worst Fit ==============================");
       new MemorySimulation(4, 1).simulate();
       System.out.println("\n================================= Worst Worst Fit ==============================");
       new MemorySimulation(4, 2).simulate();
    }
}
