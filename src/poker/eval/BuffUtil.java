package poker.eval;

import java.nio.*;
import java.util.logging.*;

/**
 * User: jim
 * Date: Oct 6, 2007
 * Time: 3:10:32 AM
 */
public class BuffUtil {

    public static final IntBuffer EMPTY_SET = ByteBuffer.allocate(0).asReadOnlyBuffer().asIntBuffer();

    public static IntBuffer allocate(int size) {
        if (size == 0) return EMPTY_SET;
        IntBuffer buf = IntBuffer.allocate(size);
        buf.limit(0); // Start empty like the slab did
        return buf;
    }

    final public static boolean isTest() {
        return true;
    }
}
