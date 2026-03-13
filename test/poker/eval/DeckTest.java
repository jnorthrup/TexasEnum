package poker.eval;
/**
 * User: jim
 * Date: Oct 19, 2007
 * Time: 3:35:42 PM
 */

import junit.framework.*;
import poker.player.*;

public class DeckTest extends TestCase {

    public void testShuffle() throws Exception {
        Seat.test = true;
        Deck deck = new Deck();
        deck.shuffle();

        final int[] cards = deck.getCards();

        final int[] dst = new int[cards.length];
        System.arraycopy(cards, 0, dst, 0, cards.length);
        final int[] ints = new int[cards.length];
        int b = 0;
        for (int i = 0; i < dst.length; i++) {
            b = dst[i];
            ints[i] = b;
        }


    }
}
