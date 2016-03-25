package org.mwdb.csv;

public interface KField {

    String name();

    KField rename(String newName);

    KField isDouble();

    KField isLong();

    KField isInt();

    KField ignore();

    KField ignoreIfValueEquals(String value);

    interface KTransformFunction {
        Object transform(String value);
    }

    KField transformFunction(KTransformFunction function);

}
