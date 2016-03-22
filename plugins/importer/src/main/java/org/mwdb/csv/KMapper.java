package org.mwdb.csv;

public interface KMapper {

    KField field(String name);

    void timestamp(String[] fields, String[] pattern);

}
