package com.graphaware.generator.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.min;

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
    public SimpleDegreeDistribution duplicate() {
        return new SimpleDegreeDistribution(degrees);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sort(Comparator<Integer> comparator) {
        Collections.sort(degrees, comparator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return passesErdosGallaiTest(); //can be swapped for Havel-Hakimi (todo should we do both?)
    }

    /**
     * All valid distributions must be graphical. This is tested using Erdos-Gallai condition on degree distribution
     * graphicality. (see Blitzstein-Diaconis paper)
     *
     * @return true iff passes.
     */
    protected final boolean passesErdosGallaiTest() {
        // Do this in-place instead?
        DegreeDistribution copy = duplicate();

        int L = copy.size();
        int degreeSum = 0;           // Has to be even by the handshaking lemma

        for (int degree : copy.getDegrees()) {
            if (degree < 0) {
                return false;
            }
            degreeSum += degree;
        }

        if (degreeSum % 2 != 0) {
            return false;
        }

        copy.sort(Collections.<Integer>reverseOrder());
        // Erdos-Gallai test
        for (int k = 1; k < L; ++k) {
            int sum = 0;
            for (int i = 0; i < k; ++i) {
                sum += copy.get(i);
            }

            int comp = 0;
            for (int j = k; j < L; ++j) {
                comp += min(k, copy.get(j));
            }

            if (sum > k * (k - 1) + comp) {
                return false;
            }
        }

        return true;
    }

    /**
     * Havel-Hakimi is a recursive alternative to the Erdos-Gallai condition
     *
     * @return true iff passes.
     */
    protected final boolean passesHavelHakimiTest() {
        /*
         * The test fails if there are less available
         * nodes to connect to than the degree of lar-
         * gest node.
         */

        DegreeDistribution copy = duplicate();

        int i = 0;
        int first;
        int L = this.size();

        while (L > 0) {
            first = copy.get(i);
            L--;

            int j = 1;
            for (int k = 0; k < first; ++k) {
                while (copy.get(j) == 0) {

                    j++;
                    if (j > L) {
                        return false;
                    }
                }

                copy.set(j, copy.get(j) - 1);
            }

            copy.set(i, 0);
            copy.sort(Collections.<Integer>reverseOrder());
        }
        return true;
    }
}
