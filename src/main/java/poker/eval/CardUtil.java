package poker.eval;

import static poker.eval.Deck.*;

import java.nio.*;
import java.util.*;

/**
 * User: jim
 * Date: Oct 1, 2007
 * Time: 11:42:56 PM
 */
@SuppressWarnings({"StatementWithEmptyBody"})
public class CardUtil {

    static public final IntBuffer EMPTYCARDS = IntBuffer.allocate(0).asReadOnlyBuffer();
    static public final int STRAIT_MATCH_NEEDED = 4;

    final public static int suit(final int card) {
        /*suit*/
        return card & 0x3;
    }

    final public static int face(final int card) {
        /*face*/
        return card >>> 16;
    }


    /**
     * merge sorts new cards into existing intbuffer hands.  attempts to benefit from excess capacity and will grow an buffer as needed
     *
     * @param card  card Value which is assumed at mpos(end) for a rewind
     * @param focus hand being added to presumed already ordered in ca game comparable state
     * @return the hand, often but not reliably the original buf
     */
    public static IntBuffer addSorted(int card, IntBuffer focus) {

        final int icap = focus.capacity();
        if (icap > 7)
            throw new Error();
        final int ilim = focus.limit();
        if ((icap == ilim))
            focus = (IntBuffer) BuffUtil.allocate(7).mark().limit(0);

        focus.reset();

        int hwater = Deck.DECK_SIZE;
        while (focus.mark().hasRemaining()) {
            hwater = focus.get();
            if (hwater >= card)
                break;
        }

//        final int pos = focus.mpos();

        final int flim = focus.limit();
        IntBuffer res;
        if (hwater == card) {
            res = (IntBuffer) focus/*.position(flim)*/;
        } else {

            final IntBuffer mv = ((IntBuffer) focus.reset()).slice();

            if (icap == flim) {
                final IntBuffer swap = BuffUtil.allocate(icap + 1);
                swap.put((IntBuffer) focus.reset());
                focus = (IntBuffer) swap.limit(flim).rewind().mark();
            }


            focus.limit(flim + 1);
            final IntBuffer swap = focus.slice();
            swap.position(1);
            swap.put(mv);
            focus.put(card);
            res = (IntBuffer) focus.position(flim);
        }
        return res;
    }

    @SuppressWarnings({"StatementWithEmptyBody"})
    /**
     * this method presupposes that Play must be equal to justify this cost, therefore buffer
     * lengths are going to be equal as well.
     */
    static public int compareHighCard(IntBuffer hand1, IntBuffer hand2) {
        hand1.rewind().mark();
        hand2.rewind().mark();
        int eq = 0;
        /*face*/
        /*face*/
        while (hand1.hasRemaining() &&
                (eq = (hand1.get() >>> 16) -
                        (hand2.get() >>> 16)) == 0) ;


        return eq;
    }


    static public int compareDefault(IntBuffer cards1, IntBuffer cards2) {
        /*face*/
        /*face*/
        return (cards1.get(0) >>> 16) - (cards2.get(0) >>> 16);
    }

    /**
     * @param cards1
     * @deprecated
     */
    public static IntBuffer hand(Collection<Card> cards1) {
        final Card[] boat1 = cards1.toArray(new Card[0]);

        final IntBuffer b1 = BuffUtil.allocate(boat1.length);
        for (Card card : boat1)
            b1.put(card(card));
        return b1;
    }

    /**
     * @deprecated
     */
    static public Card card(int card) {
        assert (card > 0);
        assert (card < DECK_SIZE);
        /*suit*/
        /*face*/
        Card card1 = new Card(Face.values()[(card >>> 16)], Suit.values()[(card & 0x3)]);

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
        {//card begin
            return (face.ordinal() << 16) | 0x3 & suit.ordinal();
        } //card end
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
        /*face*/
        final int f = (int) card >>> 16;
        return Face.values()[f];
    }

    public static IntBuffer mergeSortHands(IntBuffer... hands) {

        IntBuffer swap = hands[0];
        for (int i = 1; i < hands.length; i++) {
            IntBuffer hand = hands[i];
            hand.reset();
            while (hand.hasRemaining()) {
                final int card = hand.get();
                swap = addSorted(card, swap);
            }

        }
        return (IntBuffer) swap.rewind().mark();
    }

    /**
     * @param hand
     * @deprecated
     */
    public static IntBuffer hand(Card[] hand) {
        IntBuffer subject = BuffUtil.allocate(0);
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
    public static Card[] hand(IntBuffer hand) {
        final Card[] cards = new Card[hand.limit()];
        final IntBuffer h2 = (IntBuffer) hand.duplicate().rewind().mark();

        for (int i = 0; i < cards.length; i++) {
            Card card = null;
            card = card(h2.get());
            cards[i] = card;
        }
        return cards;
    }

    public static char[] toChar(IntBuffer cards) {
        cards.reset();
        final CharBuffer out = (CharBuffer) CharBuffer.allocate(2 * cards.limit()).mark();
        while (cards.hasRemaining()) {
            int card = cards.get();
            /*face*/
            final int f = card >>> 16;
            /*suit*/
            final int s = card & 0x3;
            final char face = Deck.faces[f];
            final char suit = Deck.suits[s];
            out.put(face).put(suit);
        }
        return out.array();
    }

    public static int compareTwoPair(IntBuffer twin1, IntBuffer twin2) {
        /*face*/
        /*face*/
        final int eq = (twin1.get(0) >>> 16) - (twin2.get(0) >>> 16);
        /*face*/
        /*face*/
        return eq != 0 ? eq : (twin1.get(2) >>> 16) - (twin2.get(2) >>> 16);
    }

    @SuppressWarnings({"SameReturnValue"})
    static public int compareOut() {
        return 0;
    }


}

