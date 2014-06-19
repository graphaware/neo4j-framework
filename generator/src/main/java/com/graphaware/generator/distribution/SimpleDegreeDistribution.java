package com.graphaware.generator.distribution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Simple {@link DegreeDistribution} where the distribution can be directly passed into the constructor.
 */
public class SimpleDegreeDistribution implements DegreeDistribution {

    private final List<Integer> degrees = new ArrayList<>();

    public SimpleDegreeDistribution(List<Integer> degrees) {
        this.degrees.addAll(degrees);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getDegrees() {
        return Collections.unmodifiableList(degrees);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isZeroList() {
        for (int degree : degrees) {
            if (degree > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int get(int index) {
        return degrees.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(int index, int value) {
        degrees.set(index, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void decrease(int index) {
        degrees.set(index, degrees.get(index) - 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return degrees.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DegreeDistribution duplicate() {
        return new SimpleDegreeDistribution(degrees);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sort(Comparator<Integer> comparator) {
        Collections.sort(degrees, comparator);
    }
}
