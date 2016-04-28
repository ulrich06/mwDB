package org.mwg.util;

/**
 * Created by ludovicmouline on 08/02/16.
 */
public interface K3LongIntMap {
    boolean contains(long universe, long time, long uuid);

   /* int get(String key);

    void put(String key, int value);*/

    int get(long universe, long time, long uuid);

    void put(long universe, long time, long uuid, int value);

    void each(K3LongMapCallBack callback);

    int size();

    void clear();

    void remove(long universe, long time, long uuid);
}
