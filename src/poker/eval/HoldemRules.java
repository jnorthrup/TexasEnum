package poker.eval;

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


    static IntBuffer doMatchWithExclusion(final IntBuffer hand, final int limit, final int avoidFace) {
        hand.rewind().mark();
        int pFace = -1;
        final IntBuffer swap = BuffUtil.allocate(limit);
        IntBuffer res = null;
        while (hand.hasRemaining()) {
            final int card = hand.get();
            /*face*/
            final int face = CardUtil.face(card);
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
            final int face = CardUtil.face(card);
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


    static IntBuffer doFullHouse(IntBuffer hand, final CardMemory cardMemory) {
        IntBuffer triple = THREEOFAKIND.recognize(hand, cardMemory);//uses cache
        IntBuffer res = null;
        if (triple != null) {
            IntBuffer pair = doMatchWithExclusion((IntBuffer) hand.rewind().mark(), 2, CardUtil.face(triple.get(0)));

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

    static IntBuffer doPair(IntBuffer hand) {
        return fastMatch(hand, Play.PAIR);
    }

    static IntBuffer doTrip(IntBuffer hand) {
        return fastMatch(hand, Play.THREEOFAKIND);
    }

    static IntBuffer doTwoPair(IntBuffer cards, CardMemory cardMemory) {
        IntBuffer r1 = Play.PAIR.recognize(cards, cardMemory); //Recognize uses cache if possible.
        if (r1 == null) return null;
        // find second pair excluding the first
        IntBuffer r2 = doMatchWithExclusion(cards, 2, CardUtil.face(r1.get(0)));
        if (r2 == null) return null;
        IntBuffer res = BuffUtil.allocate(4);
        res.put(r1);
        res.put(r2);
        res.reset();
        return res;
    }

    static IntBuffer doFour(IntBuffer cards, CardMemory cardMemory) {
        return Play.FOUROFAKIND.recognize(cards, cardMemory);
    }

    static IntBuffer doStraight(IntBuffer cards, CardMemory cardMemory) {
        return Play.STRAIGHT.recognize(cards, cardMemory);
    }

    static IntBuffer doFlush(IntBuffer cards, CardMemory cardMemory) {
        return Play.FLUSH.recognize(cards, cardMemory);
    }

    static IntBuffer doStraightFlush(IntBuffer cards, CardMemory cardMemory) {
        return Play.STRAIGHTFLUSH.recognize(cards, cardMemory);
    }

    static IntBuffer doRoyalFlush(IntBuffer cards, CardMemory cardMemory) {
        return Play.ROYALSTRAIGHTFLUSH.recognize(cards, cardMemory);
    }

    private static IntBuffer fastMatch(IntBuffer hand, Play play) {
        CardMemory mem = new CardMemory();
        return play.recognize(hand, mem);
    }

    public static int compareBoat(IntBuffer boat1, IntBuffer boat2) {
        // compare three-of-a-kind part
        int b1t = CardUtil.face(boat1.get(0));
        int b2t = CardUtil.face(boat2.get(0));
        if (b1t != b2t) return b1t - b2t;
        // compare pair part
        int b1p = CardUtil.face(boat1.get(3));
        int b2p = CardUtil.face(boat2.get(3));
        return b1p - b2p;
    }
}
