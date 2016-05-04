package org.mwg.lru;

import org.kevoree.modeling.KConfig;
import org.mwg.math.Array3LongIntMap;
import org.mwg.math.K3LongIntMap;

/**
 * Created by ludovicmouline on 01/02/16.
 */
public class LRUKeys {

    private int _capacity;

    private String[] _cache;

    private int[] _next; // indexes of next element, _next[i] : the _next (i.e older _value) of _cache[i]
    private int[] _prev; //indexes of previous element, _prev[i] : the previous (i.e younger _value) of _cache[i]
    private int _head;

    private K3LongIntMap _indexes;

    public LRUKeys(int capacity) {
        _capacity = capacity;
        _cache = new String[_capacity];

        _next = new int[_capacity];
        _prev = new int[_capacity];
        for(int i = 0; i<_capacity;i++) {
            _next[i] = (i + 1) % _capacity;
            _prev[i] = ((i - 1) % _capacity + _capacity) % _capacity;
        }

        _indexes = new Array3LongIntMap(capacity, KConfig.CACHE_LOAD_FACTOR);
    }

    /*private int containKey(String key) {
        *//*Integer index = _indexes.get(key);
        return (index == null)? -1 : index;*//*
    }*/

    /**
     *
     * @param keys
     *      keys.lenght % 3 == 0
     *      keys[3i] : universe
     *      keys[3i + 1] : time
     *      keys[3i+ 2] : uuid
     * @param values
     *      values.length == keys.lenght / 3
     *      {keys[3i], keys[3i + 1], keys[3i+ 2]} => values[i]
     *      values: KObject serialized
     *
     */
   public void put(long[] keys, String[] values) {
       int nbKChunk = keys.length / 3;
       for(int i=0;i<nbKChunk;i++) {

//           String concatKey = KContentKey.toString(keys,i);
           int indexValue = _indexes.get(keys[i],keys[i+1],keys[i+2]);//containKey(concatKey);
           if(indexValue == -1){//insert new value
               _indexes.remove(keys[i],keys[i+1],keys[i+2]);
               _cache[_head] = values[i];
               _indexes.put(keys[i],keys[i+1],keys[i+2],_head);
               _head = _next[_head];
           } else if(indexValue == _head) {//insert value that already exist and the head pointer is on this value
               _head = _next[_head];
           } else {//insert value that already exist and the head pointer is NOT on this value => the LRU order changes
               _next[_prev[indexValue]] = _next[indexValue];
               _prev[_next[indexValue]] = _prev[indexValue];
               _next[indexValue] = _head;
               _prev[indexValue] = _prev[_head];
               _next[_prev[_head]] = indexValue;
               _prev[_head] = indexValue;
           }
       }

   }

    /**
     *
     * @param keys
     *      keys.lenght % 3 == 0
     *      keys[3i] : universe
     *      keys[3i + 1] : time
     *      keys[3i+ 2] : uuid
     * @return
     */
    public String[] get(long[] keys) {
        int nbKChunkKey = keys.length / 3;
        String[] toReturn = null;
        for(int i=0;i<nbKChunkKey;i++) {
//            String concatKey = KContentKey.toString(keys,i);
            int indexValue = _indexes.get(keys[i],keys[i+1],keys[i+2]);//containKey(concatKey);
            if(indexValue != -1){
                if(indexValue != _head) {
                    _next[_prev[indexValue]] = _next[indexValue];
                    _prev[_next[indexValue]] = _prev[indexValue];
                    _next[indexValue] = _head;
                    _prev[indexValue] = _prev[_head];
                    _next[_prev[_head]] = indexValue;
                    _prev[_head] = indexValue;
                }
                if(toReturn == null) {
                    toReturn = new String[nbKChunkKey];
                }
                toReturn[i] = _cache[indexValue];
            }
        }
        return toReturn;
    }
}
