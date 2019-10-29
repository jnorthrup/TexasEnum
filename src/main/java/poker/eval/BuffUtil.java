package poker.eval;

import java.nio.*;
import java.util.logging.*;

/**
 * User: jim
 * Date: Oct 6, 2007
 * Time: 3:10:32 AM
 */
public class BuffUtil {

    static IntBuffer DIRECT_HEAP;
    private static final int MEGS = 1024 * 1024;
    final private static int initialCapacity = 2 * MEGS;
    final private static boolean test = true;
    public static final IntBuffer EMPTY_SET = ByteBuffer.allocate(0).asReadOnlyBuffer().asIntBuffer();

    static int size = initialCapacity;

    static {
        init();
    }

    private static void init() {

        IntBuffer buffer = null;
        while (buffer == null)
            try {

                if (isDirect())
                    buffer = (IntBuffer) ByteBuffer.allocateDirect(size).asIntBuffer().limit(0);
                else
                    buffer = (IntBuffer) ByteBuffer.allocate(size).asIntBuffer().limit(0);

                DIRECT_HEAP = buffer;
                Logger.getAnonymousLogger().info("Heap allocated at " + size / MEGS + " megs");
                size *= 2;

            } catch (IllegalArgumentException e) {
                size = Math.max(16 * MEGS, size / 2);
                System.gc();
            } catch (OutOfMemoryError e) {
                size = Math.max(16 * MEGS, size / 2);
                System.gc();
            }
    }

    public static IntBuffer allocate(int size) {
        if (size == 0) return EMPTY_SET;

        if (isTest()) return (IntBuffer) IntBuffer.allocate(size).mark();
        try {
            DIRECT_HEAP.limit(DIRECT_HEAP.limit() + size);
        } catch (IllegalArgumentException e) {
            init();
            return allocate(size);
        }
        final IntBuffer ret = (IntBuffer) DIRECT_HEAP.slice().limit(size).mark();
        DIRECT_HEAP.position(DIRECT_HEAP.limit());
        return ret;
    }

    public static boolean isDirect() {
        return false;
    }

    final public static boolean isTest() {
        return test;
    }

    public static void setTest(boolean test) {
//        BuffUtil.test = test;
    }
}
 