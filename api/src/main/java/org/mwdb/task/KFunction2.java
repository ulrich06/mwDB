package org.mwdb.task;

@FunctionalInterface
public interface KFunction2<T1, T2, R> {

    R apply(T1 t1, T2 t2) throws Exception;

}