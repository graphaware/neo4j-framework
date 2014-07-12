package com.graphaware.generator.utils;

import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;

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
        int choice = random.nextInt(length - omitLength);

            int offset = 0;
            Iterator<Integer> it = omitIndices.iterator();

            while (it.hasNext() && choice + offset >= it.next())
                offset++;

            return choice + offset;


    }

    /**
     * Random index choice with indices omitted.
     *
     * Warning: this algorithm does not terminate if omitIndices contains
     *          all indices from 0 to length-1. Use this only if number
     *          of entries in omitIndices is much less than length.
     *
     * @param length range to pick indices from
     * @param omitIndices indices to be omited from the selection
     * @return index from the range specified
     */
    public int randomIndexChoice(int length, HashSet<Integer> omitIndices)
    {
        int choice = 0;
        while (true) {
            choice = random.nextInt(length);
            if (!omitIndices.contains(choice))
                break;

        }
        return choice;
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


    /**
     * Random index choice with indices omitted (long)
     * @param length range to pick indices from
     * @param omit index to be omited from the selection
     * @return index from the range specified
     */
    public long randomIndexChoice(long length, long omit) {
        long choice = (long) Math.floor((length - 1) * random.nextDouble());
        return choice >= omit ? choice + 1 : choice;
    }

    /**
     * Random index choice with an index omitted (int)
     * @param length range to pick indices from
     * @param omit index to be omited from the selection
     * @return index from the range specified
     */
    public int randomIndexChoice(int length, int omit) {
        int choice = (int) Math.floor((length - 1) * random.nextDouble());
        return choice >= omit ? choice + 1 : choice;
    }

    /**
     * Random index choice (int)
     * @param length range to pick indices from
     * @return index from the range specified
     */
    public int randomIndexChoice(int length) {
        return (int) Math.floor((length - 1) * random.nextDouble());
    }

    /**
     * Random index choice (long)
     * @param length range to pick indices from
     * @return index from the range specified
     */
    public long randomIndexChoice(long length) {
        return (long) Math.floor((length - 1) * random.nextDouble());
    }
}
