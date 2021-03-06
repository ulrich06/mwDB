package org.mwg.core.utility;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.core.CoreConstants;

public class PrimitiveHelperTest {

    // Integer.MIN_VALUE == 0x80000000
    public static final int MIN_INT = -2147483648;

    // Integer.MAX_VALUE == 0x7FFFFFFF
    public static final int MAX_INT = 2147483647;

    /* MAX TESTS */

    @Test
    public void longHash_0Test() {
        try {
            PrimitiveHelper.longHash(1, 0);
            Assert.fail("This should have thrown an exception");
        } catch (Exception e) {
        }
    }

    @Test
    public void longHash_1Test() {
        try {
            PrimitiveHelper.longHash(1, CoreConstants.BEGINNING_OF_TIME);
            Assert.fail("This should have thrown an exception");
        } catch (Exception e) {
        }
    }

    @Test
    public void tripleHash_0Test() {
        try {
            PrimitiveHelper.tripleHash((byte) 1, 2, 3, 4, 0);
            Assert.fail("This should have thrown an exception");
        } catch (Exception e) {
        }
    }

    @Test
    public void tripleHash_1Test() {
        try {
            PrimitiveHelper.tripleHash((byte) 1, 2, 3, 4, CoreConstants.BEGINNING_OF_TIME);
            Assert.fail("This should have thrown an exception");
        } catch (Exception e) {
        }
    }


    /* HASH TESTS */


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
        //System.out.println("longHash_5Test: " + hash);
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
        long hash = PrimitiveHelper.tripleHash((byte) 1, 1, 2, 3, CoreConstants.END_OF_TIME);
        //System.out.println("tripleHash_3Test: " + hash);
        Assert.assertTrue(hash < CoreConstants.END_OF_TIME);
        Assert.assertTrue(hash == 6324531823975995L);
    }

    @Test
    public void tripleHash_4Test() {
        long hash = PrimitiveHelper.tripleHash((byte) 2, 1, -1, 3, CoreConstants.END_OF_TIME);
        //System.out.println("tripleHash_4Test: " + hash);
        Assert.assertTrue(hash < CoreConstants.END_OF_TIME);
        Assert.assertTrue(hash == 2261661239301336L);
    }

    @Test
    public void tripleHash_5Test() {
        long hash = PrimitiveHelper.tripleHash((byte) 3, 1, 2, 0, CoreConstants.END_OF_TIME);
        //System.out.println("tripleHash_5Test: " + hash);
        Assert.assertTrue(hash < CoreConstants.END_OF_TIME);
        Assert.assertTrue(hash == 914239194442175L);
    }

    @Test
    public void tripleHash_6Test() {
        long hash = PrimitiveHelper.tripleHash((byte) 4, 0, 0, 0, CoreConstants.END_OF_TIME);
        //System.out.println("tripleHash_6Test: " + hash);
        Assert.assertTrue(hash < CoreConstants.END_OF_TIME);
        Assert.assertTrue(hash == 1254293488547125L);
    }

    @Test
    public void tripleHash_7Test() {
        long hash = PrimitiveHelper.tripleHash((byte) 4, -1, -1, -1, 200);
        //System.out.println("tripleHash_7Test: " + hash);
        Assert.assertTrue(hash < 200);
        Assert.assertTrue(hash == 169);
    }

    @Test
    public void tripleHash_8Test() {
        long hash = PrimitiveHelper.tripleHash((byte) 1, 16, 500000, -132654987, 5000);
        //System.out.println("tripleHash_8Test: " + hash);
        Assert.assertTrue(hash < 5000);
        Assert.assertTrue(hash == 1380);
    }

    /*
    @Test
    public void stringHashPerfTest() {

        final String val = "myAttributeNamett";
        long before = System.currentTimeMillis();
        long hash = 0;

        for (int i = 0; i < 1000000000; i++) {
            hash += val.hashCode();
        }
        System.out.println("Time:" + (System.currentTimeMillis() - before) + " L:" + hash);

        before = System.currentTimeMillis();
        hash = 0;
        for (int i = 0; i < 1000000000; i++) {
            hash += PrimitiveHelper.stringHash2(val);
        }
        System.out.println("Time:" + (System.currentTimeMillis() - before) + " L:" + hash);

        before = System.currentTimeMillis();
        hash = 0;
            byte[] toBytes = val.getBytes();
        for (int i = 0; i < 100000000; i++) {
            hash += DataHasher.hash(toBytes);
        }
        System.out.println("Time:" + (System.currentTimeMillis() - before) + " L:" + hash);
    }
*/


}
