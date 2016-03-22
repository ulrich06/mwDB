package org.mwdb;

public interface KTimeExtractor {

    long time(String[] headers, String[] lineValues);

}
