package poker.eval;
/**
 * User: jim
 * Date: Oct 3, 2007
 * Time: 11:53:38 AM
 */

import junit.framework.*;
import static poker.eval.CardUtil.*;
import static poker.eval.Face.*;
import static poker.eval.Suit.*;

import java.nio.*;
import java.util.*;
import java.util.logging.*;

public class CardUtilTest extends TestCase {

    public void testAddSorted() throws Exception {

        /** bogus sorted
         *
         */

        IntBuffer subjectHand = (IntBuffer) IntBuffer.wrap(new int[]{
                card(TWO, HEARTS),
                card(ACE, SPADES),
                card(TWO, SPADES),
                card(ACE, CLUBS),
                card(ACE, DIAMONDS),
                card(TWO, DIAMONDS),
        }).mark();

        /* manually sorted

         */

        IntBuffer controlHand = (IntBuffer) IntBuffer.wrap(new int[]{
                card(ACE, CLUBS),
                card(ACE, DIAMONDS),
                card(ACE, SPADES),
                card(TWO, DIAMONDS),
                card(TWO, HEARTS),
                card(TWO, SPADES),
        }).mark();

        IntBuffer referenceHand = (IntBuffer) IntBuffer.wrap(new int[]{
                card(ACE, CLUBS),
                card(ACE, DIAMONDS),
                card(ACE, SPADES),
                card(TWO, DIAMONDS),
                card(TWO, HEARTS),
                card(TWO, SPADES),
        }).mark();

        while (controlHand.hasRemaining())
            subjectHand = addSorted(controlHand.get(), subjectHand);


        final IntBuffer buffer = mergeSortHands(controlHand);
        final IntBuffer intBuffer = mergeSortHands(referenceHand);
        final String s = new String(toChar(buffer));
        final String s1 = new String(toChar(intBuffer));
        assertEquals(
                s,
                s1
        );
        assertEquals(controlHand.rewind().mark(), referenceHand.rewind().mark());

        final Card[] cards1 = hand((IntBuffer) subjectHand.reset());
        final Card[] cards2 = hand((IntBuffer) controlHand.reset());
        final Card[] cards3 = hand(referenceHand);
        Logger.getAnonymousLogger().info("Success: " + Arrays.toString(cards1) + " becomes " + Arrays.toString(cards2) + " is " + Arrays.toString(cards3));


    }


    public void testMergeSortHands() throws Exception {
        /* manually sorted */
        Card[] control = new Card[]{
                new Card(TWO, HEARTS),
                new Card(ACE, DIAMONDS),
                new Card(TWO, SPADES),
                new Card(ACE, SPADES),
                new Card(TWO, DIAMONDS),
                new Card(ACE, CLUBS),
        };
        final IntBuffer subjectHand = hand(control);
        IntBuffer referenceHand = BuffUtil.allocate(control.length);
        referenceHand.put(card(ACE, CLUBS))
                .put(card(ACE, DIAMONDS))
                .put(card(ACE, SPADES))
                .put(card(TWO, DIAMONDS))
                .put(card(TWO, SPADES))
                .put(card(TWO, HEARTS));
        final IntBuffer buffer = mergeSortHands(subjectHand, referenceHand);

        assert (buffer.rewind().mark().equals(referenceHand.rewind().mark()));
    }


    public void testCard() throws Exception {

        final Card card = new Card(TWO, SPADES);
        final int bcard = card(card);

        assert (card(bcard).equals(card));
        assertEquals(TWO.ordinal(), card.face.ordinal());
        assertEquals(SPADES.ordinal(), card.suit.ordinal());
        assertEquals(TWO, face((Integer) bcard));
        assertEquals(SPADES, suit((Integer) bcard));


    }

    public void testCompareDefault() throws Exception {
        IntBuffer pair1 = BuffUtil.allocate(2)
                .put(card(Face.ACE, Suit.SPADES))
                .put(card(Face.ACE, HEARTS));


        IntBuffer pair2 = BuffUtil.allocate(2)
                .put(card(TWO, CLUBS))
                .put(card(TWO, HEARTS));


        final int i = CardUtil.compareDefault(pair1, pair2);
        assertTrue(i < 0);

    }

    public void testCompareHighCard() throws Exception {
        IntBuffer hand1 = BuffUtil.allocate(2)
                .put(card(ACE, CLUBS))
                .put(card(ACE, DIAMONDS));


        IntBuffer hand2 = BuffUtil.allocate(2)
                .put(card(TWO, CLUBS))
                .put(card(TWO, DIAMONDS));


        final int i = compareHighCard((IntBuffer) hand1.rewind().mark(), (IntBuffer) hand2.rewind().mark());
        assertTrue(i < 0);

    }

    public void testToChar() throws Exception {
        IntBuffer altHand = (IntBuffer) IntBuffer.wrap(new int[]{card(ACE, CLUBS), card(ACE, HEARTS)}).mark();

        final StringBuilder sb = new StringBuilder();

        final char[] value = toChar(altHand);
        sb.append(value);
        assertEquals("AcAh", sb.toString());

    }

    public void testCompareBoat() throws Exception {
        IntBuffer boat1 = BuffUtil.allocate(5)
                .put(card(THREE, CLUBS))
                .put(card(THREE, DIAMONDS))
                .put(card(THREE, SPADES))
                .put(card(ACE, DIAMONDS))
                .put(card(ACE, SPADES));

        IntBuffer boat2 = BuffUtil.allocate(5)
                .put(card(TWO, HEARTS))
                .put(card(TWO, DIAMONDS))
                .put(card(TWO, SPADES))
                .put(card(ACE, CLUBS))
                .put(card(ACE, DIAMONDS));

        assertTrue(HoldemRules.compareBoat(boat1, boat2) < 0);

        boat1.clear();
        boat2.clear();
        boat1.put(card(THREE, CLUBS))
                .put(card(THREE, DIAMONDS))
                .put(card(THREE, SPADES))
                .put(card(FOUR, DIAMONDS))
                .put(card(FOUR, SPADES));

        boat2.put(card(THREE, HEARTS))
                .put(card(THREE, DIAMONDS))
                .put(card(THREE, SPADES))
                .put(card(TWO, CLUBS))
                .put(card(TWO, DIAMONDS));

        assertTrue(HoldemRules.compareBoat(boat1, boat2) < 0);
    }

    public void testSuit() {

        assertEquals(SPADES.ordinal(), card(ACE, SPADES));

    }
}