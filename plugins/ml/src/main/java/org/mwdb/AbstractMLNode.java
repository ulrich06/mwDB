package org.mwdb;

import org.mwdb.KCallback;
import org.mwdb.KGraph;
import org.mwdb.KMLNode;
import org.mwdb.KNode;

public abstract class AbstractMLNode<A extends KNode> implements KMLNode<A> {

    private final KNode _rootNode;

    public abstract void jump(long world, long time, KCallback<A> callback);

    public AbstractMLNode(KNode p_rootNode) {
        this._rootNode = p_rootNode;
    }

    protected KNode rootNode() {
        return _rootNode;
    }

    @Override
    public KGraph graph() {
        return _rootNode.graph();
    }

    @Override
    public long world() {
        return _rootNode.world();
    }

    @Override
    public long time() {
        return _rootNode.time();
    }

    @Override
    public long id() {
        return _rootNode.id();
    }

    @Override
    public Object att(String attributeName) {
        return _rootNode.att(attributeName);
    }

    @Override
    public byte attType(String attributeName) {
        return _rootNode.attType(attributeName);
    }

    @Override
    public void attSet(String attributeName, byte attributeType, Object attributeValue) {
        _rootNode.attSet(attributeName, attributeType, attributeValue);
    }

    @Override
    public Object attMap(String attributeName, byte attributeType) {
        return _rootNode.attMap(attributeName, attributeType);
    }

    @Override
    public void attRemove(String attributeName) {
        _rootNode.attRemove(attributeName);
    }

    @Override
    public void rel(String relationName, KCallback<KNode[]> callback) {
        _rootNode.rel(relationName, callback);
    }

    @Override
    public long[] relValues(String relationName) {
        return _rootNode.relValues(relationName);
    }

    @Override
    public void relAdd(String relationName, KNode relatedNode) {
        _rootNode.relAdd(relationName, relatedNode);
    }

    @Override
    public void relRemove(String relationName, KNode relatedNode) {
        _rootNode.relRemove(relationName, relatedNode);
    }

    @Override
    public void index(String indexName, KNode nodeToIndex, String[] keyAttributes, KCallback<Boolean> callback) {
        _rootNode.index(indexName, nodeToIndex, keyAttributes, callback);
    }

    @Override
    public void unindex(String indexName, KNode nodeToIndex, String[] keyAttributes, KCallback<Boolean> callback) {
        _rootNode.unindex(indexName, nodeToIndex, keyAttributes, callback);
    }

    @Override
    public void find(String indexName, String query, KCallback<KNode[]> callback) {
        _rootNode.find(indexName, query, callback);
    }

    @Override
    public void all(String indexName, KCallback<KNode[]> callback) {
        _rootNode.all(indexName, callback);
    }

    @Override
    public long timeDephasing() {
        return _rootNode.timeDephasing();
    }

    @Override
    public void forcePhase() {
        _rootNode.forcePhase();
    }

    @Override
    public void timepoints(long beginningOfSearch, long endOfSearch, KCallback<long[]> callback) {
        _rootNode.timepoints(beginningOfSearch, endOfSearch, callback);
    }

    @Override
    public void free() {
        _rootNode.free();
    }

}
