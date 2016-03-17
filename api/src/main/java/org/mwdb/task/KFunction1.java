package org.mwdb.task;

@FunctionalInterface
public interface KFunction1<T1, R> {

    R apply(T1 t1) throws Exception;
    
}