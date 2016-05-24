package org.mwg.core.utility;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.core.CoreConstants;

/**
 * Created by gnain on 23/05/16.
 */
public class PrimitiveHelperTest {

    // Integer.MIN_VALUE == 0x80000000
    public static final int MIN_INT = -2147483648;

    // Integer.MAX_VALUE == 0x7FFFFFFF
    public static final int MAX_INT = 2147483647;

    /* MAX TESTS */
    @Test
    public void intHash_0Test() {
        try {
            PrimitiveHelper.intHash(1, 0);
            Assert.fail("This should have thrown an exception");
        }catch(Exception e) {
        }
    }

    @Test
    public void intHash_1Test() {
        try {
            PrimitiveHelper.intHash(1, MIN_INT);
            Assert.fail("This should have thrown an exception");
        }catch(Exception e) {
        }
    }

    @Test
    public void longHash_0Test() {
        try {
            PrimitiveHelper.longHash(1, 0);
            Assert.fail("This should have thrown an exception");
        }catch(Exception e) {
        }
    }

    @Test
    public void longHash_1Test() {
        try {
            PrimitiveHelper.longHash(1, CoreConstants.BEGINNING_OF_TIME);
            Assert.fail("This should have thrown an exception");
        }catch(Exception e) {
        }
    }

    @Test
    public void tripleHash_0Test() {
        try {
            PrimitiveHelper.tripleHash((byte)1, 2, 3, 4, 0);
            Assert.fail("This should have thrown an exception");
        }catch(Exception e) {
        }
    }

    @Test
    public void tripleHash_1Test() {
        try {
            PrimitiveHelper.tripleHash((byte)1, 2, 3, 4, CoreConstants.BEGINNING_OF_TIME);
            Assert.fail("This should have thrown an exception");
        }catch(Exception e) {
        }
    }


    /* HASH TESTS */

    @Test
    public void intHash_3Test() {
        int hash = PrimitiveHelper.intHash(MAX_INT, MAX_INT);
        //System.out.println("intHash_3Test: " + hash);
        Assert.assertTrue(hash < MAX_INT);
        Assert.assertTrue(hash == 1963394006);
    }

    @Test
    public void intHash_4Test() {
        int hash = PrimitiveHelper.intHash(MAX_INT, 10000);
        //System.out.println("intHash_4Test: " + hash);
        Assert.assertTrue(hash < 10000);
        Assert.assertTrue(hash == 4006);
    }

    @Test
    public void intHash_5Test() {
        int hash = PrimitiveHelper.intHash(-156487, 50);
        //System.out.println("intHash_6Test: " + hash);
        Assert.assertTrue(hash < 50);
        Assert.assertTrue(hash == 8);
    }

    @Test
    public void intHash_6Test() {
        int hash = PrimitiveHelper.intHash(0, MAX_INT);
        //System.out.println("zeroMaxIntHash: " + hash);
        Assert.assertTrue(hash < MAX_INT);
        Assert.assertTrue(hash == 441401069);

    }




    @Test
    public void longHash_3Test() {
        long hash = PrimitiveHelper.longHash(CoreConstants.END_OF_TIME, CoreConstants.END_OF_TIME);
        //System.out.println("longHash_3Test: " + hash);
        Assert.assertTrue(hash < CoreConstants.END_OF_TIME);
        Assert.assertTrue(hash == 673163482434621L);
    }

    @Test
    public void longHash_4Test() {
        long hash = PrimitiveHelper.longHash(CoreConstants.END_OF_TIME, 10000);
        //System.out.println("longHash_4Test: " + hash);
        Assert.assertTrue(hash < 10000);
        Assert.assertTrue(hash == 271);
    }

    @Test
    public void longHash_5Test() {
        long hash = PrimitiveHelper.longHash(-156487, 10000);
        System.out.println("longHash_5Test: " + hash);
        Assert.assertTrue(hash < 10000);
        Assert.assertTrue(hash == 9854);
    }

    @Test
    public void longHash_6Test() {
        long hash = PrimitiveHelper.longHash(0, 10000);
        //System.out.println("longHash_6Test: " + hash);
        Assert.assertTrue(hash < 10000);
        Assert.assertTrue(hash == 8147);
    }



    @Test
    public void tripleHash_3Test() {
        long hash = PrimitiveHelper.tripleHash((byte)0x0F, 1, 2, 3, CoreConstants.END_OF_TIME);
        //System.out.println("tripleHash_3Test: " + hash);
        Assert.assertTrue(hash < CoreConstants.END_OF_TIME);
        Assert.assertTrue(hash == 3894355925664350L);
    }

    @Test
    public void tripleHash_4Test() {
        long hash = PrimitiveHelper.tripleHash((byte)0x0F, 1, -1, 3, CoreConstants.END_OF_TIME);
        //System.out.println("tripleHash_4Test: " + hash);
        Assert.assertTrue(hash < CoreConstants.END_OF_TIME);
        Assert.assertTrue(hash == 2577890368030743L);
    }

    @Test
    public void tripleHash_5Test() {
        long hash = PrimitiveHelper.tripleHash((byte)0x0F, 1, 2, 0, CoreConstants.END_OF_TIME);
        //System.out.println("tripleHash_5Test: " + hash);
        Assert.assertTrue(hash < CoreConstants.END_OF_TIME);
        Assert.assertTrue(hash == 942405386945610L);
    }

    @Test
    public void tripleHash_6Test() {
        long hash = PrimitiveHelper.tripleHash((byte)0x0F, 0, 0, 0, CoreConstants.END_OF_TIME);
        //System.out.println("tripleHash_6Test: " + hash);
        Assert.assertTrue(hash < CoreConstants.END_OF_TIME);
        Assert.assertTrue(hash == 4257371302504537L);
    }

    @Test
    public void tripleHash_7Test() {
        long hash = PrimitiveHelper.tripleHash((byte)0xFF, -1, -1, -1, 200);
        //System.out.println("tripleHash_6Test: " + hash);
        Assert.assertTrue(hash < 200);
        Assert.assertTrue(hash == 39);
    }

    @Test
    public void tripleHash_8Test() {
        long hash = PrimitiveHelper.tripleHash((byte)0xE8, 16, 500000, -132654987, 5000);
        //System.out.println("tripleHash_6Test: " + hash);
        Assert.assertTrue(hash < 5000);
        Assert.assertTrue(hash == 4247);
    }


}
