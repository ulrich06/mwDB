package math;

import org.mwg.core.utility.PrimitiveHelper;

import java.util.Random;
import java.util.TreeMap;

/**
 * Created by assaad on 23/03/16.
 */
public class HashCollision {
    public void hashing() {
        byte x;
        long p1, p2, p3;

        TreeMap<Long, Integer> hashTree = new TreeMap<Long, Integer>();
        TreeMap<Long, Integer> randomTree = new TreeMap<Long, Integer>();

        Random rand = new Random();
        long max = 1000000000;

        long trials = 1000000;

        for (long i = 0; i < trials; i++) {
            long yrand = rand.nextLong() % max;

            x = (byte) rand.nextInt(4);
            p1 = rand.nextLong();
            p2 = rand.nextLong();
            p3 = rand.nextLong();

            long uhash = PrimitiveHelper.tripleHash(x, p1, p2, p3, max);


            if (randomTree.containsKey(yrand)) {
                randomTree.put(yrand, randomTree.get(yrand) + 1);
            } else {
                randomTree.put(yrand, 1);
            }

            if (hashTree.containsKey(uhash)) {
                hashTree.put(uhash, hashTree.get(uhash) + 1);
            } else {
                hashTree.put(uhash, 1);
            }
        }

        double randcoll = trials - randomTree.keySet().size();
        randcoll = randcoll * 100 / trials;
        System.out.println("Random size: " + randomTree.keySet().size() + " collisions: " + randcoll + " %");

        double hashcoll = trials - hashTree.keySet().size();
        hashcoll = hashcoll * 100 / trials;
        System.out.println("Hash size: " + hashTree.keySet().size() + " collisions: " + hashcoll + " %");


    }
}
