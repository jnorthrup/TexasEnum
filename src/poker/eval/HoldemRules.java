package poker.eval;

import static poker.eval.Deck.*;
import static poker.eval.Face.*;
import static poker.eval.Play.*;
import poker.player.*;

import java.util.*;

public class HoldemRules {

    private static final int ACE_ORD = ACE.ordinal();

    static public IntArrayCircularBuffer doHighCard(IntArrayCircularBuffer cards) {
        return cards.rewind().mark().slice().limit(1);
    }

    static IntArrayCircularBuffer doMatchWithExclusion(final IntArrayCircularBuffer hand, final int limit, final int avoidFace) {
        hand.rewind().mark();
        int pFace = -1;
        final IntArrayCircularBuffer swap = BuffUtil.allocate(limit);
        IntArrayCircularBuffer res = null;
        while (hand.hasRemaining()) {
            final int card = hand.get();
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
                res = swap.reset();
                break;
            }
        }
        return res;
    }

    static private IntArrayCircularBuffer doMatch(final IntArrayCircularBuffer cards, final int latchSize) {
        cards.rewind().mark();
        int pFace = -1;
        final IntArrayCircularBuffer swap = IntArrayCircularBuffer.allocate(latchSize).mark();
        while (cards.hasRemaining()) {
            final int card = cards.get();
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

    static IntArrayCircularBuffer doFullHouse(IntArrayCircularBuffer hand, final CardMemory cardMemory) {
        IntArrayCircularBuffer triple = THREEOFAKIND.recognize(hand, cardMemory);
        IntArrayCircularBuffer res = null;
        if (triple != null) {
            IntArrayCircularBuffer pair = doMatchWithExclusion(hand.rewind().mark(), 2, triple.get(0));
            if (pair != null) {
                res = IntArrayCircularBuffer.allocate(5);
                triple.reset();
                res.put(triple);
                pair.reset();
                res.put(pair);
                res.reset();
            }
        }
        return res;
    }

    static IntArrayCircularBuffer doPair(IntArrayCircularBuffer hand) {
        return fastMatch(hand, Play.PAIR);
    }

    static IntArrayCircularBuffer doTrip(IntArrayCircularBuffer hand) {
        return fastMatch(hand, Play.THREEOFAKIND);
    }

    static IntArrayCircularBuffer doTwoPair(IntArrayCircularBuffer cards, CardMemory cardMemory) {
        IntArrayCircularBuffer r1 = Play.PAIR.recognize(cards, cardMemory);
        IntArrayCircularBuffer res = null;
        if (r1 != null) {
            IntArrayCircularBuffer r2 = doMatchWithExclusion(cards, 2, r1.get(0) >>> 16);
            if (r2 != null) {
                res = IntArrayCircularBuffer.allocate(TWOPAIR.minimum);
                r1.rewind().mark();
                r2.rewind().mark();
                res.put(r1);
                res.put(r2);
                res.reset();
            }
        }
        return res;
    }

    static int compareBoat(IntArrayCircularBuffer boat1, IntArrayCircularBuffer boat2) {
        final int eq = (boat1.get(0) >>> 16) - (boat2.get(0) >>> 16);
        return eq != 0 ? eq : (boat1.get(3) >>> 16) - (boat2.get(3) >>> 16);
    }

    static IntArrayCircularBuffer doQuads(IntArrayCircularBuffer hand) {
        return fastMatch(hand, FOUROFAKIND);
    }

    private static IntArrayCircularBuffer fastMatch(final IntArrayCircularBuffer hand, Play play) {
        IntArrayCircularBuffer res = null;
        int[] cards = new int[hand.limit()];
        hand.rewind().mark();
        hand.get(cards);
        hand.rewind().mark();
        final int l = hand.limit();
        final int min = play.minimum;
        for (int i = min - 1; i < l; i++) {
            final int card = cards[i];
            final int face = card >>> 16;
            final int trail = i - min + 1;
            final int preface = cards[trail] >>> 16;
            if (face == preface) {
                res = IntArrayCircularBuffer.allocate(i - trail + 1);
                for (int j = trail; j <= i; j++) {
                    res.put(cards[j]);
                }
                break;
            }
        }
        return res;
    }

    static IntArrayCircularBuffer doFlush(IntArrayCircularBuffer hand) {
        final IntArrayCircularBuffer[] sbuf = new IntArrayCircularBuffer[SUITS_LEN];
        for (int i = 0; i < sbuf.length; i++)
            sbuf[i] = IntArrayCircularBuffer.allocate(FLUSH.minimum);

        int suit;
        IntArrayCircularBuffer res = null;
        hand.reset();
        while (hand.hasRemaining()) {
            int card = hand.get();
            suit = card & 0x3;
            sbuf[suit].put(card);
            if (!sbuf[suit].hasRemaining()) {
                res = sbuf[suit];
                break;
            }
        }

        if (res == null) {
            for (IntArrayCircularBuffer buffer : sbuf) {
                if (buffer.position() > 4) {
                    res = buffer.reset();
                    break;
                }
            }
        }
        return res;
    }

    static IntArrayCircularBuffer doStrait(IntArrayCircularBuffer cards) {
        int pFace = DECK_SIZE + 1;
        final IntArrayCircularBuffer swap = IntArrayCircularBuffer.allocate(5);
        cards.reset();
        IntArrayCircularBuffer res = null;
        while (cards.hasRemaining()) {
            final int card = cards.get();
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

    @SuppressWarnings({"StatementWithEmptyBody"})
    static private IntArrayCircularBuffer doStraitFlush(IntArrayCircularBuffer cards, CardMemory cardMemory) {
        int idx = 0;
        IntArrayCircularBuffer flush = null;
        IntArrayCircularBuffer straight;
        final int clim = cards.limit();
        do {
            cards.position(idx).mark();
            idx++;
            straight = STRAIGHT.recognize2(cards, cardMemory);

            if (straight != null) {
                straight.rewind().mark();
                if (!cardMemory.hasCacheEntry(STRAIGHT))
                    cardMemory.mpos(STRAIGHT).put(straight);

                flush = FLUSH.recognize2(straight.reset(), cardMemory);
                if (flush != null)
                    if (!cardMemory.hasCacheEntry(FLUSH))
                        cardMemory.mpos(FLUSH).put(flush.reset());
            }
        } while (5 < clim - idx && (flush == null) != (straight == null));
        return flush;
    }

    static IntArrayCircularBuffer doRoyalStraitFlush(IntArrayCircularBuffer hand, CardMemory memory) {
        if (memory.ace1st) {
            final IntArrayCircularBuffer swap = STRAIGHTFLUSH.recognize(hand, memory);
            if (swap != null && ACE_ORD == swap.reset().get(0))
                return swap;
        }
        return null;
    }
}
