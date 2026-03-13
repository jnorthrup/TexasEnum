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

import java.util.*;
import java.util.logging.*;

public class CardUtilTest extends TestCase {

    public void testAddSorted() throws Exception {

        /* build a hand one card at a time via addSorted, starting from EMPTYCARDS */
        int[] hand = EMPTYCARDS;
        hand = addSorted(card(TWO, HEARTS), hand);
        hand = addSorted(card(ACE, SPADES), hand);
        hand = addSorted(card(TWO, SPADES), hand);
        hand = addSorted(card(ACE, CLUBS), hand);
        hand = addSorted(card(ACE, DIAMONDS), hand);
        hand = addSorted(card(TWO, DIAMONDS), hand);

        /* expected: sorted by face (ACE=0 first), then suit within face */
        int[] expected = new int[]{
                card(ACE, CLUBS),
                card(ACE, DIAMONDS),
                card(ACE, SPADES),
                card(TWO, DIAMONDS),
                card(TWO, HEARTS),
                card(TWO, SPADES),
        };

        final String result = new String(toChar(hand));
        final String expect = new String(toChar(expected));
        assertEquals(expect, result);
    }


    public void testMergeSortHands() throws Exception {
        /* mirror exactly how the game engine does it: wrap pocket, mergeSortHands, then add community */
        int[] pocket = new int[]{card(ACE, CLUBS), card(ACE, DIAMONDS)};
        // this is how GameState.deal creates sorted pocket
        int[] sortedPocket = mergeSortHands(pocket);

        // this is how GameState.flop creates the community
        int[] community = new int[]{card(TWO, DIAMONDS), card(TWO, HEARTS), card(TWO, SPADES)};

        // this is how GameState.flop merges them
        final int[] buffer = mergeSortHands(sortedPocket, community);

        assertEquals(5, buffer.length);
        final String result = new String(toChar(buffer));
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
        int[] pair1 = new int[2];
        pair1[0] = card(Face.ACE, Suit.SPADES);
        pair1[1] = card(Face.ACE, HEARTS);


        int[] pair2 = new int[2];
        pair2[0] = card(TWO, CLUBS);
        pair2[1] = card(TWO, HEARTS);


        final int i = CardUtil.compareDefault(pair1, pair2);
        assertTrue(i < 0);

    }

    public void testCompareHighCard() throws Exception {
        int[] hand1 = new int[2];
        hand1[0] = card(ACE, CLUBS);
        hand1[1] = card(ACE, DIAMONDS);


        int[] hand2 = new int[2];
        hand2[0] = card(TWO, CLUBS);
        hand2[1] = card(TWO, DIAMONDS);


        final int i = compareHighCard(hand1, hand2);
        assertTrue(i < 0);

    }

    public void testToChar() throws Exception {
        int[] altHand = new int[]{card(ACE, CLUBS), card(ACE, HEARTS)};

        final StringBuilder sb = new StringBuilder();

        final char[] value = toChar(altHand);
        sb.append(value);
        assertEquals("AcAh", sb.toString());

    }

    public void testCompareBoat() throws Exception {
        int[] boat1 = new int[5];
        boat1[0] = card(THREE, CLUBS);
        boat1[1] = card(THREE, DIAMONDS);
        boat1[2] = card(THREE, SPADES);
        boat1[3] = card(ACE, DIAMONDS);
        boat1[4] = card(ACE, SPADES);

        int[] boat2 = new int[5];
        boat2[0] = card(TWO, HEARTS);
        boat2[1] = card(TWO, DIAMONDS);
        boat2[2] = card(TWO, SPADES);
        boat2[3] = card(ACE, CLUBS);
        boat2[4] = card(ACE, DIAMONDS);

        assertTrue(HoldemRules.compareBoat(boat1, boat2) < 0);

        boat1 = new int[5];
        boat2 = new int[5];
        boat1[0] = card(THREE, CLUBS);
        boat1[1] = card(THREE, DIAMONDS);
        boat1[2] = card(THREE, SPADES);
        boat1[3] = card(FOUR, DIAMONDS);
        boat1[4] = card(FOUR, SPADES);

        boat2[0] = card(THREE, HEARTS);
        boat2[1] = card(THREE, DIAMONDS);
        boat2[2] = card(THREE, SPADES);
        boat2[3] = card(TWO, CLUBS);
        boat2[4] = card(TWO, DIAMONDS);

        assertTrue(HoldemRules.compareBoat(boat1, boat2) < 0);
    }

    public void testSuit() {

        assertEquals(SPADES.ordinal(), card(ACE, SPADES));

    }
}
