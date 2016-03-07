package org.mwdb.chunk.offheap;

import java.lang.reflect.Field;

/**
 * @ignore ts
 */
public class Unsafe {

    @SuppressWarnings("restriction")
    public static sun.misc.Unsafe getUnsafe() {
        try {

            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (sun.misc.Unsafe) theUnsafe.get(null);

        } catch (Exception e) {
            throw new RuntimeException("ERROR: unsafe operations are not available");
        }
    }

}
