package poker.eval;

import java.nio.*;
import java.util.logging.*;

/**
 * User: jim
 * Date: Oct 6, 2007
 * Time: 3:10:32 AM
 * 
 * Uses a static pinned byte array for IntBuffer operations.
 * 128 bytes = 2 cache lines, holds deck (52 cards) + table (6 cards) + padding.
 */
public class BuffUtil {

    // 128 bytes = 2 cache lines (64 bytes each)
    // Holds deck (52 cards) + table (6 cards) + 70 bytes padding
    public static final byte[] DECK_AND_TABLE = new byte[128];
    
    // Static pinned buffer backed by DECK_AND_TABLE
    private static final ByteBuffer PINNED_BUFFER = ByteBuffer.wrap(DECK_AND_TABLE);
    
    // Empty set - backed by zero-length slice of pinned buffer
    public static final IntBuffer EMPTY_SET = PINNED_BUFFER.slice().limit(0).asReadOnlyBuffer().asIntBuffer();

    // Allocate a byte array of the given size
    public static byte[] allocateBytes(int size) {
        if (size == 0) return new byte[0];
        return new byte[size];
    }

    // Allocate an IntBuffer backed by a byte array
    public static IntBuffer allocate(int size) {
        if (size == 0) return EMPTY_SET;
        byte[] bytes = new byte[size * 4];  // 4 bytes per int
        return ByteBuffer.wrap(bytes).asIntBuffer().mark();
    }

    // Card encoding: (face << 2) | suit
    // face: 0-12 (4 bits), suit: 0-3 (2 bits)
    public static int encodeCard(int face, int suit) {
        return (face << 2) | suit;
    }

    public static int decodeFace(int cardByte) {
        return cardByte >>> 2;
    }

    public static int decodeSuit(int cardByte) {
        return cardByte & 0x3;
    }

    final public static boolean isTest() {
        return true;
    }
}
