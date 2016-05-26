package org.mwg.core.utility;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.core.CoreConstants;

/**
 * Created by gnain on 23/05/16.
 */
public class DataHasherTest {


    @Test
    public void dataHash_1Test() {
        long hash = DataHasher.hash(CoreConstants.END_OF_TIME + "");
        System.out.println("dataHash_1Test: " + hash);
        Assert.assertTrue(hash == 1749261374604296L);
    }

    @Test
    public void dataHash_2Test() {
        long hash = DataHasher.hash(CoreConstants.BEGINNING_OF_TIME + "");
        System.out.println("dataHash_2Test: " + hash);
        Assert.assertTrue(hash == -7914587012082605L);
    }

    @Test
    public void dataHash_3Test() {
        long hash = DataHasher.hash(CoreConstants.BEGINNING_OF_TIME + "");
        long hash2 = DataHasher.hash(CoreConstants.END_OF_TIME + "");
        //System.out.println("dataHash_3Test: " + hash + " -> " + hash2);
        Assert.assertTrue(hash != hash2);
    }

}
