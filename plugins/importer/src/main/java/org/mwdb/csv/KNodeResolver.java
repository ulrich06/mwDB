package org.mwdb.csv;

import org.mwdb.KCallback;
import org.mwdb.KGraph;
import org.mwdb.KNode;

import java.util.Map;

public interface KNodeResolver {

    void resolve(KGraph graph, Map<String, Integer> headers, String[] values, long toResolveTime, KCallback<KNode> callback);

}
