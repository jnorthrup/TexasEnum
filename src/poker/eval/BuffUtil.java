package poker.eval;

import java.util.logging.*;

/**
 * User: jim
 * Date: Oct 6, 2007
 * Time: 3:10:32 AM
 * 
 * Zero-cost static buffer utilities.
 * 128 bytes = 2 cache lines (64 bytes each).
 */
public final class BuffUtil {

    // Static pinned memory for deck and table (52 + 6 cards)
    public static final byte[] DECK_AND_TABLE = new byte[128];

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
    
    // Allocate int array
    public static int[] allocate(int size) {
        return new int[size];
    }
}
