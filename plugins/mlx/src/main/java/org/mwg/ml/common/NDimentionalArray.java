package org.mwg.ml.common;

public class NDimentionalArray {

    public NDimentionalArray() {
        //Create the hashmap here
    }

    public double get(int[] indices) {
        return 0;
    }

    //Should be synchronized
    public void set(int[] indices, double value) {

    }

    public void add(int[] indices, double value) {
        set(indices, get(indices) + value);
    }
}
