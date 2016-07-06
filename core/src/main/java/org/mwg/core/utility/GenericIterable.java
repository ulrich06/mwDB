package org.mwg.core.utility;

import org.mwg.plugin.AbstractIterable;

import java.util.Iterator;

/**
 * @native ts
 * index = 0;
 * array = false;
 * input = null;
 * max = -1;
 * constructor(elem){
 * super();
 * if(Array.isArray(elem) || elem instanceof Int8Array || elem instanceof Int16Array || elem instanceof Int32Array || elem instanceof Uint8Array || elem instanceof Uint8ClampedArray || elem instanceof Uint16Array || elem instanceof Uint32Array || elem instanceof Float32Array || elem instanceof Float64Array){
 * this.array = true;
 * this.max = elem.length;
 * }
 * this.input = elem;
 * }
 * next(): any {
 * if(this.array){
 * var res = this.input[this.index];
 * this.index = this.index + 1;
 * return res;
 * } else {
 * if(this.input != null){
 * var res = this.input;
 * this.input = null;
 * return res;
 * } else {
 * return null;
 * }
 * }
 * }
 * close():void{
 * }
 * estimate():number{
 * return this.max;
 * }
 * isArray():boolean{
 * return this.array;
 * }
 */
public class GenericIterable extends AbstractIterable {

    private static final int UNKNOW = -1;
    private static final int OBJ_ARRAY = 0;
    private static final int BOOL_ARRAY = 1;
    private static final int BYTE_ARRAY = 2;
    private static final int SHORT_ARRAY = 3;
    private static final int CHAR_ARRAY = 4;
    private static final int INT_ARRAY = 5;
    private static final int LONG_ARRAY = 6;
    private static final int FLOAT_ARRAY = 7;
    private static final int DOUBLE_ARRAY = 8;
    private static final int ITERABLE = 9;
    private static final int ABS_ITERABLE = 10;
    private static final int PLAIN_OBJ = 11;

    private byte type = UNKNOW;
    private Object[] objArray = null;
    private boolean[] boolArray = null;
    private byte[] byteArray = null;
    private short[] shortArray = null;
    private char[] charArray = null;
    private int[] intArray = null;
    private long[] longArray = null;
    private float[] floatArray = null;
    private double[] doubleArray = null;
    private Iterator iterator = null;
    private AbstractIterable absIterable = null;
    private Object plainObj = null;

    private int index = 0;
    private int max = -1;

    public GenericIterable(Object elem) {
        if (elem instanceof Object[]) {
            objArray = (Object[]) elem;
            max = objArray.length;
            type = OBJ_ARRAY;
        } else if (elem instanceof boolean[]) {
            boolArray = (boolean[]) elem;
            max = boolArray.length;
            type = BOOL_ARRAY;
        } else if (elem instanceof byte[]) {
            byteArray = (byte[]) elem;
            max = byteArray.length;
            type = BYTE_ARRAY;
        } else if (elem instanceof short[]) {
            shortArray = (short[]) elem;
            max = shortArray.length;
            type = SHORT_ARRAY;
        } else if (elem instanceof char[]) {
            charArray = (char[]) elem;
            max = charArray.length;
            type = CHAR_ARRAY;
        } else if (elem instanceof int[]) {
            intArray = (int[]) elem;
            max = intArray.length;
            type = INT_ARRAY;
        } else if (elem instanceof long[]) {
            longArray = (long[]) elem;
            max = longArray.length;
            type = LONG_ARRAY;
        } else if (elem instanceof float[]) {
            floatArray = (float[]) elem;
            max = floatArray.length;
            type = FLOAT_ARRAY;
        } else if (elem instanceof double[]) {
            doubleArray = (double[]) elem;
            max = doubleArray.length;
            type = DOUBLE_ARRAY;
        } else if (elem instanceof Iterable) {
            iterator = ((Iterable) elem).iterator();
            type = ITERABLE;
        } else if (elem instanceof AbstractIterable) {
            absIterable = (AbstractIterable) elem;
            type = ABS_ITERABLE;
            max = absIterable.estimate();
        } else {
            plainObj = elem;
            type = PLAIN_OBJ;
            max = 1;
        }
    }

    @Override
    public Object next() {
        switch (type) {
            case OBJ_ARRAY:
                if (index < max) {
                    Object res = objArray[index];
                    index++;
                    return res;
                } else {
                    return null;
                }
            case BOOL_ARRAY:
                if (index < max) {
                    Object res = boolArray[index];
                    index++;
                    return res;
                } else {
                    return null;
                }
            case BYTE_ARRAY:
                if (index < max) {
                    Object res = byteArray[index];
                    index++;
                    return res;
                } else {
                    return null;
                }
            case SHORT_ARRAY:
                if (index < max) {
                    Object res = shortArray[index];
                    index++;
                    return res;
                } else {
                    return null;
                }
            case CHAR_ARRAY:
                if (index < max) {
                    Object res = charArray[index];
                    index++;
                    return res;
                } else {
                    return null;
                }
            case INT_ARRAY:
                if (index < max) {
                    Object res = intArray[index];
                    index++;
                    return res;
                } else {
                    return null;
                }
            case LONG_ARRAY:
                if (index < max) {
                    Object res = longArray[index];
                    index++;
                    return res;
                } else {
                    return null;
                }
            case FLOAT_ARRAY:
                if (index < max) {
                    Object res = floatArray[index];
                    index++;
                    return res;
                } else {
                    return null;
                }
            case DOUBLE_ARRAY:
                if (index < max) {
                    Object res = doubleArray[index];
                    index++;
                    return res;
                } else {
                    return null;
                }
            case ITERABLE:
                if (iterator.hasNext()) {
                    return iterator.next();
                } else {
                    return null;
                }
            case ABS_ITERABLE:
                return absIterable.next();
            case PLAIN_OBJ:
                if (plainObj != null) {
                    Object res = plainObj;
                    plainObj = null;
                    return res;
                } else {
                    return null;
                }

        }
        return null;
    }

    @Override
    public void close() {
        if (absIterable != null) {
            absIterable.close();
        }
    }

    @Override
    public int estimate() {
        return this.max;
    }

    public boolean isArray() {
        return type != UNKNOW && type != PLAIN_OBJ;
    }
}
