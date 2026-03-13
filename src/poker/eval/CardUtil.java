package poker.eval;

import static poker.eval.Deck.*;

import java.util.*;

public class CardUtil {

    static public final IntArrayCircularBuffer EMPTYCARDS = IntArrayCircularBuffer.allocate(0);
    static public final int STRAIT_MATCH_NEEDED = 4;

    final public static int suit(final int card) {
        return card & 0x3;
    }

    final public static int face(final int card) {
        return card >>> 16;
    }


    public static IntArrayCircularBuffer addSorted(int card, IntArrayCircularBuffer focus) {

        if (focus.capacity() <= focus.limit()) {
            final int ilim = focus.limit();
            final IntArrayCircularBuffer grown = IntArrayCircularBuffer.allocate(7).mark();
            if (ilim > 0) {
                focus.rewind();
                grown.put(focus);
            }
            focus = grown.limit(ilim).rewind().mark();
        }

        final int icap = focus.capacity();
        final int ilim = focus.limit();

        focus.reset();

        int hwater = Deck.DECK_SIZE;
        while (focus.hasRemaining()) {
            hwater = focus.get();
            if (hwater >= card)
                break;
        }

        final int flim = focus.limit();
        IntArrayCircularBuffer res;
        if (hwater == card) {
            res = focus.rewind().mark();
        } else {

            IntArrayCircularBuffer mv = focus.reset().slice();

            if (icap == flim) {
                IntArrayCircularBuffer swap = IntArrayCircularBuffer.allocate(icap + 1);
                swap.put(focus.reset());
                focus = swap.limit(flim).rewind().mark();
            }

            focus.limit(flim + 1);
            IntArrayCircularBuffer swap = focus.slice();
            swap.position(1);
            swap.put(mv);
            focus.put(card);
            res = focus.position(flim + 1).rewind().mark();
        }
        return res;
    }

    @SuppressWarnings({"StatementWithEmptyBody"})
    static public int compareHighCard(IntArrayCircularBuffer hand1, IntArrayCircularBuffer hand2) {
        hand1.rewind().mark();
        hand2.rewind().mark();
        int eq = 0;
        while (hand1.hasRemaining() && hand2.hasRemaining() &&
                (eq = (hand1.get() >>> 16) -
                        (hand2.get() >>> 16)) == 0) ;


        return eq;
    }


    static public int compareDefault(IntArrayCircularBuffer cards1, IntArrayCircularBuffer cards2) {
        return (cards1.get(0) >>> 16) - (cards2.get(0) >>> 16);
    }

    public static IntArrayCircularBuffer hand(Collection<Card> cards1) {
        final Card[] boat1 = cards1.toArray(new Card[0]);

        final IntArrayCircularBuffer b1 = IntArrayCircularBuffer.allocate(boat1.length);
        for (Card card : boat1)
            b1.put(card(card));
        return b1;
    }

    @SuppressWarnings({"deprecation"})
    static public Card card(int card) {
        assert (card >= 0);
        assert (face(card) < FACES_LEN && suit(card) < SUITS_LEN);
        Card card1 = new Card(Face.values()[(card >>> 16)], Suit.values()[(card & 0x3)]);

        return card1;
    }

    static public int card(Card card) {
        return card(card.face, card.suit);
    }

    public static int card(Face face, Suit suit) {
        {
            return (face.ordinal() << 16) | 0x3 & suit.ordinal();
        }
    }

    @SuppressWarnings({"deprecation"})
    static public Suit suit(final Integer card) {
        return Suit.values()[card & 0x3];
    }

    @SuppressWarnings({"deprecation"})
    static public Face face(final Integer card) {
        final int f = (int) card >>> 16;
        return Face.values()[f];
    }

    public static IntArrayCircularBuffer mergeSortHands(IntArrayCircularBuffer... hands) {

        IntArrayCircularBuffer swap = hands[0];
        for (int i = 1; i < hands.length; i++) {
            IntArrayCircularBuffer hand = hands[i];
            hand.reset();
            while (hand.hasRemaining()) {
                final int card = hand.get();
                swap = addSorted(card, swap);
            }

        }
        return swap.rewind().mark();
    }

    public static IntArrayCircularBuffer hand(Card[] hand) {
        IntArrayCircularBuffer subject = IntArrayCircularBuffer.allocate(0);
        for (Card card : hand) {
            final int card1 = card(card);
            subject = addSorted(card1, subject);
        }
        return subject;
    }

    public static Card[] hand(IntArrayCircularBuffer hand) {
        final Card[] cards = new Card[hand.limit()];
        IntArrayCircularBuffer h2 = hand.duplicate().rewind().mark();

        for (int i = 0; i < cards.length; i++) {
            Card card = null;
            card = card(h2.get());
            cards[i] = card;
        }
        return cards;
    }

    public static char[] toChar(IntArrayCircularBuffer cards) {
        cards.reset();
        char[] out = new char[2 * cards.limit()];
        int outIdx = 0;
        while (cards.hasRemaining()) {
            int card = cards.get();
            final int f = card >>> 16;
            final int s = card & 0x3;
            final char face = Deck.faces[f];
            final char suit = Deck.suits[s];
            out[outIdx++] = face;
            out[outIdx++] = suit;
        }
        return out;
    }

    public static int compareTwoPair(IntArrayCircularBuffer twin1, IntArrayCircularBuffer twin2) {
        final int eq = (twin1.get(0) >>> 16) - (twin2.get(0) >>> 16);
        return eq != 0 ? eq : (twin1.get(2) >>> 16) - (twin2.get(2) >>> 16);
    }

    @SuppressWarnings({"SameReturnValue"})
    static public int compareOut() {
        return 0;
    }


}
