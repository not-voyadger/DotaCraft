package com.dotaCraft.Utils;

public class DotaUnits {
    public static final double MULTIPLIER = 0.015;

    // Convert dota units to blocks.
    public static double toBlocks(double dotaUnits) {
        return dotaUnits * MULTIPLIER;
    }

    // Convert units massives to blocks
    public static double[] toBlocks(double[] dotaUnitsArray) {
        if (dotaUnitsArray == null) return new double[0];
        double[] blocks = new double[dotaUnitsArray.length];
        for (int i = 0; i < dotaUnitsArray.length; i++) {
            blocks[i] = dotaUnitsArray[i] * MULTIPLIER;
        }
        return blocks;
    }
}