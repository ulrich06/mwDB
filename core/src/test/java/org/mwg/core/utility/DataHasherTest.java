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
        //System.out.println("dataHash_1Test: " + hash);
        Assert.assertTrue(hash == 5045780844029558696L);
    }

    @Test
    public void dataHash_2Test() {
        long hash = DataHasher.hash(CoreConstants.BEGINNING_OF_TIME + "");
        //System.out.println("dataHash_2Test: " + hash);
        Assert.assertTrue(hash == -6421040456387667485L);
    }

    @Test
    public void dataHash_3Test() {
        long hash = DataHasher.hash(CoreConstants.BEGINNING_OF_TIME + "");
        long hash2 = DataHasher.hash(CoreConstants.END_OF_TIME + "");
        //System.out.println("dataHash_3Test: " + hash + " -> " + hash2);
        Assert.assertTrue(hash != hash2);
    }

}
