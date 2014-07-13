package com.graphaware.generator.utils;

import java.util.*;

/**
 * Chooses an index at random, omiting certain indices
 */
public class RandomIndexChoice {

    private final Random random = new Random();

    /**
     * Random index choice with indices omitted.
     * <p/>
     * Warning: this algorithm does not terminate if omitIndices contains
     * all indices from 0 to length-1. Use this only if number
     * of entries in omitIndices is much less than length.
     *
     * @param length      range to pick indices from
     * @param omitIndices indices to be omited from the selection
     * @return index from the range specified
     */
    public int randomIndexChoice(int length, Set<Integer> omitIndices) {
        while (true) {
            int choice = random.nextInt(length);
            if (!omitIndices.contains(choice)) {
                return choice;
            }
        }
    }


    /**
     * Random index choice with indices omitted (long)
     *
     * @param length      range to pick indices from
     * @param omitIndices indices to be omited from the selection
     * @return index from the range specified
     */
    public long randomIndexChoice(long length, PriorityQueue<Long> omitIndices) {
        int omitLength = omitIndices.size();
        //todo this will not fly - try using BigInteger
        long choice = (long) Math.floor((length - omitLength) * random.nextDouble());

        int offset = 0;
        Iterator<Long> it = omitIndices.iterator();

        while (it.hasNext() && choice + offset >= it.next())
            offset++;

        return choice + offset;

    }

    /**
     * Random index choice with an index omitted (int)
     *
     * @param length range to pick indices from
     * @param omit   index to be omited from the selection
     * @return index from the range specified
     */
    public int randomIndexChoice(int length, int omit) {
        return new Long(randomIndexChoice(new Integer(length).longValue(), new PriorityQueue<>(Collections.<Long>singleton(new Integer(omit).longValue())))).intValue();
    }

    /**
     * Random index choice (int)
     *
     * @param length range to pick indices from
     * @return index from the range specified
     */
    public int randomIndexChoice(int length) {
        return random.nextInt(length);
    }
}
