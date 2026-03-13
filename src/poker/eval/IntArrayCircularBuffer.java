package poker.eval;

public final class IntArrayCircularBuffer {
    private final int[] buffer;
    private final int capacity;
    private int head;
    private int tail;
    private int count;

    public IntArrayCircularBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.capacity = capacity;
        this.buffer = new int[capacity];
        this.head = 0;
        this.tail = 0;
        this.count = 0;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public boolean isFull() {
        return count == capacity;
    }

    public int size() {
        return count;
    }

    public int capacity() {
        return capacity;
    }

    public int offer(int value) {
        if (count == capacity) {
            return -1;
        }
        buffer[tail] = value;
        tail = (tail + 1) % capacity;
        count++;
        return value;
    }

    public int poll() {
        if (count == 0) {
            return -1;
        }
        int value = buffer[head];
        head = (head + 1) % capacity;
        count--;
        return value;
    }

    public int peek() {
        if (count == 0) {
            return -1;
        }
        return buffer[head];
    }

    public int get(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + count);
        }
        return buffer[(head + index) % capacity];
    }

    public int set(int index, int value) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + count);
        }
        int oldValue = buffer[(head + index) % capacity];
        buffer[(head + index) % capacity] = value;
        return oldValue;
    }

    public void clear() {
        head = 0;
        tail = 0;
        count = 0;
    }

    public int[] toArray() {
        int[] result = new int[count];
        for (int i = 0; i < count; i++) {
            result[i] = buffer[(head + i) % capacity];
        }
        return result;
    }

    public void copyTo(int[] dst, int offset, int length) {
        if (length > count) {
            length = count;
        }
        for (int i = 0; i < length; i++) {
            dst[offset + i] = buffer[(head + i) % capacity];
        }
    }

    public static final int GB = 1024 * 1024 * 1024;
    public static final int INT_SIZE = 4;
    public static final int TWO_GB_INTS = (2 * GB) / INT_SIZE;

    public static IntArrayCircularBuffer allocateTwoGig() {
        return new IntArrayCircularBuffer(TWO_GB_INTS);
    }
}
