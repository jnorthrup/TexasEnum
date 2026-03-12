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
        return IntBuffer.allocate(size).mark();
    }

    final public static boolean isTest() {
        return true;
    }
}
