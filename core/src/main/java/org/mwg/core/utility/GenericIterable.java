package org.mwg.core.utility;

import org.mwg.plugin.AbstractIterable;

import java.util.Iterator;

/**
 * @native ts
 * index = 0;
 * isArray = false;
 * input = null;
 * max = -1;
 * constructor(elem){
 * super();
 * if(Array.isArray(elem) || elem instanceof Int8Array || elem instanceof Int16Array || elem instanceof Int32Array || elem instanceof Uint8Array || elem instanceof Uint8ClampedArray || elem instanceof Uint16Array || elem instanceof Uint32Array || elem instanceof Float32Array || elem instanceof Float64Array){
 * this.isArray = true;
 * this.max = elem.length;
 * }
 * this.input = elem;
 * }
 * next(): any {
 * if(this.isArray){
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
 */
public class GenericIterable extends AbstractIterable {

    private byte type = -1;
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
            type = 0;
        } else if (elem instanceof boolean[]) {
            boolArray = (boolean[]) elem;
            max = boolArray.length;
            type = 1;
        } else if (elem instanceof byte[]) {
            byteArray = (byte[]) elem;
            max = byteArray.length;
            type = 2;
        } else if (elem instanceof short[]) {
            shortArray = (short[]) elem;
            max = shortArray.length;
            type = 3;
        } else if (elem instanceof char[]) {
            charArray = (char[]) elem;
            max = charArray.length;
            type = 4;
        } else if (elem instanceof int[]) {
            intArray = (int[]) elem;
            max = intArray.length;
            type = 5;
        } else if (elem instanceof long[]) {
            longArray = (long[]) elem;
            max = longArray.length;
            type = 6;
        } else if (elem instanceof float[]) {
            floatArray = (float[]) elem;
            max = floatArray.length;
            type = 7;
        } else if (elem instanceof double[]) {
            doubleArray = (double[]) elem;
            max = doubleArray.length;
            type = 8;
        } else if (elem instanceof Iterable) {
            iterator = ((Iterable) elem).iterator();
            type = 9;
        } else if (elem instanceof AbstractIterable) {
            absIterable = (AbstractIterable) elem;
            type = 10;
        } else {
            plainObj = elem;
            max = 1;
            type = 11;
        }
    }

    @Override
    public Object next() {
        switch (type) {
            case 0:
                if (index < max) {
                    Object res = objArray[index];
                    index++;
                    return res;
                } else {
                    return null;
                }
            case 1:
                if (index < max) {
                    Object res = boolArray[index];
                    index++;
                    return res;
                } else {
                    return null;
                }
            case 2:
                if (index < max) {
                    Object res = byteArray[index];
                    index++;
                    return res;
                } else {
                    return null;
                }
            case 3:
                if (index < max) {
                    Object res = shortArray[index];
                    index++;
                    return res;
                } else {
                    return null;
                }
            case 4:
                if (index < max) {
                    Object res = charArray[index];
                    index++;
                    return res;
                } else {
                    return null;
                }
            case 5:
                if (index < max) {
                    Object res = intArray[index];
                    index++;
                    return res;
                } else {
                    return null;
                }
            case 6:
                if (index < max) {
                    Object res = longArray[index];
                    index++;
                    return res;
                } else {
                    return null;
                }
            case 7:
                if (index < max) {
                    Object res = floatArray[index];
                    index++;
                    return res;
                } else {
                    return null;
                }
            case 8:
                if (index < max) {
                    Object res = doubleArray[index];
                    index++;
                    return res;
                } else {
                    return null;
                }
            case 9:
                if (iterator.hasNext()) {
                    return iterator.next();
                } else {
                    return null;
                }
            case 10:
                return absIterable.next();
            case 11:
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
}
