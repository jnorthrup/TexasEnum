package poker.player;

import poker.eval.*;

import java.util.*;

public class CardMemory {
    private final IntArrayCircularBuffer cache;
    private static final int PLAY_LEN = Play.values().length;
    public static final int CACHE_SIZE = 7;
    private static final int MEM_SIZE = CACHE_SIZE * PLAY_LEN;
    private static final IntArrayCircularBuffer CLEAN = IntArrayCircularBuffer.allocate(MEM_SIZE);
    public int lastCount;
    public int ranks;
    public ArrayList<IntArrayCircularBuffer> runs = new ArrayList<IntArrayCircularBuffer>();
    public IntArrayCircularBuffer[] flush = {
            BuffUtil.allocate(7),
            BuffUtil.allocate(7),
            BuffUtil.allocate(7),
            BuffUtil.allocate(7),
    };
    public int flushidx;
    public IntArrayCircularBuffer straight = BuffUtil.EMPTY_SET;
    public boolean ace1st;
    public IntArrayCircularBuffer straightflush;

    public CardMemory() {
        cache = BuffUtil.allocate(MEM_SIZE);
        init();
    }

    public IntArrayCircularBuffer mpos(Play play) {
        final int position = CACHE_SIZE * play.ordinal();
        return cache.position(position).slice().mark().limit(play.minimum);
    }


    static {
        Arrays.fill(CLEAN.array(), (int) -1);
    }

    protected void init() {
        wipe();
    }

    public IntArrayCircularBuffer slice() {
        return cache.slice();
    }

    public IntArrayCircularBuffer duplicate() {
        return cache.duplicate();
    }

    public IntArrayCircularBuffer asReadOnlyBuffer() {
        return cache.duplicate();
    }

    public int get() {
        return cache.get();
    }

    public IntArrayCircularBuffer put(int b) {
        return cache.put(b);
    }

    public int get(int index) {
        return cache.get(index);
    }

    public IntArrayCircularBuffer put(int index, int b) {
        return cache.putAt(index, b);
    }

    public IntArrayCircularBuffer get(int[] dst, int offset, int length) {
        for (int i = 0; i < length; i++) {
            dst[offset + i] = cache.get(i);
        }
        return cache;
    }

    public IntArrayCircularBuffer get(int[] dst) {
        return get(dst, 0, dst.length);
    }

    public IntArrayCircularBuffer put(IntArrayCircularBuffer src) {
        while (src.hasRemaining()) {
            this.put(src.get());
        }
        return cache;
    }

    public IntArrayCircularBuffer put(int[] src, int offset, int length) {
        for (int i = 0; i < length; i++) {
            cache.put(src[offset + i]);
        }
        return cache;
    }

    public IntArrayCircularBuffer put(int[] src) {
        return put(src, 0, src.length);
    }

    public IntArrayCircularBuffer clear() {
        cache.clear();
        return cache;
    }

    public boolean isDirect() {
        return false;
    }

    public String toString() {
        return cache.toString();
    }

    public int hashCode() {
        return cache.hashCode();
    }


    public int compareTo(IntArrayCircularBuffer that) {
        return 0;
    }

    public IntArrayCircularBuffer mark() {
        return cache.mark();
    }

    public IntArrayCircularBuffer reset() {
        return cache.reset();
    }

    public boolean isReadOnly() {
        return false;
    }

    public boolean hasCacheEntry(Play play) {
        return mpos(play).get() != -1;
    }

    public void wipe() {

    }
}
