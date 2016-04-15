package org.mwdb;

import org.mwdb.task.KFunction1;

public interface KTraversal {

    KTraversal world(long world);

    KTraversal time(long time);


    KTraversal with(String name, String pattern);

    KTraversal without(String name, String pattern);

    KTraversal has(String name);

    KTraversal filter(KFunction1<KNode, Boolean> closure);

    KTraversal relation(String name);

    KTraversal as(String variableName);

    KTraversal from(String variableName);

    KTraversal count();

    KTraversal values(String name);


    KTraversal map();

    KTraversal flatMap(String name);


    /**
     * TODO
     */
    KTraversal where(String name);


}
