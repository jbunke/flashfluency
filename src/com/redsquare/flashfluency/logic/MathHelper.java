package com.redsquare.flashfluency.logic;

import java.util.List;
import java.util.Random;

public class MathHelper {
    private static final Random r = new Random();

    public static <T> int randomInsertionIndex(final List<T> list) {
        final int exclusiveMaxBound = list.size() + 1;

        return boundedRandom(exclusiveMaxBound);
    }

    private static int boundedRandom(final int max) {
        return (int)(r.nextDouble() * max);
    }
}
