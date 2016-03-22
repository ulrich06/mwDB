package org.mwdb;

import java.io.*;
import java.util.StringTokenizer;

public class CSVImporter {

    private final String COMMA = ",";
    private String SEP;
    private KTimeExtractor timeExtractor;

    public CSVImporter() {
        SEP = COMMA;
    }

    public void setSeparator(String newSeparator){
        SEP = newSeparator;
    }

    public void singleNodeImport(File csvFile, KNode targetNode, KCallback<Boolean> callback) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(csvFile));
        String header = reader.readLine();
        if (header == null) {
            if (callback != null) {
                callback.on(false);
            }
        } else {
            StringTokenizer st = new StringTokenizer(header, SEP);
            String[] headers = new String[st.countTokens()];
            for (int i = 0; i < headers.length; i++) {
                headers[i] = st.nextToken();
            }

            String next


            long extractedTime = timeExtractor.time(headers)


        }
    }

}
