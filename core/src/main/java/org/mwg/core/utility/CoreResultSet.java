package org.mwg.core.utility;

import org.mwg.plugin.AbstractNode;
import org.mwg.utility.GenericIterator;
import org.mwg.utility.ResultSet;

/**
 * @ignore ts
 */
public class CoreResultSet implements ResultSet {

    private Object[] objArray = null;

    private boolean[] boolArray = null;
    private byte[] byteArray = null;
    private short[] shortArray = null;
    private char[] charArray = null;
    private int[] intArray = null;
    private long[] longArray = null;
    private float[] floatArray = null;
    private double[] doubleArray = null;

    private Object plainObj = null;

    private boolean isArray = true;

    private int maxElem;


    public CoreResultSet(Object elem) {
        if (elem instanceof Object[]) {
            objArray = (Object[]) elem;
            maxElem = objArray.length;
        } else if (elem instanceof boolean[]) {
            boolArray = (boolean[]) elem;
            maxElem = boolArray.length;
        } else if (elem instanceof byte[]) {
            byteArray = (byte[]) elem;
            maxElem = byteArray.length;
        } else if (elem instanceof short[]) {
            shortArray = (short[]) elem;
            maxElem = shortArray.length;
        } else if (elem instanceof char[]) {
            charArray = (char[]) elem;
            maxElem = charArray.length;
        } else if (elem instanceof int[]) {
            intArray = (int[]) elem;
            maxElem = intArray.length;
        } else if (elem instanceof long[]) {
            longArray = (long[]) elem;
            maxElem = longArray.length;
        } else if (elem instanceof float[]) {
            floatArray = (float[]) elem;
            maxElem = floatArray.length;
        } else if (elem instanceof double[]) {
            doubleArray = (double[]) elem;
            maxElem = doubleArray.length;
        }  else {
            plainObj = elem;
            isArray = false;
            maxElem = 1;
        }
    }


    @Override
    public GenericIterator iterator() {
        return new GenericIteratorOImpl();
    }

    @Override
    public Object get() {
        if(objArray != null) {
            return objArray;
        } else if(boolArray != null) {
            return boolArray;
        } else if(byteArray != null) {
            return byteArray;
        } else if(shortArray != null) {
            return shortArray;
        } else if(charArray != null) {
            return charArray;
        } else if(intArray != null) {
            return intArray;
        } else if(longArray != null) {
            return longArray;
        } else if(floatArray != null) {
            return longArray;
        } else if(doubleArray != null) {
            return doubleArray;
        } else {
            return plainObj;
        }
    }


    @Override
    public ResultSet clone() {
       if(objArray != null) {
            return new CoreResultSet(cloneObjectArray(objArray));
        } else if(boolArray != null) {
            return new CoreResultSet(boolArray);
        } else if(byteArray != null) {
            return new CoreResultSet(byteArray);
        } else if(shortArray != null) {
            return new CoreResultSet(shortArray);
        } else if(charArray != null) {
            return new CoreResultSet(charArray);
        } else if(intArray != null) {
            return new CoreResultSet(intArray);
        } else if(longArray != null) {
            return new CoreResultSet(longArray);
        } else if(doubleArray != null) {
            return new CoreResultSet(doubleArray);
        } else if(floatArray != null) {
            return new CoreResultSet(floatArray);
        } else {
            if(plainObj instanceof AbstractNode) {
                return new CoreResultSet(((AbstractNode) plainObj).clone());
            }
            return new CoreResultSet(plainObj);
        }
    }

    private Object[] cloneObjectArray(Object[] toCopy){
        Object[] res = new Object[toCopy.length];
        for(int i=0;i<toCopy.length;i++) {
           if(toCopy[i] instanceof AbstractNode) {
               res[i] = ((AbstractNode)toCopy[i]).clone();
           } else if(toCopy[i] instanceof Object[]) {
               res[i] = cloneObjectArray((Object[]) toCopy[i]);
           } else {
               res[i] = toCopy[i];
           }
        }
        return res;
    }

    @Override
    public void clean() {
        boolArray = null;
        byteArray = null;
        shortArray = null;
        charArray = null;
        intArray = null;
        longArray = null;
        floatArray = null;
        doubleArray = null;

        if(plainObj != null) {
            if(plainObj instanceof AbstractNode) {
                ((AbstractNode) plainObj).free();
            }
        }

        if(objArray != null) {
            cleanObj(objArray);
        }
    }

    private void cleanObj(Object[] toClean) {
        for(int i=0;i<toClean.length;i++) {
            if(toClean[i] instanceof AbstractNode) {
                ((AbstractNode)toClean[i]).free();
            } else if(toClean[i] instanceof Object[]) {
                cleanObj((Object[]) toClean[i]);
            } else {
                toClean = null;
            }
        }
    }

    @Override
    public int estimate() {
        return maxElem;
    }

    @Override
    public boolean isArray() {
        return isArray;
    }

    private class GenericIteratorOImpl implements GenericIterator {
        private int nextElem;

        @Override
        public Object next() {
            if(nextElem < maxElem) {
                if(objArray != null) {
                    return objArray[nextElem++];
                } else if(boolArray != null) {
                    return boolArray[nextElem++];
                } else if(byteArray != null) {
                    return byteArray[nextElem++];
                } else if(shortArray != null) {
                    return shortArray[nextElem++];
                } else if(charArray != null) {
                    return charArray[nextElem++];
                } else if(intArray != null) {
                    return intArray[nextElem++];
                } else if(longArray != null) {
                    return longArray[nextElem++];
                } else if(floatArray != null) {
                    return longArray[nextElem++];
                } else if(doubleArray != null) {
                    return doubleArray[nextElem++];
                } else {
                    nextElem++;
                    return plainObj;
                }
            } else {
                throw new RuntimeException("Iterator go out");
            }
        }

        @Override
        public boolean hasNext() {
            return nextElem == maxElem;
        }

        @Override
        public void close() {

        }
    }

}
