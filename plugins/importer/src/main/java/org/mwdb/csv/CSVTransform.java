package org.mwdb.csv;

public interface CSVTransform {

    CSVElement[] transform(String[] headers, String[][] lines);

}
