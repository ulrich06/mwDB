package org.mwg.importer;

import org.mwg.plugin.AbstractIterable;

import java.io.*;

class IterableLines extends AbstractIterable {

    private final Reader _reader;
    private final BufferedReader _buffer;

    IterableLines(String path) throws FileNotFoundException {
        File file = new File(path);
        if (file.exists()) {
            _reader = new FileReader(file);
        } else {
            _reader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(path));
        }
        _buffer = new BufferedReader(_reader);
    }

    @Override
    public Object next() {
        try {
            return _buffer.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close() {
        try {
            _buffer.close();
            _reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int estimate() {
        return -1;
    }
}


/*Task t = action(ActionReadFiles.READFILES_NAME,"smarthome").foreach(then(context -> {
                System.err.println(context.result());
                context.setResult(null);
            }));
            t.execute(g,null);

            Task t = .foreach(then(context -> {
                System.err.println(context.result());
                context.setResult(null);
            }));
            t.execute(g,null);*/
