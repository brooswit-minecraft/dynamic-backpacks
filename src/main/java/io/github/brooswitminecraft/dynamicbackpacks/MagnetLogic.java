package io.github.brooswitminecraft.dynamicbackpacks;

import java.util.List;

/**
 * Pure item-attraction math, free of Minecraft types so it can be unit
 * tested. Gold/Netherite attraction is a finite pull-force budget divided
 * across nearby items by distance, not a fixed force applied to each item
 * independently.
 */
public final class MagnetLogic {
    private MagnetLogic() {
    }

    /**
     * How much of the base pull strength is available given how full the
     * main inventory is. A full inventory has effectively zero pull; an
     * empty one gets the full base strength.
     */
    public static double availablePull(double basePull, int occupiedSlots, int mainSlots) {
        if (mainSlots <= 0) {
            return 0.0;
        }
        double fullness = Math.min(1.0, Math.max(0.0, (double) occupiedSlots / mainSlots));
        return basePull * (1.0 - fullness);
    }

    /**
     * Splits the available pull across every eligible item by inverse
     * distance: closer items get a stronger share, farther ones a weaker
     * share, and a crowd of items each gets only a sliver. Returns the
     * acceleration magnitude for each distance, same order as the input.
     */
    public static double[] distributePull(double availablePull, List<Double> distances) {
        double[] shares = new double[distances.size()];
        if (availablePull <= 0 || distances.isEmpty()) {
            return shares;
        }
        double weightSum = 0.0;
        double[] weights = new double[distances.size()];
        for (int i = 0; i < distances.size(); i++) {
            double distance = Math.max(0.5, distances.get(i));
            weights[i] = 1.0 / distance;
            weightSum += weights[i];
        }
        for (int i = 0; i < weights.length; i++) {
            shares[i] = availablePull * (weights[i] / weightSum);
        }
        return shares;
    }
}
