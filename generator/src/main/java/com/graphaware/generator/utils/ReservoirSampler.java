package com.graphaware.generator.utils;

import java.util.PriorityQueue;
import java.util.Random;
import java.util.List;

import static java.util.Collections.sort;

/**
 * Implementation note:
 *       There is a ReservoirSampler Class already present in common.utils, though I am not sure
 *       if it is the same thing as this one. The reservoir sampler programmed here simply
 *       picks an item from collection at random.
 *
 *       I was also thinking hard about relating this one to WeightedReservoirSampler. The issue is
 *       that whereas this sampler can be generic (chooses an item at random), the WeightedReservoir
 *       Sampler requires a list of weights to sample from in addition. There may be some nice Java
 *       minded way of composing this into a structure, but I am not sure of any at this stage.
 *
 * TODO: Merge with com.graphaware.common.util such that this is usefull in the whole scope of the framework
 */
public class ReservoirSampler<T> {
    private final Random random;

    public ReservoirSampler() {
        random = new Random();
    }

    /**
     * Randomly chooses an item from the list
     * @param items list to sample from
     * @return item chosen at random
     */
    public T randomChoice(List<T> items) {
        double maxRnd = 0.0;
        T choice = null; // though formally null, non-null guaranteed, provided items are not empty
        for (T item : items) {
            if (random.nextDouble() > maxRnd)
                choice = item;
        }

        return choice;
    }

    /**
     * Randomly chooses an index and returns it.
     * Good for lists without random access
     * @param items list to sample from
     * @return randomly chosen index
     */
    public int randomIndexChoice(List<T> items)
    {
        return randomIndexChoice(items, null);
    }

    /**
     * Randomly chooses an index and returns it, omiting the chosen indices.
     * @param items list to sample from
     * @return randomly chosen index, indices omited
     */
    public int randomIndexChoice(List<T> items, List<Integer> omitIndices) {
        double maxRnd = 0.0;
        double rnd;
        int index = 0; // though formally null, non-null guaranteed, provided items are not empty

        sort(omitIndices); // sort the omit indices - this could be further optimised if omitIndices are guaranteed sorted
        int j = 0;
        int omitLength = omitIndices.size();

        for (int i = 0; i < items.size(); ++i) {

            if (omitIndices != null && j < omitLength &&
                omitIndices.get(j).equals(i)) { // if an index is present in omit list, skip it.
                j ++ ;
                continue;
            }

            rnd = random.nextDouble();
            if (rnd > maxRnd) {
                maxRnd = rnd;
                index = i;
            }
        }

        return index;
    }

    /**
     * Randomly chooses an index and returns it, omiting the chosen indices.
     *
     * TODO: make a variant which guarantees sorted input -> no need to call sort explicitly.
     * @param length length of the index sequence
     * @return randomly chosen index, indices omited
     */
    public int randomIndexChoice(int length, List<Integer> omitIndices) {
        double maxRnd = 0.0;
        double rnd;
        int index = 0; // though formally null, non-null guaranteed, provided items are not empty

        for (int i = 0; i < length; ++i) {
            if (omitIndices != null && omitIndices.contains(i))
                continue;

            rnd = random.nextDouble();
            if (rnd > maxRnd) {
                maxRnd = rnd;
                index = i;
            }
        }

        return index;
    }

    /**
     * Priority queue variant of the above
     * @param length
     * @param omitIndices
     * @return
     */
    public int randomIndexChoice(int length, PriorityQueue<Integer> omitIndices) {
        double maxRnd = 0.0;
        double rnd;
        int index = 0; // though formally null, non-null guaranteed, provided items are not empty


        for (int i = 0; i < length; ++i) {
            if (omitIndices != null && !omitIndices.isEmpty() &&
                    omitIndices.peek().equals(i)) { // if an index is present in omit list, skip it.
                omitIndices.poll();
                continue;
            }

            rnd = random.nextDouble();
            if (rnd > maxRnd) {
                maxRnd = rnd;
                index = i;
            }
        }

        return index;
    }

}
