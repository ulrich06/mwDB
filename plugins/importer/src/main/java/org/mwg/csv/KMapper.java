package org.mwg.csv;

public interface KMapper {

    KField field(String name);

    void extractTime(String fields, String pattern);

    KMapper globallyIgnoreIfValueEquals(String value);

    void nodeResolver(KNodeResolver resolver);

}
