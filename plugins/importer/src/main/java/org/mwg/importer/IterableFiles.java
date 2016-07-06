package org.mwg.importer;

import org.mwg.plugin.AbstractIterable;

import java.io.File;
import java.net.URL;

class IterableFiles extends AbstractIterable {
    private String[] _filesPath;
    private int _nextIndex;

    IterableFiles(String path) {
        File file = new File(path);
        if(!file.exists()) {
            URL url = this.getClass().getClassLoader().getResource(path);
            if(url == null) {
                throw new RuntimeException("File " + path + " does not exist and it is not present in resources directory.");
            }
            file = new File(url.getPath());
        }

        if(file.isDirectory()) {
            File[] listFiles = file.listFiles();
            _filesPath = new String[listFiles.length];
            for(int i=0;i<listFiles.length;i++) {
                _filesPath[i] = listFiles[i].getAbsolutePath();
            }
        } else {
            _filesPath = new String[] {file.getAbsolutePath()};
        }
    }

    @Override
    public Object next() {
        if(_nextIndex == _filesPath.length ) {
            return null;
        }
        return _filesPath[_nextIndex++];
    }

    @Override
    public void close() {
        _filesPath = null;
    }

    @Override
    public int estimate() {
        return -1;
    }
}
