package poker.eval;

public class BuffUtil {

    public static final IntArrayCircularBuffer EMPTY_SET = IntArrayCircularBuffer.allocate(1);

    public static IntArrayCircularBuffer allocate(int size) {
        if (size == 0) return EMPTY_SET;
        return IntArrayCircularBuffer.allocate(size).mark();
    }

    final public static boolean isTest() {
        return true;
    }
}
