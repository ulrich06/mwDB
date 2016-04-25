package org.mwg.csv;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.Node;

import java.util.Map;

public interface KNodeResolver {

    void resolve(Graph graph, Map<String, Integer> headers, String[] values, long toResolveWorld, long toResolveTime, Callback<Node> callback);

}
