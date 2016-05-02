package org.mwg.utils;


public interface DynamicArray<T> {
    T get(int index);
    void put(int index, T element);

    default void remove(int index) {
        put(index,null);
    }

    boolean isEmpty(int index);

    void clean();
}
