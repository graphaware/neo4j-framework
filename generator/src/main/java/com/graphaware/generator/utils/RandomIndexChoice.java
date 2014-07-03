package com.graphaware.generator.utils;

import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Chooses an index at random, omiting certain indices
 */
public class RandomIndexChoice {

    private final Random random;

    public RandomIndexChoice() {
        random = new Random();
    }

    /**
     * Random index choice with indices omitted.
     * @param length range to pick indices from
     * @param omitIndices indices to be omited from the selection
     * @return index from the range specified
     */
    public int randomIndexChoice(int length, PriorityQueue<Integer> omitIndices) {
        int omitLength = omitIndices.size();
        int choice = (int) Math.floor((length - omitLength) * random.nextDouble());

        int offset = 0;
        Iterator<Integer> it = omitIndices.iterator();

        while(it.hasNext() && choice + offset >= it.next())
            offset ++;

        return choice + offset;

    }

    /**
     * Random index choice with indices omitted (long)
     * @param length range to pick indices from
     * @param omitIndices indices to be omited from the selection
     * @return index from the range specified
     */
    public long randomIndexChoice(long length, PriorityQueue<Long> omitIndices) {
        int omitLength = omitIndices.size();
        long choice = (long) Math.floor((length - omitLength) * random.nextDouble());

        int offset = 0;
        Iterator<Long> it = omitIndices.iterator();

        while(it.hasNext() && choice + offset >= it.next())
            offset ++;

        return choice + offset;

    }
}
