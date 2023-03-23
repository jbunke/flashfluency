package com.redsquare.flashfluency.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MathHelper {
    private static final Random r = new Random();

    public static <T> T randomElementFromSet(final Set<T> set) {
        if (set.size() == 0)
            return null;

        return new ArrayList<>(set).get(boundedRandom(set.size()));
    }

    public static <T> int randomInsertionIndex(final List<T> list) {
        final int exclusiveMaxBound = list.size() + 1;

        return boundedRandom(exclusiveMaxBound);
    }

    public static int boundedRandom(final int max) {
        return (int)(r.nextDouble() * max);
    }

    public static boolean p(final double probability) {
        return r.nextDouble() < probability;
    }
}
