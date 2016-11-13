/*
 * Copyright (c) 2016 PayPal, Inc.
 *
 * All rights reserved.
 *
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY
 * KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A
 * PARTICULAR PURPOSE.
 */

package com.tafi.primer.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Helper class for prime number randomizations and stuff.
 *
 * @author trabinerson
 */
public class PrimeHelper {

    private static final int[] PRIMES = {2, 3, 5, 7, 11, 13, 17, 19, 23};
    private static final int MAX_POSSIBLE_TO_FACTOR = 50000;
    private static final int FIRST_LARGE_PRIME = 13;

    public static class ProblemSet {

        public final List<Integer> primes;
        public final int toFactor;

        public ProblemSet(List<Integer> primes, int toFactor) {
            this.primes = primes;
            this.toFactor = toFactor;
        }
    }

    public static ProblemSet randomizeProblemSet(int level) {
        int numPrimes = 5;

        // Set max possible prime by level
        int maxPrimeIndex = PRIMES.length - 1;
        if (level <= PRIMES.length - numPrimes + 1) {
            maxPrimeIndex = numPrimes + level - 2;
        }

        // Get ArrayList of primes - bah java
        ArrayList<Integer> primes = new ArrayList<>(maxPrimeIndex + 1);
        for (int i = 0; i <= maxPrimeIndex; i++) {
            primes.add(i, PRIMES[i]);
        }

        // Randomize primes
        Collections.shuffle(primes);
        ArrayList<Integer> chosenPrimes = new ArrayList<>(numPrimes);
        for (int k = 0; k < numPrimes; k++) {
            chosenPrimes.add(k, primes.get(k));
        }

        // Make sure either 2 or 3 are chosen
        int smallest;
        Random random = new Random();
        if (chosenPrimes.contains(2)) {
            smallest = 2;
        }
        else if (chosenPrimes.contains(3)) {
            smallest = 3;
        }
        else {
            smallest = 2;
            if (random.nextBoolean()) {
                smallest++;
            }
            chosenPrimes.add(0, smallest);
            chosenPrimes.remove(chosenPrimes.size() - 1);
        }

        // Choose primes to use for number
        int numFactors = (int)(level / 2.) + 2;
        ArrayList<Integer> participatingPrimes = new ArrayList<>(numFactors);
        int numLargeChosen = 0;
        int toFactor = 1;
        for (int k = 0; k < numFactors; k++) {
            int prime = chooseNextPrime(random, numPrimes, chosenPrimes, smallest, toFactor, level, numLargeChosen);
            if (prime >= FIRST_LARGE_PRIME) {
                numLargeChosen++;
            }
            participatingPrimes.add(k, prime);
            toFactor *= prime;
        }

        // Sort
        Collections.sort(chosenPrimes);

        return new ProblemSet(chosenPrimes, toFactor);
    }

    private static int chooseNextPrime(
            Random random, int numPrimes, ArrayList<Integer> chosenPrimes,
            int smallest, int currentProduct, int level, int numLargeChosen) {

        // Try to randomize a good prime
        int maxTrials = 500;
        int numTrials = 0;
        int prime;
        do {
            int r = random.nextInt(numPrimes);
            prime = chosenPrimes.get(r);
            if (isPrimeKosher(prime, level, numLargeChosen, currentProduct)) {
                return prime;
            }
            numTrials++;
        }
        while (numTrials < maxTrials);

        // OK, all primes suck
        return smallest;
    }

    private static boolean isPrimeKosher(int prime, int level, int numLargeChosen, int currentProduct) {
        return !isPrimeTooLarge(prime, level, numLargeChosen) && !doesPrimeExceedMax(prime, currentProduct);
    }

    private static boolean isPrimeTooLarge(int prime, int level, int numLargeChosen) {
        if (prime < FIRST_LARGE_PRIME) {
            return false;
        }
        return (numLargeChosen > 0 && level < 10) || (numLargeChosen > 1 && level < 20);
    }

    private static boolean doesPrimeExceedMax(int prime, int currentProduct) {
        return prime * currentProduct >= MAX_POSSIBLE_TO_FACTOR;
    }
}
