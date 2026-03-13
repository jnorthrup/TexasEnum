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

        /* build a hand one card at a time via addSorted, starting from EMPTYCARDS */
        IntArrayCircularBuffer hand = EMPTYCARDS;
        hand = addSorted(card(TWO, HEARTS), hand);
        hand = addSorted(card(ACE, SPADES), hand);
        hand = addSorted(card(TWO, SPADES), hand);
        hand = addSorted(card(ACE, CLUBS), hand);
        hand = addSorted(card(ACE, DIAMONDS), hand);
        hand = addSorted(card(TWO, DIAMONDS), hand);

        /* expected: sorted by face (ACE=0 first), then suit within face */
        IntArrayCircularBuffer expected = (IntArrayCircularBuffer) IntArrayCircularBuffer.wrap(new int[]{
                card(ACE, CLUBS),
                card(ACE, DIAMONDS),
                card(ACE, SPADES),
                card(TWO, DIAMONDS),
                card(TWO, HEARTS),
                card(TWO, SPADES),
        }).mark();

        final String result = new String(toChar((IntArrayCircularBuffer) hand.rewind().mark()));
        final String expect = new String(toChar((IntArrayCircularBuffer) expected.rewind().mark()));
        assertEquals(expect, result);
    }


    public void testMergeSortHands() throws Exception {
        /* mirror exactly how the game engine does it: wrap pocket, mergeSortHands, then add community */
        IntArrayCircularBuffer pocket = (IntArrayCircularBuffer) IntArrayCircularBuffer.wrap(
                new int[]{card(ACE, CLUBS), card(ACE, DIAMONDS)}
        ).mark();
        // this is how GameState.deal creates sorted pocket
        IntArrayCircularBuffer sortedPocket = mergeSortHands(pocket);

        // this is how GameState.flop creates the community
        IntArrayCircularBuffer community = (IntArrayCircularBuffer) IntArrayCircularBuffer.wrap(
                new int[]{card(TWO, DIAMONDS), card(TWO, HEARTS), card(TWO, SPADES), -1, -1}
        ).mark().limit(3);

        // this is how GameState.flop merges them
        final IntArrayCircularBuffer buffer = mergeSortHands(sortedPocket, community);

        assertEquals(5, buffer.limit());
        final String result = new String(toChar((IntArrayCircularBuffer) buffer.rewind().mark()));
        assertEquals("AcAd2d2h2s", result);
    }


    public void testCard() throws Exception {

        final Card card = new Card(TWO, SPADES);
        final int bcard = card(card);

        assertEquals(card.face, card(bcard).face);
        assertEquals(card.suit, card(bcard).suit);
        assertEquals(TWO.ordinal(), card.face.ordinal());
        assertEquals(SPADES.ordinal(), card.suit.ordinal());
        assertEquals(TWO, face((Integer) bcard));
        assertEquals(SPADES, suit((Integer) bcard));


    }

    public void testCompareDefault() throws Exception {
        IntArrayCircularBuffer pair1 = BuffUtil.allocate(2)
                .put(card(Face.ACE, Suit.SPADES))
                .put(card(Face.ACE, HEARTS));


        IntArrayCircularBuffer pair2 = BuffUtil.allocate(2)
                .put(card(TWO, CLUBS))
                .put(card(TWO, HEARTS));


        final int i = CardUtil.compareDefault(pair1, pair2);
        assertTrue(i < 0);

    }

    public void testCompareHighCard() throws Exception {
        IntArrayCircularBuffer hand1 = BuffUtil.allocate(2)
                .put(card(ACE, CLUBS))
                .put(card(ACE, DIAMONDS));


        IntArrayCircularBuffer hand2 = BuffUtil.allocate(2)
                .put(card(TWO, CLUBS))
                .put(card(TWO, DIAMONDS));


        final int i = compareHighCard((IntArrayCircularBuffer) hand1.rewind().mark(), (IntArrayCircularBuffer) hand2.rewind().mark());
        assertTrue(i < 0);

    }

    public void testToChar() throws Exception {
        IntArrayCircularBuffer altHand = (IntArrayCircularBuffer) IntArrayCircularBuffer.wrap(new int[]{card(ACE, CLUBS), card(ACE, HEARTS)}).mark();

        final StringBuilder sb = new StringBuilder();

        final char[] value = toChar(altHand);
        sb.append(value);
        assertEquals("AcAh", sb.toString());

    }

    public void testCompareBoat() throws Exception {
        IntArrayCircularBuffer boat1 = BuffUtil.allocate(5)
                .put(card(THREE, CLUBS))
                .put(card(THREE, DIAMONDS))
                .put(card(THREE, SPADES))
                .put(card(ACE, DIAMONDS))
                .put(card(ACE, SPADES));

        IntArrayCircularBuffer boat2 = BuffUtil.allocate(5)
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