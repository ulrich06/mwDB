package org.mwdb.chunk;

import org.mwdb.struct.KLongLongMap;

public interface KWorldOrderChunk extends KChunk, KLongLongMap {

    long magic();

}
