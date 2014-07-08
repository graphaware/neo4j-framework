package com.graphaware.generator;

import com.graphaware.generator.utils.WeightedReservoirSampler;
import junit.framework.TestCase;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;

public class WeightedReservoirSamplerTest extends TestCase {

    /**
     * Tests the weighted reservoir sampling
     * @throws Exception
     */
    @Test
    public void testRandomChoice() throws Exception {
        WeightedReservoirSampler sampler = new WeightedReservoirSampler();
        List<Integer> weights = new ArrayList<>(Arrays.asList(5,10,15,20));
        List<Integer> omit    = new ArrayList<>(Arrays.asList(0,2));

        for (int i = 0; i < 100; ++i)
            System.out.println("random choice: " + sampler.randomIndexChoice(weights, 2));

    }
}
