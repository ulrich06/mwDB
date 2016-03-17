package org.mwdb.task;

@FunctionalInterface
public interface KConsumer2<T1, T2> {

    void accept(T1 t1, T2 t2) throws Exception;

}