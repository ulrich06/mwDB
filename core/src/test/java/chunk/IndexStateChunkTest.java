package chunk;

import org.junit.Assert;
import org.junit.Test;
import org.mwdb.Constants;
import org.mwdb.chunk.KIndexStateChunk;
import org.mwdb.chunk.heap.HeapIndexStateChunk;

public class IndexStateChunkTest {

    @Test
    public void testHeap() {
        test(new HeapIndexStateChunk(-1, -1, -1, null));
    }

    private void test(KIndexStateChunk chunk) {

        Assert.assertTrue(!chunk.contains("name"));
        Assert.assertTrue(chunk.getValue("name") == Constants.NULL_LONG);
        chunk.put("name", 0);
        Assert.assertTrue(chunk.contains("name"));

        Assert.assertTrue(!chunk.contains("value"));
        chunk.put("value", 1);
        Assert.assertTrue(chunk.contains("value"));

        Assert.assertTrue(0 == chunk.getValue("name"));
        Assert.assertTrue(1 == chunk.getValue("value"));

    }

}
