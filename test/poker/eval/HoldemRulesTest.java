package poker.eval;
/**
 * User: jim
 * Date: Oct 7, 2007
 * Time: 4:06:28 AM
 */

import junit.framework.*;
import static poker.eval.CardUtil.*;
import static poker.eval.Face.*;
import static poker.eval.HoldemRules.*;
import static poker.eval.Suit.*;
import static poker.eval.Play.assess;
import static poker.eval.Play.STRAIGHTFLUSH;
import static poker.eval.Play.*;
import poker.player.*;

import java.util.*;

public class HoldemRulesTest extends TestCase {

    HoldemRules rules;

    public void testDoHighCard() throws Exception {
        final int[] cards = getSpadesRoyalStraightFlush();

        final int[] buffer = doHighCard(cards);
        assertEquals(buffer[0], card(ACE, SPADES));


    }

    private int[] getSpadesRoyalStraightFlush() {
        final int[] cards = new int[7];
        cards[0] = card(ACE, SPADES);
        cards[1] = card(KING, SPADES);
        cards[2] = card(QUEEN, SPADES);
        cards[3] = card(JACK, SPADES);
        cards[4] = card(TEN, SPADES);
        cards[5] = card(NINE, SPADES);
        cards[6] = card(EIGHT, SPADES);
        return cards;
    }


    public void testDoMatch() throws Exception {
        int[] r1;
        {
            int[] cards;
            cards = new int[7];
            cards[0] = card(KING, DIAMONDS);
            cards[1] = card(KING, SPADES);
            cards[2] = card(QUEEN, SPADES);
            cards[3] = card(JACK, SPADES);
            cards[4] = card(JACK, DIAMONDS);
            r1 = HoldemRules.doMatchWithExclusion(cards, 2, Face.KING.ordinal());
        }

        int[] r2;
        {
            int[] cards;
            cards = new int[7];
            cards[0] = card(KING, DIAMONDS);
            cards[1] = card(KING, SPADES);
            cards[2] = card(QUEEN, SPADES);
            cards[3] = card(JACK, SPADES);
            cards[4] = card(JACK, DIAMONDS);
            r2 = HoldemRules.doMatchWithExclusion(cards, 2, Face.JACK.ordinal());
        }
        int[] r3;
        {
            int[] cards;
            cards = new int[7];
            cards[0] = card(KING, DIAMONDS);
            cards[1] = card(KING, SPADES);
            cards[2] = card(KING, HEARTS);
            cards[3] = card(KING, CLUBS);
            cards[4] = card(JACK, DIAMONDS);
            r3 = HoldemRules.doMatchWithExclusion(cards, 4, -1);
        }

        assertEquals(r1.length, r2.length);
        /*face*/
        assertEquals(JACK.ordinal(), face(r1[0]));
        /*face*/
        assertEquals(KING.ordinal(), face(r2[0]));
        /*face*/
        assertEquals(KING.ordinal(), face(r3[0]));
        assertEquals(4, r3.length);
    }

    public void testDoPair() throws Exception {
        int[] r1;
        int[] cards;
        cards = new int[]
                {
                        (card(KING, DIAMONDS)),
                        (card(KING, SPADES)),
                        (card(QUEEN, SPADES)),
                        (card(JACK, SPADES)),
                };
        r1 = HoldemRules.doPair(cards);
        assertEquals(2, r1.length);
    }

    public void testDoTrip() throws Exception {
        int[] cards;
        cards = new int[]
                {
                        (card(KING, DIAMONDS)),
                        (card(KING, SPADES)),
                        (card(KING, HEARTS)),
                        (card(JACK, SPADES)),
                };
        int[] r1 = HoldemRules.doTrip(cards);
        assertEquals(3, r1.length);
    }

    public void testDoTwoPair() throws Exception {
        int[] cards;
        cards = new int[]
                {
                        (card(KING, DIAMONDS)),
                        (card(KING, SPADES)),
                        (card(QUEEN, SPADES)),
                        (card(QUEEN, HEARTS)),
                        (card(JACK, SPADES)),
                };
        int[] r1 = HoldemRules.doTwoPair(cards, new CardMemory());
        assertEquals(4, r1.length);
        assertEquals(KING.ordinal(), face(r1[0]));
        assertEquals(QUEEN.ordinal(), face(r1[2]));
    }

    public void testDoFullHouse() throws Exception {
        int[] cards;
        cards = new int[]
                {
                        (card(KING, DIAMONDS)),
                        (card(KING, SPADES)),
                        (card(KING, HEARTS)),
                        (card(QUEEN, SPADES)),
                        (card(QUEEN, HEARTS)),
                };
        int[] r1 = HoldemRules.doFullHouse(cards, new CardMemory());
        assertEquals(5, r1.length);
    }

    public void testDoFour() throws Exception {
        int[] cards;
        cards = new int[]
                {
                        (card(KING, DIAMONDS)),
                        (card(KING, SPADES)),
                        (card(KING, HEARTS)),
                        (card(KING, CLUBS)),
                        (card(JACK, SPADES)),
                };
        int[] r1 = HoldemRules.doFour(cards, new CardMemory());
        assertEquals(4, r1.length);
    }

    public void testDoStraight() throws Exception {
        int[] cards;
        cards = new int[]
                {
                        (card(TWO, DIAMONDS)),
                        (card(THREE, SPADES)),
                        (card(FOUR, HEARTS)),
                        (card(FIVE, CLUBS)),
                        (card(SIX, SPADES)),
                };
        int[] r1 = HoldemRules.doStraight(cards, new CardMemory());
        assertEquals(5, r1.length);
    }

    public void testDoFlush() throws Exception {
        int[] cards;
        cards = new int[]
                {
                        (card(TWO, SPADES)),
                        (card(THREE, SPADES)),
                        (card(FOUR, SPADES)),
                        (card(FIVE, SPADES)),
                        (card(SIX, SPADES)),
                };
        int[] r1 = HoldemRules.doFlush(cards, new CardMemory());
        assertEquals(5, r1.length);
    }

    public void testDoStraightFlush() throws Exception {
        int[] cards;
        cards = new int[]
                {
                        (card(TWO, SPADES)),
                        (card(THREE, SPADES)),
                        (card(FOUR, SPADES)),
                        (card(FIVE, SPADES)),
                        (card(SIX, SPADES)),
                };
        int[] r1 = HoldemRules.doStraightFlush(cards, new CardMemory());
        assertEquals(5, r1.length);
    }

    public void testDoRoyalFlush() throws Exception {
        int[] cards;
        cards = new int[]
                {
                        (card(ACE, SPADES)),
                        (card(KING, SPADES)),
                        (card(QUEEN, SPADES)),
                        (card(JACK, SPADES)),
                        (card(TEN, SPADES)),
                };
        int[] r1 = HoldemRules.doRoyalFlush(cards, new CardMemory());
        assertEquals(5, r1.length);
    }

    public void testAssess() throws Exception {
        // Test assess method
        int[] cards = new int[]
                {
                        (card(ACE, SPADES)),
                        (card(KING, SPADES)),
                        (card(QUEEN, SPADES)),
                        (card(JACK, SPADES)),
                        (card(TEN, SPADES)),
                };
        Pair<Play, int[]> result = assess(cards);
        assertEquals(Play.ROYALSTRAIGHTFLUSH, result.getFirst());
        assertEquals(5, result.getSecond().length);
    }
}
