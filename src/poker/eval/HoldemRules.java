package poker.eval;

import static poker.eval.Deck.*;
import static poker.eval.Face.*;
import static poker.eval.Play.*;
import poker.player.*;

import java.util.*;


public class HoldemRules {


    private static final int ACE_ORD = ACE.ordinal();

    static public int[] doHighCard(int[] cards) {
        return Arrays.copyOfRange(cards, 0, 1);
    }


    static int[] doMatchWithExclusion(final int[] hand, final int limit, final int avoidFace) {
        int pFace = -1;
        final int[] swap = BuffUtil.allocate(limit);
        int[] res = null;
        int swapIdx = 0;
        for (int card : hand) {
            final int face = CardUtil.face(card);
            final int distance = face - pFace;

            if (distance != 0)
                swapIdx = 0;
            else if (face == avoidFace) {
                swapIdx = 0;
                continue;
            }
            pFace = face;
            swap[swapIdx++] = card;

            if (swapIdx >= limit) {
                res = swap;
                break;
            }
        }
        return res;
    }

    static private int[] doMatch(final int[] cards, final int latchSize) {

        int pFace = -1;
        final int[] swap = BuffUtil.allocate(latchSize);
        int swapIdx = 0;
        for (int card : cards) {
            final int face = CardUtil.face(card);
            final int distance = face - pFace;

            if (distance != 0)
                swapIdx = 0;
            pFace = face;
            swap[swapIdx++] = card;

            if (swapIdx >= latchSize)
                return swap;
        }
        return null;
    }


    static int[] doFullHouse(int[] hand, final CardMemory cardMemory) {
        int[] triple = THREEOFAKIND.recognize(hand, cardMemory);//uses cache
        int[] res = null;
        if (triple != null) {
            int[] pair = doMatchWithExclusion(hand, 2, CardUtil.face(triple[0]));

            if (pair != null) {
                res = BuffUtil.allocate(5);
                System.arraycopy(triple, 0, res, 0, 3);
                System.arraycopy(pair, 0, res, 3, 2);
            }
        }
        return res;
    }

    static int[] doPair(int[] hand) {
        return fastMatch(hand, Play.PAIR);
    }

    static int[] doTrip(int[] hand) {
        return fastMatch(hand, Play.THREEOFAKIND);
    }

    static int[] doTwoPair(int[] cards, CardMemory cardMemory) {
        int[] r1 = Play.PAIR.recognize(cards, cardMemory); //Recognize uses cache if possible.
        if (r1 == null) return null;
        // find second pair excluding the first
        int[] r2 = doMatchWithExclusion(cards, 2, CardUtil.face(r1[0]));
        if (r2 == null) return null;
        int[] res = BuffUtil.allocate(4);
        System.arraycopy(r1, 0, res, 0, 2);
        System.arraycopy(r2, 0, res, 2, 2);
        return res;
    }

    static int[] doFour(int[] cards, CardMemory cardMemory) {
        return Play.FOUROFAKIND.recognize(cards, cardMemory);
    }

    static int[] doStraight(int[] cards, CardMemory cardMemory) {
        return Play.STRAIGHT.recognize(cards, cardMemory);
    }

    static int[] doFlush(int[] cards, CardMemory cardMemory) {
        return Play.FLUSH.recognize(cards, cardMemory);
    }

    static int[] doStraightFlush(int[] cards, CardMemory cardMemory) {
        return Play.STRAIGHTFLUSH.recognize(cards, cardMemory);
    }

    static int[] doRoyalFlush(int[] cards, CardMemory cardMemory) {
        return Play.ROYALSTRAIGHTFLUSH.recognize(cards, cardMemory);
    }

    private static int[] fastMatch(int[] hand, Play play) {
        CardMemory mem = new CardMemory();
        return play.recognize(hand, mem);
    }

    public static int compareBoat(int[] boat1, int[] boat2) {
        // compare three-of-a-kind part
        int b1t = CardUtil.face(boat1[0]);
        int b2t = CardUtil.face(boat2[0]);
        if (b1t != b2t) return b1t - b2t;
        // compare pair part
        int b1p = CardUtil.face(boat1[3]);
        int b2p = CardUtil.face(boat2[3]);
        return b1p - b2p;
    }
}
