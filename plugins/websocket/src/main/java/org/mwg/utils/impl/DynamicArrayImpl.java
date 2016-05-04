package org.mwg.utils.impl;

import org.mwg.utils.DynamicArray;

public class DynamicArrayImpl<T> implements DynamicArray<T> {
    private Object[] _data;


    public DynamicArrayImpl(int initial) {
        _data = new Object[initial];
    }

    @Override
    public T get(int index) {
        return (T) _data[index];
    }

    @Override
    public void put(int index, T element) {
        if(index >= _data.length) {
            Object[] tmp = new Object[_data.length * 2];
            System.arraycopy(_data,0,tmp,0,_data.length);
            _data = tmp;
        }
        _data[index] = element;
    }

    @Override
    public boolean isEmpty(int index) {
        if(index >= _data.length) {
            return true;
        }
        return _data[index] == null;
    }

    @Override
    public void clean() {
        for(int i =0;i<_data.length;i++) {
            _data[i] = null;
        }
    }
}
