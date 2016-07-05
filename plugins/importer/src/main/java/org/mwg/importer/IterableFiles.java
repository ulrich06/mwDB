package org.mwg.importer;

import org.mwg.plugin.AbstractIterable;

import java.io.File;
import java.net.URL;

class IterableFiles extends AbstractIterable {
    private File[] _files;
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
            _files = file.listFiles();
        } else {
            _files = new File[] {file};
        }
    }

    @Override
    public Object next() {
        if(_nextIndex == _files.length ) {
            return null;
        }
        return _files[_nextIndex++];
    }

    @Override
    public void close() {
        _files = null;
    }

    @Override
    public int estimate() {
        return -1;
    }
}
