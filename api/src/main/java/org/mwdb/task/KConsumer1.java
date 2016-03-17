package org.mwdb.task;

@FunctionalInterface
public interface KConsumer1<T1> {

    void accept(T1 t1) throws Exception;

}