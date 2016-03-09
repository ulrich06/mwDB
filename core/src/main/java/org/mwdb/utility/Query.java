package org.mwdb.utility;

import org.mwdb.Constants;
import org.mwdb.plugin.KResolver;

public class Query {

    public long hash;

    private int capacity = 1;
    public long[] attributes = new long[capacity];
    public String[] values = new String[capacity];
    public int size = 0;

    public void add(long att, String val) {
        if (size == capacity) {
            //init
            int temp_capacity = capacity * 2;
            long[] temp_attributes = new long[temp_capacity];
            String[] temp_values = new String[temp_capacity];
            //copy
            System.arraycopy(attributes, 0, temp_attributes, 0, capacity);
            System.arraycopy(values, 0, temp_values, 0, capacity);
            //assign
            attributes = temp_attributes;
            values = temp_values;
            capacity = temp_capacity;

        }
        attributes[size] = att;
        values[size] = val;
        size++;
    }

    public void compute() {
        sort();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < size; i++) {
            Base64.encodeLongToBuffer(attributes[i], buffer);
            buffer.append(values[i]);
        }
        hash = PrimitiveHelper.stringHash(buffer.toString());
    }

    private void sort() {
        for (int i = (size - 1); i >= 0; i--) {
            for (int j = 1; j <= i; j++) {
                if (attributes[j - 1] > attributes[j]) {
                    long tempK = attributes[j - 1];
                    String tempV = values[j - 1];
                    attributes[j - 1] = attributes[j];
                    values[j - 1] = values[j];
                    attributes[j] = tempK;
                    values[j] = tempV;
                }
            }
        }
    }

    /**
     * Parse the query and return the complex FlatQuery object, containing the decomposition of keys/values
     */
    public static Query parseQuery(String query, KResolver p_resolver) {
        int cursor = 0;
        long currentKey = Constants.NULL_LONG;
        int lastElemStart = 0;
        Query flatQuery = new Query();
        while (cursor < query.length()) {
            if (query.charAt(cursor) == Constants.QUERY_KV_SEP) {
                if (lastElemStart != -1) {
                    currentKey = p_resolver.key(query.substring(lastElemStart, cursor));
                }
                lastElemStart = cursor + 1;
            } else if (query.charAt(cursor) == Constants.QUERY_SEP) {
                if (currentKey != Constants.NULL_LONG) {
                    flatQuery.add(currentKey, query.substring(lastElemStart, cursor));
                }
                currentKey = Constants.NULL_LONG;
                lastElemStart = cursor + 1;
            }
            cursor++;
        }
        //insert the last element
        if (currentKey != Constants.NULL_LONG) {
            flatQuery.add(currentKey, query.substring(lastElemStart, cursor));
        }
        flatQuery.compute();
        return flatQuery;
    }


}
