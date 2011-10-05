package poker.eval;

import com.sun.org.apache.bcel.internal.generic.*;
import static poker.eval.Deck.*;
import static poker.eval.Face.*;
import static poker.eval.Play.*;
import poker.player.*;

import java.nio.*;
import java.util.*;


public class HoldemRules {


    private static final int ACE_ORD = ACE.ordinal();

    static public IntBuffer doHighCard(IntBuffer cards) {
        return (IntBuffer) ((IntBuffer) cards.rewind().mark()).slice().limit(1);
    }


    static private IntBuffer doMatchWithExclusion(final IntBuffer hand, final int limit, final int avoidFace) {
        hand.rewind().mark();
        int pFace = -1;
        final IntBuffer swap = BuffUtil.allocate(limit);
        IntBuffer res = null;
        while (hand.hasRemaining()) {
            final int card = hand.get();
            /*face*/
            final int face = card >>> 16;
            final int distance = face - pFace;

            if (distance != 0)
                swap.reset();
            else if (face == avoidFace) {
                swap.reset();
                continue;
            }
            pFace = face;
            swap.put(card);

            if (!swap.hasRemaining()) {
                res = (IntBuffer) swap.reset();
                break;
            }
        }
        return res;
    }

    static private IntBuffer doMatch(final IntBuffer cards, final int latchSize) {

        cards.rewind().mark();
        int pFace = -1;
        final IntBuffer swap = (IntBuffer) BuffUtil.allocate(latchSize).mark();
        while (cards.hasRemaining()) {
            final int card = cards.get();
            /*face*/
            final int face = card >>> 16;
            final int distance = face - pFace;

            if (distance != 0)
                swap.reset();
            pFace = face;
            swap.put(card);

            if (!swap.hasRemaining())
                return swap;
        }
        return null;
    }


    private static IntBuffer doFullHouse(IntBuffer hand, final CardMemory cardMemory) {
        IntBuffer triple = THREEOFAKIND.recognize(hand, cardMemory);//uses cache
        IntBuffer res = null;
        if (triple != null) {
            IntBuffer pair = doMatchWithExclusion((IntBuffer) hand.rewind().mark(), 2, triple.get(0));

            if (pair != null) {
                res = BuffUtil.allocate(5);
                triple.reset();
                res.put(triple);
                pair.reset();
                res.put(pair);
                res.reset();
            }
        }
        return res;
    }

    private static IntBuffer doPair(IntBuffer hand) {
        return fastMatch(hand, Play.PAIR);
    }

    private static IntBuffer doTrip(IntBuffer hand) {
        return fastMatch(hand, Play.THREEOFAKIND);
    }

    static private IntBuffer doTwoPair(IntBuffer cards, CardMemory cardMemory) {
        IntBuffer r1 = Play.PAIR.recognize(cards, cardMemory); //Recognize uses cache if possible.
        IntBuffer res = null;
        if (r1 != null) {
            /*face*/
            IntBuffer r2 = doMatchWithExclusion(cards, 2, r1.get(0) >>> 16);
            if (r2 != null) {
                res = BuffUtil.allocate(TWOPAIR.minimum);
                r1.rewind().mark();
                r2.rewind().mark();
                res.put(r1);
                res.put(r2);
                res.reset();
            }
        }
        return res;
    }

    static int compareBoat(IntBuffer boat1, IntBuffer boat2) {
        /*face*/
        /*face*/
        final int eq = (boat1.get(0) >>> 16) - (boat2.get(0) >>> 16);
        /*face*/
        /*face*/
        return eq != 0 ? eq : (boat1.get(3) >>> 16) - (boat2.get(3) >>> 16);
    }

    static private IntBuffer doQuads(IntBuffer hand) {
        return fastMatch(hand, FOUROFAKIND);
    }

    private static IntBuffer fastMatch(final IntBuffer hand, Play play) {
        IntBuffer res = null;
        int[] cards = new int[hand.limit()];
        hand.rewind().mark();

        hand.get(cards).rewind().mark();
        final int l = hand.limit();
        final int min = play.minimum;
        for (int i = min - 1; i < l; i++) {
            final int card = cards[i];
            /*face*/
            final int face = card >>> 16;
            final int trail = i - min + 1;
            /*face*/
            final int preface = cards[trail] >>> 16;
            if (face == preface) {
                res = IntBuffer.wrap(Arrays.copyOfRange(cards, trail, i + 1));
                break;
            }
        }
        return res;
    }

    static private IntBuffer doFlush(IntBuffer hand) {

        final IntBuffer[] sbuf = new IntBuffer[SUITS_LEN];
        for (int i = 0; i < sbuf.length; i++)
            sbuf[i] = BuffUtil.allocate(FLUSH.minimum);


        int suit;
        IntBuffer res = null;
        hand.reset();
        while (hand.hasRemaining()) {
            int card = hand.get();
            /*suit*/
            suit = card & 0x3;
            sbuf[suit].put(card);
            if (!sbuf[suit].hasRemaining()) {
                res = sbuf[suit];
                break;
            }
        }

        if (res == null)
            for (IntBuffer buffer : sbuf) {
                if (buffer.position() > 4) {
                    res = (IntBuffer) buffer.reset();
                    break;
                }
            }
        return res;


    }

    static private IntBuffer doStrait(IntBuffer cards) {
        int pFace = DECK_SIZE + 1;
        final IntBuffer swap = BuffUtil.allocate(5);
        cards.reset();
        IntBuffer res = null;
        while (cards.hasRemaining()) {
            final int card = cards.get();
            /*face*/
            int face = card >>> 16;
            final int distance = face - pFace;

            if (distance != 0) {
                pFace = face;
                if (distance != 1)
                    swap.reset();
                swap.put(card);
                if (!swap.hasRemaining()) {
                    res = swap;
                    break;
                }
                if (swap.remaining() + cards.position() < 5) break;
            }
        }
        return res;
    }


    /**
     * @param cards
     * @param cardMemory
     * @return
     */
    @SuppressWarnings({"StatementWithEmptyBody"})
    static private IntBuffer doStraitFlush(IntBuffer cards, CardMemory cardMemory) {
        int idx = 0;
        IntBuffer flush = null;
        IntBuffer straight;
        final int clim = cards.limit();
        do {
            cards.position(idx).mark();
            idx++;
            straight = STRAIGHT.recognize2(cards, cardMemory);

            if (straight != null) {
                straight.rewind().mark();
                if (!cardMemory.hasCacheEntry(STRAIGHT))
                    cardMemory.mpos(STRAIGHT).put(straight);


                flush = (IntBuffer) FLUSH.recognize2((IntBuffer) straight.reset(), cardMemory);
                if (flush != null)
                    if (!cardMemory.hasCacheEntry(FLUSH))
                        cardMemory.mpos(FLUSH).put(((IntBuffer) flush.reset()));
            }
        } while (5 < clim - idx && (flush == null) != (straight == null)); /*
            this must avoid the cache due to 6-card and 7-card
            flushes being higher than the straight */
        return flush;
    }

    static IntBuffer doRoyalStraitFlush(IntBuffer hand, CardMemory memory) {

        if (memory.ace1st) {

            final IntBuffer swap = STRAIGHTFLUSH.recognize(hand, memory);

            if (swap != null && ACE_ORD == ((IntBuffer) swap.reset()).get(0))
                return swap;
        }
        return null;
    }                              

}