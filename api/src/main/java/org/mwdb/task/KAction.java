package org.mwdb.task;

@FunctionalInterface
public interface KAction {
    void run() throws Exception;
}