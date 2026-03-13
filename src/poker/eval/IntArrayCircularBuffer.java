package poker.eval;

public final class IntArrayCircularBuffer {
    private final int[] buffer;
    public int head;
    public int tail;

    public IntArrayCircularBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.buffer = new int[capacity];
        this.head = 0;
        this.tail = 0;
    }

    public int capacity() {
        return buffer.length;
    }

    public int offer(int value) {
        buffer[tail] = value;
        tail = (tail + 1) % buffer.length;
        return value;
    }

    public int poll() {
        int value = buffer[head];
        head = (head + 1) % buffer.length;
        return value;
    }

    public int peek() {
        return buffer[head];
    }

    public int get(int index) {
        return buffer[(head + index) % buffer.length];
    }

    public int set(int index, int value) {
        int oldValue = buffer[(head + index) % buffer.length];
        buffer[(head + index) % buffer.length] = value;
        return oldValue;
    }

    public void clear() {
        head = 0;
        tail = 0;
    }

    public int[] toArray() {
        int len = tail >= head ? tail - head : buffer.length - head + tail;
        int[] result = new int[len];
        for (int i = 0; i < len; i++) {
            result[i] = buffer[(head + i) % buffer.length];
        }
        return result;
    }

    public void copyTo(int[] dst, int offset, int length) {
        for (int i = 0; i < length; i++) {
            dst[offset + i] = buffer[(head + i) % buffer.length];
        }
    }

    public static final int GB = 1024 * 1024 * 1024;
    public static final int INT_SIZE = 4;
    public static final int TWO_GB_INTS = (2 * GB) / INT_SIZE;

    public static IntArrayCircularBuffer allocateTwoGig() {
        return new IntArrayCircularBuffer(TWO_GB_INTS);
    }
}
