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

import java.nio.*;

public class HoldemRulesTest extends TestCase {

    HoldemRules rules;

    public void testDoHighCard() throws Exception {
        final IntBuffer cards = getSpadesRoyalStraightFlush();

        final IntBuffer buffer = doHighCard(cards);
        assertEquals(buffer.get(0), card(ACE, SPADES));


    }

    private IntBuffer getSpadesRoyalStraightFlush() {
        final IntBuffer cards = BuffUtil.allocate(7);
        cards.put(card(ACE, SPADES));
        cards.put(card(KING, SPADES));
        cards.put(card(QUEEN, SPADES));
        cards.put(card(JACK, SPADES));
        cards.put(card(TEN, SPADES));
        cards.put(card(NINE, SPADES));
        cards.put(card(EIGHT, SPADES));
        return cards;
    }


    public void testDoMatch() throws Exception {
        IntBuffer r1;
        {
            IntBuffer cards;
            cards = BuffUtil.allocate(7);
            cards.put(card(KING, DIAMONDS));
            cards.put(card(KING, SPADES));
            cards.put(card(QUEEN, SPADES));
            cards.put(card(JACK, SPADES));
            cards.put(card(JACK, DIAMONDS));
            r1 = HoldemRules.doMatchWithExclusion(cards, 2, Face.KING.ordinal());
        }

        IntBuffer r2;
        {
            IntBuffer cards;
            cards = BuffUtil.allocate(7);
            cards.put(card(KING, DIAMONDS));
            cards.put(card(KING, SPADES));
            cards.put(card(QUEEN, SPADES));
            cards.put(card(JACK, SPADES));
            cards.put(card(JACK, DIAMONDS));
            r2 = HoldemRules.doMatchWithExclusion(cards, 2, Face.JACK.ordinal());
        }
        IntBuffer r3;
        {
            IntBuffer cards;
            cards = BuffUtil.allocate(7);
            cards.put(card(KING, DIAMONDS));
            cards.put(card(KING, SPADES));
            cards.put(card(KING, HEARTS));
            cards.put(card(KING, CLUBS));
            cards.put(card(JACK, DIAMONDS));
            r3 = HoldemRules.doMatchWithExclusion(cards, 4, -1);
        }

        assertEquals(r1.limit(), r2.limit());
        /*face*/
        assertEquals(JACK.ordinal(), r1.get(0) >>> 16);
        /*face*/
        assertEquals(KING.ordinal(), r2.get(0) >>> 16);
        /*face*/
        assertEquals(KING.ordinal(), r3.get(0) >>> 16);
        assertEquals(4, r3.limit());
    }

    public void testDoPair() throws Exception {
        IntBuffer r1;
        IntBuffer cards;
        cards = (IntBuffer) IntBuffer.wrap(new int[]
                {
                        (card(KING, DIAMONDS)),
                        (card(KING, SPADES)),
                        (card(QUEEN, SPADES)),
                        (card(JACK, SPADES)),
                        (card(EIGHT, DIAMONDS)),
                });

        r1 = doPair(cards);
        assertEquals(2, r1.limit());

        /*face*/
        assertEquals(KING.ordinal(), r1.get(0) >>> 16);

        //KdJdTs9s8h3h3s


    }

    public void testDoTrip() throws Exception {
        IntBuffer r1;
        CardMemory memory;
        {
            IntBuffer cards;
            cards = BuffUtil.allocate(7);
            cards.put(card(KING, DIAMONDS));
            cards.put(card(KING, SPADES));
            cards.put(card(KING, HEARTS));
            cards.put(card(JACK, SPADES));
            cards.put(card(EIGHT, DIAMONDS));
            memory = new CardMemory();
            r1 = HoldemRules.doTrip(cards);
        }

        assertEquals(3, r1.limit());
        /*face*/
        assertEquals(KING.ordinal(), r1.get(0) >>> 16);


    }

    public void testDoQuads() throws Exception {
        IntBuffer r1;
        CardMemory memory;
        {
            IntBuffer cards;
            cards = BuffUtil.allocate(7);
            cards.put(card(ACE, DIAMONDS));
            cards.put(card(KING, DIAMONDS));
            cards.put(card(KING, SPADES));
            cards.put(card(KING, HEARTS));
            cards.put(card(KING, CLUBS));
            cards.put(card(EIGHT, DIAMONDS));
            memory = new CardMemory();
            r1 = HoldemRules.doQuads(cards);
            assertEquals(4, r1.limit());
            /*face*/
            assertEquals(KING.ordinal(), r1.get(0) >>> 16);
        }

        //known failure
        //FOUROFAKIND:7c7s7d4c4s8s2s[name='004e25cc-74f2-4897-be0c-4a8e84264627][out=false, wins=0, stack=499.0, act=check, pocket=7s4s]
        {
            IntBuffer cards;
            cards = BuffUtil.allocate(7);
            cards.put(card(SEVEN, CLUBS));
            cards.put(card(SEVEN, DIAMONDS));
            cards.put(card(SEVEN, SPADES));
            cards.put(card(FOUR, HEARTS));
            cards.put(card(FOUR, CLUBS));
            cards.put(card(EIGHT, DIAMONDS));
            cards.put(card(TWO, DIAMONDS));
            memory = new CardMemory();
            r1 = HoldemRules.doQuads(cards);
            assertNull(r1);
        }

    }

    public void testDoFlush() throws Exception {
        final IntBuffer cards = BuffUtil.allocate(6);
        cards.put(card(ACE, HEARTS));
        cards.put(card(QUEEN, SPADES));
        cards.put(card(JACK, SPADES));
        cards.put(card(TEN, SPADES));
        cards.put(card(NINE, SPADES));
        cards.put(card(EIGHT, SPADES));
        CardMemory memory = new CardMemory();
        IntBuffer r1 = HoldemRules.doFlush(cards);
        assertEquals(5, r1.limit());
        /*face*/
        assertEquals(QUEEN.ordinal(), r1.get(0) >>> 16);


    }

    public void testDoStrait() throws Exception {
        final IntBuffer cards = BuffUtil.allocate(7);
        cards.put(card(ACE, SPADES));

        cards.put(card(QUEEN, SPADES));
        cards.put(card(JACK, SPADES));
        cards.put(card(TEN, SPADES));
        cards.put(card(NINE, SPADES));
        cards.put(card(EIGHT, SPADES));
        CardMemory memory = new CardMemory();
        IntBuffer r1 = HoldemRules.doStrait(cards);
        assertEquals(5, r1.limit());
        /*face*/
        assertEquals(QUEEN.ordinal(), r1.get(0) >>> 16);

    }

    public void testDoStraitFlush() throws Exception {
        IntBuffer cards;
        {
            cards = (IntBuffer) IntBuffer.wrap(
                    new int[]{
                            (card(ACE, HEARTS))
                            , (card(KING, SPADES))
                            , (card(QUEEN, SPADES))
                            , (card(JACK, SPADES))
                            , (card(TEN, SPADES))
                            , (card(NINE, SPADES))
                            , (card(EIGHT, SPADES))}).rewind().mark();

            final Pair<Play, IntBuffer> pair = assess(cards);
            final Play play = pair.getFirst();
            assertEquals(STRAIGHTFLUSH, play);
            IntBuffer r1 = pair.getSecond();
            assertEquals(5, r1.limit());
            /*face*/
            final int king = KING.ordinal();
            assertEquals(king, face(r1.get(0)));
        }
        {
            cards = (IntBuffer) IntBuffer.wrap(
                    new int[]{
                            (card(ACE, HEARTS))
                            , (card(SEVEN, HEARTS))
                            , (card(SIX, HEARTS))
                            , (card(FIVE, HEARTS))
                            , (card(FOUR, HEARTS))
                            , (card(THREE, DIAMONDS))
                            , (card(THREE, HEARTS))
                            , (card(THREE, SPADES))
                            , (card(TWO, HEARTS))}).rewind().mark();

            final CardMemory memory = new CardMemory();
            final Pair<Play, IntBuffer> pair = assess(cards, memory);
            final Play play = pair.getFirst();
            assertEquals(STRAIGHTFLUSH, play);
            IntBuffer r1 = pair.getSecond();
            assertEquals(5, r1.limit());
            /*face*/
            final int boss = SEVEN.ordinal();
            final int i = face(r1.get(0));
            assertEquals(boss, i);
        }
        {
            cards = (IntBuffer) IntBuffer.wrap(
                    new int[]{
                            (card(ACE, HEARTS))
                            , (card(SEVEN, HEARTS))
                            , (card(SIX, HEARTS))
                            , (card(FIVE, HEARTS))
                            , (card(FOUR, HEARTS))
                            , (card(THREE, DIAMONDS))
                            , (card(THREE, SPADES))
                            , (card(TWO, HEARTS))}).rewind().mark();
            final CardMemory memory = new CardMemory();
            final Pair<Play, IntBuffer> intBufferPair = assess(cards, memory);

            final Play play = intBufferPair.getFirst();
            final IntBuffer buffer = intBufferPair.getSecond();
            assertEquals(ACE.ordinal(), face(buffer.get(0)));
        }
//        {
//            cards = (IntBuffer) IntBuffer.wrap(
//                    new int[]{
//                            (card(KING, DIAMONDS)),
//                            (card(KING, SPADES)),
//                            (card(QUEEN, SPADES)),
//                            (card(JACK, SPADES)),
//                            (card(EIGHT, DIAMONDS))}).rewind().mark();
//            assertNull(HoldemRules.doStraitFlush(cards, new CardMemory()));
//        }
    }

    public void testDoRoyalStraitFlush() throws Exception {

        final IntBuffer cards = (IntBuffer) IntBuffer.wrap(
                new int[]{
                        (card(ACE, DIAMONDS))
                        , (card(ACE, HEARTS))
                        , (card(ACE, SPADES))
                        , (card(KING, HEARTS))
                        , (card(KING, SPADES))
                        , (card(QUEEN, HEARTS))
                        , (card(QUEEN, SPADES))
                        , (card(JACK, SPADES))
                        , (card(TEN, SPADES))
                        , (card(NINE, SPADES))
                        , (card(EIGHT, HEARTS))
                        , (card(EIGHT, SPADES))
                        , (card(SEVEN, SPADES))}).mark();
        final Pair<Play, IntBuffer> pair = assess(cards);


        final Play play = pair.getFirst();


        assertEquals(ROYALSTRAIGHTFLUSH, play);

        final IntBuffer r1 = pair.getSecond();
        assertEquals(SPADES.ordinal(), suit(r1.get()));

        /*face*/
        assertEquals(ACE.ordinal(), face(r1.get(0) ));
    }

    public void testDoFullHouse() throws Exception {
        {
            IntBuffer cards;
            cards = IntBuffer.wrap(new int[]{
                    (card(EIGHT, DIAMONDS)),
                    (card(SEVEN, CLUBS)),
                    (card(SEVEN, SPADES)),
                    (card(FOUR, SPADES)),
                    (card(FOUR, HEARTS)),
                    (card(FOUR, CLUBS)),
                    (card(TWO, DIAMONDS)),});


            IntBuffer buffer = (IntBuffer) cards.rewind().mark();
            IntBuffer r1 = (IntBuffer) doFullHouse(buffer, new CardMemory()).reset();
            System.gc();
        }
    }
 
    public void testDoTwoPair() throws Exception {
        IntBuffer r1;
        IntBuffer cards;
        cards = (IntBuffer) IntBuffer.wrap(new int[]
                {
                        (card(KING, DIAMONDS)),
                        (card(KING, SPADES)),
                        (card(QUEEN, SPADES)),
                        (card(QUEEN, HEARTS)),
                        (card(EIGHT, DIAMONDS)),
                });

        r1 = doTwoPair(cards, new CardMemory());
        assertEquals(4, r1.limit());

        /*face*/
        assertEquals(KING.ordinal(), r1.get(0) >>> 16);
        cards = (IntBuffer) IntBuffer.wrap(new int[]
                {
                        (card(ACE, DIAMONDS)),
                        (card(KING, SPADES)),
                        (card(QUEEN, SPADES)),
                        (card(QUEEN, HEARTS)),
                        (card(EIGHT, DIAMONDS)),
                });

        r1 = doTwoPair(cards, new CardMemory());
        assertNull(r1);

        //KdJdTs9s8h3h3s
    }
}