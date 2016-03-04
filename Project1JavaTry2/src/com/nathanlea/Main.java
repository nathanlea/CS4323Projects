package com.nathanlea;

public class Main {

    public static void main(String[] args) {
        /**
         * method [
         * 0 - First Fit
         * 1 - Best Fit - Current Best Hole
         * 2 - Worst Fit - Current Worst Hole
         * 3 - Best Best Fit - Best Hole Ever
         * 4 - Worst Worst Fit - Worst Hole Ever ]
         */
        //for( int o = 0; o < 1000; o++) {
            new MemorySimulation(0).start();
        //}
    }
}
