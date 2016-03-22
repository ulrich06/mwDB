package org.mwdb.csv;

import org.mwdb.KNode;

public class CSVElement {

    public long time;

    private String[] names = new String[1];

    private Object[] values = new Object[1];

    private byte[] types = new byte[1];

    private int valuesIndex = 0;

    public void add(String name, Object value, byte type) {
        if (valuesIndex == values.length) {
            String[] tempNames = new String[names.length * 2];
            System.arraycopy(names, 0, tempNames, 0, names.length);
            names = tempNames;

            Object[] tempValues = new Object[values.length * 2];
            System.arraycopy(values, 0, tempValues, 0, values.length);
            values = tempValues;

            byte[] tempBytes = new byte[types.length * 2];
            System.arraycopy(types, 0, tempBytes, 0, types.length);
            types = tempBytes;

        }
        names[valuesIndex] = name;
        values[valuesIndex] = value;
        types[valuesIndex] = type;
        valuesIndex++;
    }

    void inject(KNode target) {
        for (int i = 0; i < valuesIndex; i++) {
            target.attSet(names[i], types[i], values[i]);
        }
    }

}
