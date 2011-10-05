package poker.player;

import poker.eval.*;

import java.nio.*;
import java.util.*;

/**
 * cache holds prior recognized hands.
 * <p/>
 * mpos(PLAY) method sets up the cache with a mark and an offset.
 * <p/>
 * cache slotws with no prior recognition begin life with -1 as the first card
 */

public class CardMemory {
    private final IntBuffer cache;
    private static final int PLAY_LEN = Play.values().length;
    public static final int CACHE_SIZE = 7;
    private static final int MEM_SIZE = CACHE_SIZE * PLAY_LEN;
    private static final IntBuffer CLEAN = IntBuffer.allocate(MEM_SIZE);
    public int lastCount;
    public int ranks;
    public ArrayList<IntBuffer> runs = new ArrayList<IntBuffer>();
    public IntBuffer[] flush = {
            BuffUtil.allocate(7),
            BuffUtil.allocate(7),
            BuffUtil.allocate(7),
            BuffUtil.allocate(7),
    };
    public int flushidx;
    public IntBuffer straight =BuffUtil.EMPTY_SET;
    public boolean ace1st;
    public IntBuffer straightflush;

    public CardMemory() {
        cache = BuffUtil.allocate(MEM_SIZE);
        init();
    }

    /**
     * positions and mark's the cache beginning at the slot for the play
     *
     * @param play
     * @return
     */
    public IntBuffer mpos(Play play) {
        final int position = CACHE_SIZE * play.ordinal();
        return (IntBuffer) ((IntBuffer) cache.position(position))
                .slice()
                .mark()
                .limit(play.minimum);

    }


    static {
        Arrays.fill(CLEAN.array(), (int) -1);
    }

    protected void init() {
        wipe();
    }

    public IntBuffer slice() {
        return cache.slice();
    }

    public IntBuffer duplicate() {
        return cache.duplicate();
    }

    public IntBuffer asReadOnlyBuffer() {
        return cache.asReadOnlyBuffer();
    }

    public int get() {
        return cache.get();
    }

    public IntBuffer put(int b) {
        return cache.put(b);
    }

    public int get(int index) {
        return cache.get(index);
    }

    public IntBuffer put(int index, int b) {
        return cache.put(index, b);
    }

    public IntBuffer get(int[] dst, int offset, int length) {
        return cache.get(dst, offset, length);
    }

    public IntBuffer get(int[] dst) {
        return cache.get(dst);
    }

    public IntBuffer put(IntBuffer src) {
        return cache.put(src);
    }

    public IntBuffer put(int[] src, int offset, int length) {
        return cache.put(src, offset, length);
    }

    public IntBuffer put(int[] src) {
        return cache.put(src);
    }

    public boolean hasArray() {
        return cache.hasArray();
    }

    public int[] array() {
        return cache.array();
    }

    public int arrayOffset() {
        return cache.arrayOffset();
    }

    public IntBuffer compact() {
        return cache.compact();
    }

    public boolean isDirect() {
        return cache.isDirect();
    }

    public String toString() {
        return cache.toString();
    }

    public int hashCode() {
        return cache.hashCode();
    }


    public int compareTo(IntBuffer that) {
        return cache.compareTo(that);
    }

    public IntBuffer mark() {
        return (IntBuffer) cache.mark();
    }

    public IntBuffer reset() {
        return (IntBuffer) cache.reset();
    }

    public boolean isReadOnly() {
        return cache.isReadOnly();
    }

    public boolean hasCacheEntry(Play play) {
        return mpos(play).get() != -1;
    }

    public void wipe() {

    }
}
