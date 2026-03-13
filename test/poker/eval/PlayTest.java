package poker.eval;
/**
 * User: jim
 * Date: Oct 4, 2007
 * Time: 12:22:00 AM
 */

import junit.framework.*;
import static poker.eval.CardUtil.*;
import static poker.eval.Face.*;
import static poker.eval.Play.*;
import static poker.eval.Suit.*;
import poker.player.*;

import java.util.*;

public class PlayTest extends TestCase {
    int[] cards = new int[]{
            (card(KING, DIAMONDS)),
            (card(KING, SPADES)),
            (card(QUEEN, SPADES)),
            (card(JACK, SPADES)),
            (card(EIGHT, DIAMONDS))};

    public void testAssess() throws Exception {
        final Pair<Play, int[]> intBufferPair = Play.assess(cards);
        final Play play = intBufferPair.getFirst();
        assertEquals(PAIR, play);
        final int[] buffer = intBufferPair.getSecond();
        final int i = buffer[0];
        assertEquals(KING, face((Integer) i));
        assertEquals(2, buffer.length);
    }

}
