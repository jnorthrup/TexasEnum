package poker.eval;
/**
 * User: jim
 * Date: Oct 19, 2007
 * Time: 3:35:42 PM
 */

import junit.framework.*;
import poker.player.*;

import java.nio.*;

public class DeckTest extends TestCase {

    public void testShuffle() throws Exception {
        Seat.test = true;
        Deck deck = new Deck();
        deck.shuffle();

        final IntBuffer cards = deck.getCards();

        cards.rewind().mark();
        final int[] dst = new int[cards.limit()];
        cards.get(dst);
        final int[] ints = new int[cards.limit()];
        int b = 0;
        for (int i = 0; i < dst.length; i++) {
            b = dst[i];
            ints[i] = b;
        }


    }
}