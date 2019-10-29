package poker.game;

import junit.framework.*;
import poker.eval.*;
import static poker.eval.CardUtil.*;
import static poker.eval.Face.*;
import static poker.eval.Suit.*;
import poker.player.*;

import java.nio.*;
import java.util.*;
import java.util.logging.*;

/**
 * User: jim
 * Date: Sep 8, 2007
 * Time: 6:53:39 PM
 */
public class GameStateTest extends TestCase {

    public void test2PlayerGame() {
//

        GameState.test = true;
        Dealer dealer = new Dealer();
        Player player;
        player = new Player();
        dealer.players.add(player);
        player = new Player();
        dealer.players.add(player);
        dealer.deck.setCards(
                (IntBuffer) IntBuffer.wrap(
                        new int[]{
                                card(THREE, HEARTS), card(THREE, CLUBS), card(EIGHT, CLUBS),
                                card(SIX, CLUBS), card(QUEEN, SPADES), card(SEVEN, SPADES),
                                card(NINE, SPADES), card(EIGHT, SPADES), card(QUEEN, HEARTS),
                                card(TEN, SPADES), card(JACK, CLUBS), card(SIX, HEARTS),
                                card(TEN, CLUBS), card(KING, HEARTS), card(FOUR, SPADES),
                                card(JACK, SPADES), card(FOUR, HEARTS), card(EIGHT, DIAMONDS),
                                card(THREE, SPADES), card(FOUR, CLUBS), card(THREE, DIAMONDS),
                                card(NINE, HEARTS), card(ACE, HEARTS), card(NINE, CLUBS),
                                card(EIGHT, HEARTS), card(SIX, SPADES), card(TEN, DIAMONDS),
                                card(TWO, HEARTS), card(ACE, DIAMONDS), card(SIX, DIAMONDS),
                                card(FIVE, CLUBS), card(FIVE, DIAMONDS), card(QUEEN, DIAMONDS),
                                card(NINE, DIAMONDS), card(SEVEN, DIAMONDS), card(ACE, CLUBS),
                                card(QUEEN, CLUBS), card(SEVEN, HEARTS), card(KING, SPADES),
                                card(FIVE, SPADES), card(JACK, DIAMONDS), card(JACK, HEARTS)}).mark());


        GameState.start(dealer);

        final IntBuffer deck = dealer.deck.getCards();
        deck.rewind().mark();
        final int[] dst = new int[deck.limit()];
        deck.get(dst);
        final int[] ints = new int[deck.limit()];
        Integer b = 0;
        ArrayList<String> s = new ArrayList<String>();


        for (int i = 0; i < dst.length; i++) {
            b = dst[i];
            ints[i] = b;
            s.add("card(" + CardUtil.face(b) + "," + CardUtil.suit(b) + ")");
        }
        Logger.getAnonymousLogger().info(s.toString());

    }

    private static int ITERATIONS = 100000;
    private static final int ITER = ITERATIONS * 1000;

    public void testGameSpeed() {
        final boolean b = GameState.test;
        Dealer.test = false;
        GameState.test = false;
        getGameSpeed();
        GameState.test = b;
    }

    private long getGameSpeed() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++) {
            Dealer dealer = new Dealer();
            for (int pgen = 0; pgen < 10; pgen++)
                dealer.players.add(new Player());
            GameState.start(dealer);
        }

        long duration = System.currentTimeMillis() - start;

        Logger.getAnonymousLogger().info("ITERATIONS (" + ITER + ")/ duration (" + duration + ")=  " + ((float) ITER / duration));

        return ITER / duration;
    }

    private static int TOTAL = 100000;
}