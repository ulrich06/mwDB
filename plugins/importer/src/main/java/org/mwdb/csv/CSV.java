package org.mwdb.csv;

import org.mwdb.KCallback;
import org.mwdb.KNode;

import java.io.*;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CSV {

    private final String COMMA = ",";
    private String SEP;
    private int groupSize = 1;
    private CSVTransform transform;

    public CSV() {
        SEP = COMMA;
        transform = new DefaultTransform();
    }

    public void setSeparator(String newSeparator) {
        SEP = newSeparator;
    }

    public void groupBy(int p_groupSize) {
        this.groupSize = p_groupSize;
    }

    public void usingTransform(CSVTransform p_transform) {
        transform = p_transform;
    }

    public void singleNodeImport(File csvFile, KNode targetNode, KCallback<Boolean> callback) throws IOException {

        int ligneIndex = 0;
        BufferedReader reader = new BufferedReader(new FileReader(csvFile));

        String header = reader.readLine();
        ligneIndex++;
        if (header == null) {
            if (callback != null) {
                callback.on(false);
            }
        } else {

            AtomicLong counter = new AtomicLong(0);
            AtomicLong waiter = new AtomicLong(0);
            AtomicBoolean isfinished = new AtomicBoolean(false);
            KCallback injectCallback = new KCallback() {
                @Override
                public void on(Object result) {
                    long current = counter.incrementAndGet();
                    if (isfinished.get() && waiter.get() == current) {
                        if (callback != null) {
                            callback.on(true);
                        }
                    }
                }
            };

            StringTokenizer st = new StringTokenizer(header, SEP);
            String[] headers = new String[st.countTokens()];
            for (int i = 0; i < headers.length; i++) {
                headers[i] = st.nextToken();
            }
            String[][] values = new String[groupSize][];
            int valueIndex = 0;
            String valueLine = reader.readLine();
            while (valueLine != null) {
                if (valueIndex == values.length) {
                    //process result
                    waiter.incrementAndGet();
                    inject(targetNode, transform.transform(headers, values), injectCallback);

                    values = new String[groupSize][];
                    valueIndex = 0;
                }
                StringTokenizer loopSt = new StringTokenizer(valueLine, SEP);
                String[] loopValue = new String[loopSt.countTokens()];
                for (int i = 0; i < loopValue.length; i++) {
                    loopValue[i] = loopSt.nextToken();
                }
                values[valueIndex] = loopValue;
                valueIndex++;
                valueLine = reader.readLine();
                ligneIndex++;
                if (ligneIndex % 100_000 == 0) {
                    System.out.println(ligneIndex + " lines imported");
                }
            }
            reader.close();
            isfinished.set(true);
            //process old result
            if (valueIndex != 0) {
                waiter.incrementAndGet();
                inject(targetNode, transform.transform(headers, values), injectCallback);
            }
        }
    }

    private void inject(KNode target, CSVElement[] elements, KCallback callback) {
        for (int i = 0; i < elements.length; i++) {
            final CSVElement current = elements[i];
            target.graph().lookup(target.world(), elements[i].time, target.id(), new KCallback<KNode>() {
                @Override
                public void on(KNode result) {
                    if (result != null) {
                        current.inject(result);
                        result.free();
                        callback.on(null);
                    } else {
                        System.err.println("Node not found !!!");
                    }
                }
            });
        }
    }


}
