package poker.player;

import poker.eval.*;

import java.util.*;

/**
 * cache holds prior recognized hands.
 * <p/>
 * mpos(PLAY) method sets up the cache with a mark and an offset.
 * <p/>
 * cache slotws with no prior recognition begin life with -1 as the first card
 */

public class CardMemory {
    private final int[] cache;
    private static final int PLAY_LEN = Play.values().length;
    public static final int CACHE_SIZE = 7;
    private static final int MEM_SIZE = CACHE_SIZE * PLAY_LEN;
    private static final int[] CLEAN = new int[MEM_SIZE];
    public int lastCount;
    public int ranks;
    public ArrayList<int[]> runs = new ArrayList<int[]>();
    public int[][] flush = {
            new int[7],
            new int[7],
            new int[7],
            new int[7],
    };
    public int flushidx;
    public int[] straight = new int[0];
    public boolean ace1st;
    public int[] straightflush;

    public CardMemory() {
        cache = new int[MEM_SIZE];
        init();
    }

    /**
     * positions and mark's the cache beginning at the slot for the play
     *
     * @param play
     * @return
     */
    public int[] mpos(Play play) {
        final int position = CACHE_SIZE * play.ordinal();
        int[] slice = new int[play.minimum];
        System.arraycopy(cache, position, slice, 0, play.minimum);
        return slice;
    }


    static {
        Arrays.fill(CLEAN, -1);
    }

    protected void init() {
        wipe();
    }

    public int get() {
        return cache[0];
    }

    public void put(int b) {
        cache[0] = b;
    }

    public int get(int index) {
        return cache[index];
    }

    public void put(int index, int b) {
        cache[index] = b;
    }

    public void get(int[] dst, int offset, int length) {
        System.arraycopy(cache, 0, dst, offset, length);
    }

    public void get(int[] dst) {
        System.arraycopy(cache, 0, dst, 0, dst.length);
    }

    public void put(int[] src, int offset, int length) {
        System.arraycopy(src, offset, cache, 0, length);
    }

    public void put(int[] src) {
        System.arraycopy(src, 0, cache, 0, src.length);
    }

    public boolean hasArray() {
        return true;
    }

    public int[] array() {
        return cache;
    }

    public int arrayOffset() {
        return 0;
    }

    public String toString() {
        return Arrays.toString(cache);
    }

    public int hashCode() {
        return Arrays.hashCode(cache);
    }

    public int compareTo(int[] that) {
        for (int i = 0; i < cache.length && i < that.length; i++) {
            int cmp = Integer.compare(cache[i], that[i]);
            if (cmp != 0) return cmp;
        }
        return Integer.compare(cache.length, that.length);
    }

    public boolean hasCacheEntry(Play play) {
        int[] pos = mpos(play);
        return pos.length > 0 && pos[0] != -1;
    }

    public void wipe() {

    }
}
