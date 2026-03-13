package poker.eval;

import static poker.eval.Deck.*;
import static poker.eval.BuffUtil.*;

import java.util.*;

/**
 * User: jim
 * Date: Oct 1, 2007
 * Time: 11:42:56 PM
 * 
 * Replaced IntBuffer with int[] for memory efficiency.
 * Cards are stored as single bytes using (face << 2) | suit encoding.
 */
@SuppressWarnings({"StatementWithEmptyBody"})
public class CardUtil {

    static public final int[] EMPTYCARDS = new int[0];
    static public final int STRAIT_MATCH_NEEDED = 4;

    // Card encoding: (face << 2) | suit
    // face: 0-12 (4 bits), suit: 0-3 (2 bits)
    final public static int suit(final int card) {
        return card & 0x3;
    }

    final public static int face(final int card) {
        return card >>> 2;
    }

    /**
     * merge sorts new cards into existing int array hands.
     * @param card  card Value which is assumed at the end for a rewind
     * @param focus hand being added to presumed already ordered in comparable state
     * @return the hand, often but not reliably the original buf
     */
    public static int[] addSorted(int card, int[] focus) {

        // ensure focus has room to grow
        // Focus is full if it's non-empty and has no -1 markers (card values are 0-51)
        final int ilim = focus.length;
        boolean needsGrow = false;
        if (ilim > 0) {
            // Check if array is full (no -1 markers)
            int count = 0;
            for (int i = 0; i < ilim; i++) {
                if (focus[i] != -1) count++;
            }
            needsGrow = (count == ilim);
        }
        
        if (needsGrow || ilim == 0) {
            final int newSize = Math.max(7, ilim + 1);
            final int[] grown = BuffUtil.allocate(newSize);
            if (ilim > 0) {
                System.arraycopy(focus, 0, grown, 0, ilim);
            }
            focus = grown;
        }

        final int icap = focus.length;

        int hwater = Deck.DECK_SIZE;
        int idx = 0;
        while (idx < ilim) {
            hwater = focus[idx];
            if (hwater >= card)
                break;
            idx++;
        }

        final int flim = ilim;
        int[] res;
        if (hwater == card) {
            res = focus;
        } else {
            // shift elements to make room
            if (icap == flim) {
                final int[] swap = BuffUtil.allocate(icap + 1);
                System.arraycopy(focus, 0, swap, 0, icap);
                focus = swap;
            }

            // shift right from idx to flim
            for (int i = flim; i > idx; i--) {
                focus[i] = focus[i - 1];
            }
            focus[idx] = card;
            res = focus;
        }
        return res;
    }

    @SuppressWarnings({"StatementWithEmptyBody"})
    /**
     * this method presupposes that Play must be equal to justify this cost, therefore buffer
     * lengths are going to be equal as well.
     */
    static public int compareHighCard(int[] hand1, int[] hand2) {
        int eq = 0;
        int len = Math.min(hand1.length, hand2.length);
        for (int i = 0; i < len; i++) {
            eq = face(hand1[i]) - face(hand2[i]);
            if (eq != 0) break;
        }
        return eq;
    }


    static public int compareDefault(int[] cards1, int[] cards2) {
        return face(cards1[0]) - face(cards2[0]);
    }

    /**
     * @param cards1
     * @deprecated
     */
    public static int[] hand(Collection<Card> cards1) {
        final Card[] boat1 = cards1.toArray(new Card[0]);

        final int[] b1 = BuffUtil.allocate(boat1.length);
        for (int i = 0; i < boat1.length; i++)
            b1[i] = card(boat1[i]);
        return b1;
    }

    /**
     * @deprecated
     */
    static public Card card(int card) {
        assert (card >= 0);
        assert (face(card) < FACES_LEN && suit(card) < SUITS_LEN);
        Card card1 = new Card(Face.values()[face(card)], Suit.values()[suit(card)]);
        return card1;
    }

    /**
     * @param card a card
     * @return a card
     * @deprecated
     */
    static public int card(Card card) {
        return card(card.face, card.suit);
    }

    public static int card(Face face, Suit suit) {
        return (face.ordinal() << 2) | suit.ordinal();
    }

    /**
     * @deprecated
     */
    static public Suit suit(final Integer card) {
        return Suit.values()[card & 0x3];
    }

    /**
     * @param card 0-51
     * @return face Enum
     * @deprecated
     */
    static public Face face(final Integer card) {
        final int f = card >>> 2;
        return Face.values()[f];
    }

    public static int[] mergeSortHands(int[]... hands) {

        int[] swap = hands[0];
        for (int i = 1; i < hands.length; i++) {
            int[] hand = hands[i];
            for (int j = 0; j < hand.length; j++) {
                swap = addSorted(hand[j], swap);
            }
        }
        return swap;
    }

    /**
     * @param hand
     * @deprecated
     */
    public static int[] hand(Card[] hand) {
        int[] subject = BuffUtil.allocate(0);
        for (Card card : hand) {
            final int card1 = card(card);
            subject = addSorted(card1, subject);
        }
        return subject;
    }

    /**
     * @param hand hand of cards
     * @return null or successful match
     * @deprecated
     */
    public static Card[] hand(int[] hand) {
        final Card[] cards = new Card[hand.length];
        for (int i = 0; i < cards.length; i++) {
            cards[i] = card(hand[i]);
        }
        return cards;
    }

    public static char[] toChar(int[] cards) {
        final char[] out = new char[2 * cards.length];
        for (int i = 0; i < cards.length; i++) {
            int card = cards[i];
            final int f = face(card);
            final int s = suit(card);
            out[2 * i] = Deck.faces[f];
            out[2 * i + 1] = Deck.suits[s];
        }
        return out;
    }

    public static int compareTwoPair(int[] twin1, int[] twin2) {
        final int eq = face(twin1[0]) - face(twin2[0]);
        return eq != 0 ? eq : face(twin1[2]) - face(twin2[2]);
    }

    @SuppressWarnings({"SameReturnValue"})
    static public int compareOut() {
        return 0;
    }

}
