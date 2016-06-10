package org.mwg.core.utility;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Constants;
import org.mwg.struct.Buffer;
import org.mwg.plugin.Base64;

public class Base64Test {

    @Test
    public void beginingOfTimeEncodingTest() {
        testLong(Constants.BEGINNING_OF_TIME);
    }

    @Test
    public void typeEncoding() {
        testLong(DataHasher.hash("GaussianGmm"));
    }

    @Test
    public void endOfTimeEncodingTest() {
        testLong(Constants.END_OF_TIME);
    }

    @Test
    public void nullEncodingTest() {
        testLong(Constants.NULL_LONG);
    }

    @Test
    public void zeroEncodingTest() {
        testLong(0l);
    }

    @Test
    public void oneEncodingTest() {
        testLong(1l);
    }

    @Test
    public void randomBigNumTest() {
        testLong(68719476737l);
    }

    @Test
    public void hashTest() {
        testLong(-365393685203911L);
    }

    private void testLong(long val) {


        Buffer buffer = BufferBuilder.newHeapBuffer();
        Base64.encodeLongToBuffer(val, buffer);
        long dec = Base64.decodeToLongWithBounds(buffer, 0, buffer.length());
        Assert.assertEquals(val, dec);

    }


    @Test
    public void minIntEncodingTest() {
        testInt(0x80000000);
    }

    @Test
    public void maxIntEncodingTest() {
        testInt(0x7fffffff);
    }

    private void testInt(int val) {
        Buffer buffer = BufferBuilder.newHeapBuffer();
        Base64.encodeIntToBuffer(val, buffer);
        int dec = Base64.decodeToIntWithBounds(buffer, 0, buffer.length());
        //System.out.println(val + " -> " + enc + " -> " + dec);
        Assert.assertEquals(val, dec);
        buffer.free();

    }


    /**
     * @native ts
     * this.testDouble(Number.MAX_VALUE);
     */
    @Test
    public void maxDoubleEncodingTest() {
        testDouble(Double.MAX_VALUE);
    }

    /**
     * @native ts
     * this.testDouble(Number.MIN_VALUE);
     */
    @Test
    public void minDoubleEncodingTest() {
        testDouble(PrimitiveHelper.DOUBLE_MIN_VALUE());
    }

    /**
     * @native ts
     * this.testDouble(-Number.MAX_VALUE);
     */
    @Test
    public void negMaxDoubleEncodingTest() {
        testDouble(-PrimitiveHelper.DOUBLE_MAX_VALUE());
    }

    /**
     * @native ts
     * this.testDouble(-Number.MIN_VALUE);
     */
    @Test
    public void negMinDoubleEncodingTest() {
        testDouble(-PrimitiveHelper.DOUBLE_MIN_VALUE());
    }

    @Test
    public void zeroDoubleEncodingTest() {
        testDouble(0);
        testDouble(0.1);
        testDouble(0.25);
        testDouble(0.5);
        testDouble(0.75);
        testDouble(1.1);
        testDouble(2.1);
        testDouble(0.000000000000002);
    }


    private void testDouble(double val) {
        Buffer buffer = BufferBuilder.newHeapBuffer();
        Base64.encodeDoubleToBuffer(val, buffer);
        double dec = Base64.decodeToDoubleWithBounds(buffer, 0, buffer.length());
        //System.out.println(val + " -> " + enc + " -> " + dec);
        Assert.assertTrue(val == dec);
        buffer.free();

    }


    @Test
    public void boolArrayEncodingTest() {

        for (int i = 0; i < 255; i++) {
            boolean[] tmpArray = new boolean[i];
            for (int j = 0; j < i; j++) {
                tmpArray[j] = Math.random() < 0.5;
            }
            boolArrayInnerTest(tmpArray);
        }
    }

    private void boolArrayInnerTest(boolean[] array) {
        Buffer buffer = BufferBuilder.newHeapBuffer();
        Base64.encodeBoolArrayToBuffer(array, buffer);
        boolean[] dec = Base64.decodeToBoolArrayWithBounds(buffer, 0, buffer.length(), array.length);
        //System.out.println(0x7fffffff + " -> " + enc + " -> " + dec);
        Assert.assertTrue(array.length == dec.length);
        for (int i = 0; i < array.length; i++) {
            // Assert.assertEquals("\n" + Arrays.toString(array) + "\n -> "+ enc +" -> \n" + Arrays.toString(dec),array[i], dec[i]);
            Assert.assertEquals(array[i], dec[i]);
        }
    }


}
