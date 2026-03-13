package poker.eval;

public final class IntArrayCircularBuffer {
    private final int[] buffer;
    public int head;
    public int tail;
    public int position;
    public int limit;
    private int mark = -1;

    public IntArrayCircularBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.buffer = new int[capacity];
        this.head = 0;
        this.tail = 0;
        this.position = 0;
        this.limit = capacity;
    }

    public IntArrayCircularBuffer(int[] array) {
        this.buffer = array;
        this.head = 0;
        this.tail = 0;
        this.position = 0;
        this.limit = array.length;
    }

    public int capacity() {
        return buffer.length;
    }

    public IntArrayCircularBuffer mark() {
        mark = position;
        return this;
    }

    public IntArrayCircularBuffer reset() {
        if (mark >= 0) {
            position = mark;
        }
        return this;
    }

    public IntArrayCircularBuffer rewind() {
        position = 0;
        mark = -1;
        return this;
    }

    public IntArrayCircularBuffer position(int pos) {
        this.position = pos;
        return this;
    }

    public IntArrayCircularBuffer limit(int lim) {
        this.limit = lim;
        return this;
    }

    public boolean hasRemaining() {
        return position < limit;
    }

    public int remaining() {
        return limit - position;
    }

    public IntArrayCircularBuffer flip() {
        limit = position;
        position = 0;
        mark = -1;
        return this;
    }

    public int position() {
        return position;
    }

    public int limit() {
        return limit;
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

    public int get() {
        return buffer[head + position];
    }

    public int get(int index) {
        return buffer[(head + index) % buffer.length];
    }

    public IntArrayCircularBuffer put(int value) {
        buffer[head + position] = value;
        position++;
        return this;
    }

    public IntArrayCircularBuffer putAt(int index, int value) {
        buffer[(head + index) % buffer.length] = value;
        return this;
    }

    public IntArrayCircularBuffer put(IntArrayCircularBuffer src) {
        while (src.hasRemaining()) {
            this.put(src.get());
        }
        return this;
    }

    public int set(int index, int value) {
        int oldValue = buffer[(head + index) % buffer.length];
        buffer[(head + index) % buffer.length] = value;
        return oldValue;
    }

    public void clear() {
        head = 0;
        tail = 0;
        position = 0;
        limit = buffer.length;
        mark = -1;
    }

    public IntArrayCircularBuffer slice() {
        IntArrayCircularBuffer result = new IntArrayCircularBuffer(buffer);
        result.head = this.head + this.position;
        result.position = 0;
        result.limit = this.limit;
        return result;
    }

    public IntArrayCircularBuffer duplicate() {
        IntArrayCircularBuffer result = new IntArrayCircularBuffer(buffer);
        result.head = this.head;
        result.tail = this.tail;
        result.position = this.position;
        result.limit = this.limit;
        result.mark = this.mark;
        return result;
    }

    public int[] toArray() {
        int len = tail >= head ? tail - head : buffer.length - head + tail;
        int[] result = new int[len];
        for (int i = 0; i < len; i++) {
            result[i] = buffer[(head + i) % buffer.length];
        }
        return result;
    }

    public int[] array() {
        return buffer;
    }

    public void copyTo(int[] dst, int offset, int length) {
        for (int i = 0; i < length; i++) {
            dst[offset + i] = buffer[(head + i) % buffer.length];
        }
    }

    public static IntArrayCircularBuffer allocate(int capacity) {
        return new IntArrayCircularBuffer(capacity);
    }

    public static final int GB = 1024 * 1024 * 1024;
    public static final int INT_SIZE = 4;
    public static final int TWO_GB_INTS = (2 * GB) / INT_SIZE;

    public static IntArrayCircularBuffer allocateTwoGig() {
        return new IntArrayCircularBuffer(TWO_GB_INTS);
    }
}
