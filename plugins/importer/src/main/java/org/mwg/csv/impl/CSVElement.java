package org.mwg.csv.impl;

import org.mwg.Node;
import org.mwg.Type;

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

    public void inject(Node target, boolean verbose) {
        if (verbose) {
            System.out.print("<Inject(" + target.world() + "," + target.time() + "," + target.id() + ")>");
        }
        for (int i = 0; i < valuesIndex; i++) {
            if (i != 0) {
                if (verbose) {
                    System.out.print("|");
                }
            }
            if (verbose) {
                System.out.print(names[i] + ":" + Type.typeName(types[i]) + "->" + values[i]);
            }
            target.set(names[i], types[i], values[i]);
        }
        if (verbose) {
            System.out.println("</Inject>");
        }
    }

    public void verbosePrint() {
        System.out.print("<TransLine>");
        for (int i = 0; i < valuesIndex; i++) {
            if (i != 0) {
                System.out.print("|");
            }
            System.out.print(names[i] + ":" + Type.typeName(types[i]) + "->" + values[i]);
        }
        System.out.println("</TransLine>");
    }

}
