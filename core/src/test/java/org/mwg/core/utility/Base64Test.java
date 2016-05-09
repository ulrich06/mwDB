package org.mwg.core.utility;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Constants;
import org.mwg.struct.Buffer;

public class Base64Test {

    @Test
    public void beginingOfTimeEncodingTest() {
        testLong(Constants.BEGINNING_OF_TIME);
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

    private void testLong(long val) {



        Buffer buffer = BufferBuilder.newHeapBuffer();
        Base64.encodeLongToBuffer(val, buffer);
        long dec = Base64.decodeToLongWithBounds(buffer, 0, buffer.size());
        Assert.assertEquals(val, dec);

        /*
        //System.out.println("Encode");
        StringBuilder buffer = new StringBuilder();
        Base64.encodeLongToBuffer(val, buffer);
        //System.out.println("Decode");
        dec = Base64.decodeToLong(buffer.toString());
        //System.out.println(val + " -> " + enc + " -> " + dec);
        Assert.assertEquals(val, dec);
        */
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
        int dec = Base64.decodeToIntWithBounds(buffer, 0, buffer.size());
        //System.out.println(val + " -> " + enc + " -> " + dec);
        Assert.assertEquals(val, dec);
        buffer.free();

        /*
        //System.out.println("Encode");
        StringBuilder buffer = new StringBuilder();
        Base64.encodeIntToBuffer(val, buffer);
        //System.out.println("Decode");
        dec = Base64.decodeToInt(buffer.toString());
        //System.out.println(val + " -> " + enc + " -> " + dec);
        Assert.assertEquals(val, dec);
        */
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

    /**
     * @native ts
     * var enc = Base64.encodeDouble(val);
     * var dec = Base64.decodeToDouble(enc);
     * org.junit.Assert.assertEquals(val, dec);
     * var buffer = new java.lang.StringBuilder();
     * Base64.encodeDoubleToBuffer(val, buffer);
     * dec = Base64.decodeToDouble(buffer.toString());
     * org.junit.Assert.assertEquals(val, dec);
     */
    private void testDouble(double val) {
        Buffer buffer = BufferBuilder.newHeapBuffer();
        Base64.encodeDoubleToBuffer(val, buffer);
        double dec = Base64.decodeToDoubleWithBounds(buffer, 0, buffer.size());
        //System.out.println(val + " -> " + enc + " -> " + dec);
        Assert.assertEquals(val, dec, 0);
        buffer.free();

        /*
        //System.out.println("Encode");
        StringBuilder buffer = new StringBuilder();
        Base64.encodeDoubleToBuffer(val, buffer);
        //System.out.println("Decode");
        dec = Base64.decodeToDouble(buffer.toString());
        //System.out.println(val + " -> " + enc + " -> " + dec);
        Assert.assertEquals(val, dec, 0);
        */
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
        Base64.encodeBoolArrayToBuffer(array,buffer);
        boolean[] dec = Base64.decodeToBoolArrayWithBounds(buffer,0,buffer.size(), array.length);
        //System.out.println(0x7fffffff + " -> " + enc + " -> " + dec);
        Assert.assertTrue(array.length == dec.length);
        for (int i = 0; i < array.length; i++) {
            // Assert.assertEquals("\n" + Arrays.toString(array) + "\n -> "+ enc +" -> \n" + Arrays.toString(dec),array[i], dec[i]);
            Assert.assertEquals(array[i], dec[i]);
        }
    }


   /* @Test
    public void emptyStringEncodingTest() { testString(""); }

    @Test
    public void l1StringEncodingTest() { testString("a"); }

    @Test
    public void l2StringEncodingTest() { testString("ab"); }

    @Test
    public void l3StringEncodingTest() { testString("abc"); }

    @Test
    public void l4StringEncodingTest() { testString("abcd"); }

    private void testString(String val) {
        //System.out.println("Encode");
        String enc = Base64.encodeString(val);
        //System.out.println("Decode");
        String dec = Base64.decodeToString(enc);
        //System.out.println(val + " -> " + enc + " -> " + dec);
        Assert.assertEquals(val, dec);

        //System.out.println("Encode");
        StringBuilder buffer = new StringBuilder();
        Base64.encodeStringToBuffer(val, buffer);
        //System.out.println("Decode");
        dec = Base64.decodeToString(buffer.toString());
        //System.out.println(val + " -> " + enc + " -> " + dec);
        Assert.assertEquals(val, dec);
    }
*/

    /*

        private String printBits(Long val) {
            String toString = Long.toBinaryString(val);
            String res = "";

            for(int i = 0; i < 64-toString.length(); i++) {
                res += "0";
            }
            return res + toString;
        }


        public static void main(String[] args) {
            String res = "";
            int i = 0;
            for(char c= 'A'; c <='Z'; c++) {
                res += "\""+c+"\":" + i + ", "; i++;
            }
            for(char c= 'a'; c <='z'; c++) {
                res += "\""+c+"\":" + i + ", "; i++;
            }
            for(char c= '0'; c <='9'; c++) {
                res += "\""+c+"\":" + i + ", "; i++;
            }
            res += "\"+\":" + i + ", "; i++;
            res += "\"/\":" + i;
            System.out.println(res);
        }


        */

    @Test
    public void testBigLong() {
        //Long.Max
        long longMax = Long.MAX_VALUE;
        Buffer buffer = BufferBuilder.newHeapBuffer();
        Base64.encodeLongToBuffer(longMax,buffer);

        long decodedLongMax = Base64.decodeToLongWithBounds(buffer,0,buffer.size());
        Assert.assertEquals(longMax,decodedLongMax);

        buffer.free();

        //Long.Min
        long longMin = Long.MIN_VALUE;
        Buffer buffer2 = BufferBuilder.newHeapBuffer();
        Base64.encodeLongToBuffer(longMin,buffer2);

        long decodedLongMin = Base64.decodeToLongWithBounds(buffer2,0,buffer2.size());
        Assert.assertEquals(longMin,decodedLongMin);

        buffer2.free();
    }


}
